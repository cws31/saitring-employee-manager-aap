package dev.sonu.saitring_employee_manager.attendance.service;

import dev.sonu.saitring_employee_manager.attendance.dto.*;
import dev.sonu.saitring_employee_manager.attendance.entity.Attendance;
import dev.sonu.saitring_employee_manager.attendance.entity.AttendanceStatus;
import dev.sonu.saitring_employee_manager.attendance.repository.AttendanceRepository;
import dev.sonu.saitring_employee_manager.common.exception.BadRequestException;
import dev.sonu.saitring_employee_manager.common.exception.ResourceNotFoundException;
import dev.sonu.saitring_employee_manager.employee.entity.Employee;
import dev.sonu.saitring_employee_manager.employee.entity.EmployeeStatus;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRepository;
import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final OwnerRepository ownerRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 EmployeeRepository employeeRepository,
                                 OwnerRepository ownerRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.ownerRepository = ownerRepository;
    }

    @Override
    @Transactional
    public AttendanceResponse markAttendance(Long ownerId, MarkAttendanceRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner tenant not found with ID: " + ownerId));

        Employee employee = employeeRepository.findByIdAndOwnerId(request.getEmployeeId(), ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new BadRequestException("Cannot mark attendance for inactive/blocked employee: " + employee.getEmployeeName());
        }

        if (request.getAttendanceDate().isBefore(employee.getJoiningDate())) {
            throw new BadRequestException("Cannot mark attendance before employee joining date [" +
                    employee.getJoiningDate() + "]. Requested date: [" + request.getAttendanceDate() + "]");
        }

        Optional<Attendance> existingOpt = attendanceRepository.findByOwnerIdAndEmployeeIdAndAttendanceDate(
                ownerId, employee.getId(), request.getAttendanceDate()
        );

        Attendance attendance;
        if (existingOpt.isPresent()) {
            attendance = existingOpt.get();
            attendance.setStatus(request.getStatus());
            attendance.setNote(request.getNote());
            log.info("Updated attendance ID [{}] for employee [{}] on [{}] to [{}]",
                    attendance.getId(), employee.getId(), request.getAttendanceDate(), request.getStatus());
        } else {
            attendance = new Attendance(owner, employee, request.getAttendanceDate(), request.getStatus(), request.getNote());
            log.info("Marked new attendance for employee [{}] on [{}] as [{}]",
                    employee.getId(), request.getAttendanceDate(), request.getStatus());
        }

        Attendance saved = attendanceRepository.save(attendance);
        return AttendanceResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse updateAttendance(Long ownerId, Long id, UpdateAttendanceRequest request) {
        Attendance attendance = attendanceRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with ID: " + id));

        attendance.setStatus(request.getStatus());
        attendance.setNote(request.getNote());

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Updated attendance record [{}] for owner [{}] to status [{}]", id, ownerId, request.getStatus());
        return AttendanceResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceById(Long ownerId, Long id) {
        Attendance attendance = attendanceRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with ID: " + id));
        return AttendanceResponse.fromEntity(attendance);
    }

    @Override
    @Transactional
    public void deleteAttendance(Long ownerId, Long id) {
        Attendance attendance = attendanceRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with ID: " + id));
        attendanceRepository.delete(attendance);
        log.info("Deleted attendance record [{}] for owner [{}]", id, ownerId);
    }

    @Override
    @Transactional
    public BulkAttendanceResponse bulkMarkAttendance(Long ownerId, BulkMarkAttendanceRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner tenant not found with ID: " + ownerId));

        List<BulkAttendanceItem> items = request.getAttendances();
        Set<Long> employeeIds = items.stream()
                .map(BulkAttendanceItem::getEmployeeId)
                .collect(Collectors.toSet());

        List<Employee> employees = employeeRepository.findAllByOwnerIdAndIdIn(ownerId, employeeIds);
        Map<Long, Employee> employeeMap = employees.stream()
                .collect(Collectors.toMap(Employee::getId, e -> e));

        // Validate all requested employees exist and belong to owner
        for (Long empId : employeeIds) {
            if (!employeeMap.containsKey(empId)) {
                throw new ResourceNotFoundException("Employee not found with ID: " + empId);
            }
        }

        // Validate active status and joining date
        for (BulkAttendanceItem item : items) {
            Employee emp = employeeMap.get(item.getEmployeeId());
            if (emp.getStatus() != EmployeeStatus.ACTIVE) {
                throw new BadRequestException("Cannot mark attendance for inactive/blocked employee: " + emp.getEmployeeName());
            }
            if (request.getAttendanceDate().isBefore(emp.getJoiningDate())) {
                throw new BadRequestException("Cannot mark attendance before employee joining date [" +
                        emp.getJoiningDate() + "] for employee [" + emp.getEmployeeName() + "]. Requested date: [" +
                        request.getAttendanceDate() + "]");
            }
        }

        // Find existing attendance records for this date
        List<Attendance> existingAttendances = attendanceRepository.findByOwnerIdAndAttendanceDateAndEmployeeIdIn(
                ownerId, request.getAttendanceDate(), employeeIds
        );
        Map<Long, Attendance> existingMap = existingAttendances.stream()
                .collect(Collectors.toMap(a -> a.getEmployee().getId(), a -> a));

        List<Attendance> toSave = new ArrayList<>();
        for (BulkAttendanceItem item : items) {
            Employee emp = employeeMap.get(item.getEmployeeId());
            Attendance existing = existingMap.get(item.getEmployeeId());
            if (existing != null) {
                existing.setStatus(item.getStatus());
                existing.setNote(item.getNote());
                toSave.add(existing);
            } else {
                toSave.add(new Attendance(owner, emp, request.getAttendanceDate(), item.getStatus(), item.getNote()));
            }
        }

        List<Attendance> savedList = attendanceRepository.saveAll(toSave);
        log.info("Bulk marked [{}] attendance records for owner [{}] on [{}]", savedList.size(), ownerId, request.getAttendanceDate());

        List<AttendanceResponse> responseList = savedList.stream()
                .map(AttendanceResponse::fromEntity)
                .toList();

        return new BulkAttendanceResponse(request.getAttendanceDate(), responseList.size(), responseList);
    }

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "employeeName", "mobileNumber", "dailyRate", "joiningDate"
    );

    private Pageable sanitizePageable(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "joiningDate", "employeeName"));
        }

        List<Sort.Order> validOrders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            if (ALLOWED_SORT_PROPERTIES.contains(property)) {
                validOrders.add(order);
            }
        }

        Sort cleanSort = validOrders.isEmpty()
                ? Sort.by(Sort.Direction.ASC, "joiningDate", "employeeName")
                : Sort.by(validOrders);

        int pageNumber = Math.max(0, pageable.getPageNumber());
        int pageSize = pageable.getPageSize() > 0 && pageable.getPageSize() <= 100 ? pageable.getPageSize() : 20;

        return PageRequest.of(pageNumber, pageSize, cleanSort);
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyRegisterResponse getMonthlyRegister(Long ownerId, YearMonth yearMonth, Pageable pageable) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        if (pageable != null && pageable.isUnpaged()) {
            List<Employee> eligibleEmployees = employeeRepository.findEligibleEmployees(ownerId, endDate);
            List<Attendance> attendances = attendanceRepository.findByOwnerIdAndAttendanceDateBetween(ownerId, startDate, endDate);
            List<RegisterEmployeeRow> employeeRows = buildEmployeeRows(eligibleEmployees, attendances);
            return new MonthlyRegisterResponse(yearMonth.toString(), yearMonth.lengthOfMonth(), eligibleEmployees.size(), employeeRows);
        }

        Pageable safePageable = sanitizePageable(pageable);
        Page<Employee> employeePage = employeeRepository.findEligibleEmployees(ownerId, endDate, safePageable);

        List<Attendance> attendances;
        if (employeePage.isEmpty()) {
            attendances = Collections.emptyList();
        } else {
            List<Long> employeeIds = employeePage.getContent().stream().map(Employee::getId).toList();
            attendances = attendanceRepository.findByOwnerIdAndAttendanceDateBetweenAndEmployeeIdIn(
                    ownerId, startDate, endDate, employeeIds
            );
        }

        List<RegisterEmployeeRow> employeeRows = buildEmployeeRows(employeePage.getContent(), attendances);

        return new MonthlyRegisterResponse(
                yearMonth.toString(),
                yearMonth.lengthOfMonth(),
                (int) employeePage.getTotalElements(),
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages(),
                employeePage.isFirst(),
                employeePage.isLast(),
                employeePage.hasNext(),
                employeePage.hasPrevious(),
                employeeRows
        );
    }

    private List<RegisterEmployeeRow> buildEmployeeRows(List<Employee> employees, List<Attendance> attendances) {
        Map<Long, List<Attendance>> attendanceByEmployee = attendances.stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

        List<RegisterEmployeeRow> employeeRows = new ArrayList<>();
        for (Employee emp : employees) {
            List<Attendance> empAttendances = attendanceByEmployee.getOrDefault(emp.getId(), Collections.emptyList());

            Map<Integer, AttendanceStatus> dailyStatus = new TreeMap<>();
            int present = 0;
            int half = 0;
            int absent = 0;

            for (Attendance a : empAttendances) {
                dailyStatus.put(a.getAttendanceDate().getDayOfMonth(), a.getStatus());
                if (a.getStatus() == AttendanceStatus.PRESENT) {
                    present++;
                } else if (a.getStatus() == AttendanceStatus.HALF_DAY) {
                    half++;
                } else if (a.getStatus() == AttendanceStatus.ABSENT) {
                    absent++;
                }
            }

            double effectiveDays = present + (0.5 * half);
            BigDecimal wages = emp.getDailyRate().multiply(BigDecimal.valueOf(effectiveDays)).setScale(2, java.math.RoundingMode.HALF_UP);

            RegisterEmployeeRow row = new RegisterEmployeeRow();
            row.setEmployeeId(emp.getId());
            row.setEmployeeName(emp.getEmployeeName());
            row.setMobileNumber(emp.getMobileNumber());
            row.setDailyRate(emp.getDailyRate());
            row.setJoiningDate(emp.getJoiningDate());
            row.setDailyStatus(dailyStatus);
            row.setPresentCount(present);
            row.setHalfDayCount(half);
            row.setAbsentCount(absent);
            row.setTotalEffectiveDays(effectiveDays);
            row.setEstimatedWages(wages);

            employeeRows.add(row);
        }
        return employeeRows;
    }

    @Override
    @Transactional(readOnly = true)
    public DailyAttendanceSummaryResponse getDailySummary(Long ownerId, LocalDate date) {
        // 1. Eligible employees active and joined on or before date
        List<Employee> eligibleEmployees = employeeRepository.findEligibleEmployees(ownerId, date);

        // 2. Marked attendances on this date
        List<Attendance> attendances = attendanceRepository.findByOwnerIdAndAttendanceDateWithEmployee(ownerId, date);
        Map<Long, Attendance> attendanceMap = attendances.stream()
                .collect(Collectors.toMap(a -> a.getEmployee().getId(), a -> a));

        int present = 0;
        int half = 0;
        int absent = 0;

        List<DailyEmployeeAttendanceRecord> records = new ArrayList<>();
        for (Employee emp : eligibleEmployees) {
            Attendance att = attendanceMap.get(emp.getId());
            if (att != null) {
                if (att.getStatus() == AttendanceStatus.PRESENT) {
                    present++;
                } else if (att.getStatus() == AttendanceStatus.HALF_DAY) {
                    half++;
                } else if (att.getStatus() == AttendanceStatus.ABSENT) {
                    absent++;
                }

                records.add(new DailyEmployeeAttendanceRecord(
                        emp.getId(), emp.getEmployeeName(), emp.getMobileNumber(), emp.getDailyRate(), emp.getJoiningDate(),
                        att.getId(), att.getStatus(), att.getNote(), true
                ));
            } else {
                records.add(new DailyEmployeeAttendanceRecord(
                        emp.getId(), emp.getEmployeeName(), emp.getMobileNumber(), emp.getDailyRate(), emp.getJoiningDate(),
                        null, null, null, false
                ));
            }
        }

        int totalMarked = attendances.size();
        int totalUnmarked = eligibleEmployees.size() - totalMarked;
        double totalEffectiveDays = present + (0.5 * half);

        DailyAttendanceSummaryResponse response = new DailyAttendanceSummaryResponse();
        response.setAttendanceDate(date);
        response.setTotalEligibleEmployees(eligibleEmployees.size());
        response.setTotalMarked(totalMarked);
        response.setTotalUnmarked(Math.max(0, totalUnmarked));
        response.setPresentCount(present);
        response.setHalfDayCount(half);
        response.setAbsentCount(absent);
        response.setTotalEffectiveDays(totalEffectiveDays);
        response.setRecords(records);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyAttendanceSummaryResponse getMonthlySummary(Long ownerId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Employee> eligibleEmployees = employeeRepository.findEligibleEmployees(ownerId, endDate);
        List<Attendance> attendances = attendanceRepository.findByOwnerIdAndAttendanceDateBetween(ownerId, startDate, endDate);

        Map<Long, List<Attendance>> attByEmp = attendances.stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

        int totalPresent = 0;
        int totalHalf = 0;
        int totalAbsent = 0;
        BigDecimal totalWages = BigDecimal.ZERO;

        for (Attendance a : attendances) {
            if (a.getStatus() == AttendanceStatus.PRESENT) {
                totalPresent++;
            } else if (a.getStatus() == AttendanceStatus.HALF_DAY) {
                totalHalf++;
            } else if (a.getStatus() == AttendanceStatus.ABSENT) {
                totalAbsent++;
            }
        }

        for (Employee emp : eligibleEmployees) {
            List<Attendance> empAtts = attByEmp.getOrDefault(emp.getId(), Collections.emptyList());
            long empPresent = empAtts.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long empHalf = empAtts.stream().filter(a -> a.getStatus() == AttendanceStatus.HALF_DAY).count();
            double empEffectiveDays = empPresent + (0.5 * empHalf);
            BigDecimal empWages = emp.getDailyRate().multiply(BigDecimal.valueOf(empEffectiveDays)).setScale(2, java.math.RoundingMode.HALF_UP);
            totalWages = totalWages.add(empWages);
        }

        double totalEffectiveDays = totalPresent + (0.5 * totalHalf);

        MonthlyAttendanceSummaryResponse summary = new MonthlyAttendanceSummaryResponse();
        summary.setMonth(yearMonth.toString());
        summary.setTotalDaysInMonth(yearMonth.lengthOfMonth());
        summary.setTotalEligibleEmployees(eligibleEmployees.size());
        summary.setTotalPresentRecords(totalPresent);
        summary.setTotalHalfDayRecords(totalHalf);
        summary.setTotalAbsentRecords(totalAbsent);
        summary.setTotalEffectiveDays(totalEffectiveDays);
        summary.setTotalEstimatedWages(totalWages);

        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeMonthlySummaryResponse getEmployeeMonthlySummary(Long ownerId, Long employeeId, YearMonth yearMonth) {
        Employee employee = employeeRepository.findByIdAndOwnerId(employeeId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Attendance> attendances = attendanceRepository.findByOwnerIdAndEmployeeIdAndAttendanceDateBetween(
                ownerId, employeeId, startDate, endDate
        );

        int present = 0;
        int half = 0;
        int absent = 0;

        for (Attendance a : attendances) {
            if (a.getStatus() == AttendanceStatus.PRESENT) {
                present++;
            } else if (a.getStatus() == AttendanceStatus.HALF_DAY) {
                half++;
            } else if (a.getStatus() == AttendanceStatus.ABSENT) {
                absent++;
            }
        }

        double effectiveDays = present + (0.5 * half);
        BigDecimal estimatedSalary = employee.getDailyRate().multiply(BigDecimal.valueOf(effectiveDays)).setScale(2, java.math.RoundingMode.HALF_UP);
        List<AttendanceResponse> history = attendances.stream()
                .map(AttendanceResponse::fromEntity)
                .toList();

        EmployeeMonthlySummaryResponse response = new EmployeeMonthlySummaryResponse();
        response.setEmployeeId(employee.getId());
        response.setEmployeeName(employee.getEmployeeName());
        response.setMobileNumber(employee.getMobileNumber());
        response.setDailyRate(employee.getDailyRate());
        response.setJoiningDate(employee.getJoiningDate());
        response.setMonth(yearMonth.toString());
        response.setTotalPresent(present);
        response.setTotalHalfDay(half);
        response.setTotalAbsent(absent);
        response.setEffectiveDays(effectiveDays);
        response.setEstimatedSalary(estimatedSalary);
        response.setAttendanceHistory(history);

        return response;
    }
}
