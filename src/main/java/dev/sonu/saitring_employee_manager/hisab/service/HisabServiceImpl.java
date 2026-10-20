package dev.sonu.saitring_employee_manager.hisab.service;

import dev.sonu.saitring_employee_manager.advance.entity.Advance;
import dev.sonu.saitring_employee_manager.advance.repository.AdvanceRepository;
import dev.sonu.saitring_employee_manager.attendance.entity.Attendance;
import dev.sonu.saitring_employee_manager.attendance.entity.AttendanceStatus;
import dev.sonu.saitring_employee_manager.attendance.repository.AttendanceRepository;
import dev.sonu.saitring_employee_manager.common.exception.BadRequestException;
import dev.sonu.saitring_employee_manager.common.exception.ResourceNotFoundException;
import dev.sonu.saitring_employee_manager.employee.entity.Employee;
import dev.sonu.saitring_employee_manager.employee.entity.EmployeeRateHistory;
import dev.sonu.saitring_employee_manager.employee.entity.EmployeeStatus;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRateHistoryRepository;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRepository;
import dev.sonu.saitring_employee_manager.hisab.dto.*;
import dev.sonu.saitring_employee_manager.hisab.entity.HisabStatus;
import dev.sonu.saitring_employee_manager.hisab.entity.MonthlyHisab;
import dev.sonu.saitring_employee_manager.hisab.repository.HisabRepository;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HisabServiceImpl implements HisabService {

    private static final Logger log = LoggerFactory.getLogger(HisabServiceImpl.class);

    private static final Set<String> ALLOWED_EMPLOYEE_SORT_PROPERTIES = Set.of(
            "id", "employeeName", "mobileNumber", "dailyRate", "joiningDate"
    );

    private final HisabRepository hisabRepository;
    private final AttendanceRepository attendanceRepository;
    private final AdvanceRepository advanceRepository;
    private final EmployeeRepository employeeRepository;
    private final OwnerRepository ownerRepository;
    private final EmployeeRateHistoryRepository employeeRateHistoryRepository;

    public HisabServiceImpl(HisabRepository hisabRepository,
                            AttendanceRepository attendanceRepository,
                            AdvanceRepository advanceRepository,
                            EmployeeRepository employeeRepository,
                            OwnerRepository ownerRepository,
                            EmployeeRateHistoryRepository employeeRateHistoryRepository) {
        this.hisabRepository = hisabRepository;
        this.attendanceRepository = attendanceRepository;
        this.advanceRepository = advanceRepository;
        this.employeeRepository = employeeRepository;
        this.ownerRepository = ownerRepository;
        this.employeeRateHistoryRepository = employeeRateHistoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyHisabTableResponse getMonthlyHisabTable(Long ownerId, YearMonth yearMonth, Pageable pageable) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 1. Paginate active employees who joined on or before month end
        Pageable safePageable = sanitizePageable(pageable, ALLOWED_EMPLOYEE_SORT_PROPERTIES, "joiningDate", Sort.Direction.ASC);
        Page<Employee> employeePage = employeeRepository.findEligibleEmployees(ownerId, endDate, safePageable);

        List<HisabTableRow> rows = new ArrayList<>();
        if (!employeePage.isEmpty()) {
            List<Long> employeeIds = employeePage.getContent().stream().map(Employee::getId).toList();

            // 2. Fetch attendances for employees on this page
            List<Attendance> attendances = attendanceRepository.findByOwnerIdAndAttendanceDateBetweenAndEmployeeIdIn(
                    ownerId, startDate, endDate, employeeIds
            );
            Map<Long, List<Attendance>> attByEmp = attendances.stream()
                    .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

            // 3. Fetch monthly advances for employees on this page
            List<Advance> advances = advanceRepository.findByOwnerIdAndAdvanceDateBetweenAndEmployeeIdIn(
                    ownerId, startDate, endDate, employeeIds
            );
            Map<Long, List<Advance>> advByEmp = advances.stream()
                    .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

            // 4. Fetch cumulative running advances for employees on this page
            List<Object[]> runningAdvances = advanceRepository.sumRunningAdvanceByEmployeeIds(ownerId, employeeIds);
            Map<Long, BigDecimal> runningAdvMap = new HashMap<>();
            for (Object[] row : runningAdvances) {
                Long empId = (Long) row[0];
                BigDecimal bal = (BigDecimal) row[1];
                runningAdvMap.put(empId, bal != null ? bal.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            }

            // 5. Fetch settled hisab records if already settled for any of these employees
            List<MonthlyHisab> settledList = hisabRepository.findByOwnerIdAndMonthAndEmployeeIdIn(
                    ownerId, yearMonth.toString(), employeeIds
            );
            Map<Long, MonthlyHisab> settledMap = settledList.stream()
                    .collect(Collectors.toMap(h -> h.getEmployee().getId(), h -> h));

            // 6. Fetch historical wage rates active on or before month end for employees on this page
            List<EmployeeRateHistory> rates = employeeRateHistoryRepository.findEffectiveRatesForEmployeesOnOrBefore(
                    ownerId, employeeIds, endDate
            );
            Map<Long, BigDecimal> effectiveRateByEmp = new HashMap<>();
            for (EmployeeRateHistory rh : rates) {
                effectiveRateByEmp.putIfAbsent(rh.getEmployee().getId(), rh.getDailyRate().setScale(2, RoundingMode.HALF_UP));
            }

            // 7. Fetch prior settled hisab carry-forward balances for opening advance balance calculation
            List<MonthlyHisab> priorSettled = hisabRepository.findPriorSettledHisabsForEmployees(
                    ownerId, employeeIds, yearMonth.toString()
            );
            Map<Long, BigDecimal> openingBalanceMap = new HashMap<>();
            for (MonthlyHisab ph : priorSettled) {
                openingBalanceMap.putIfAbsent(ph.getEmployee().getId(), ph.getCarryForward());
            }

            for (Employee emp : employeePage.getContent()) {
                MonthlyHisab settled = settledMap.get(emp.getId());
                HisabTableRow row = new HisabTableRow();
                row.setEmployeeId(emp.getId());
                row.setEmployeeName(emp.getEmployeeName());
                row.setMobileNumber(emp.getMobileNumber());
                row.setJoiningDate(emp.getJoiningDate());

                BigDecimal activeRate = effectiveRateByEmp.getOrDefault(
                        emp.getId(),
                        emp.getDailyRate().setScale(2, RoundingMode.HALF_UP)
                );
                row.setRate(activeRate);

                if (settled != null) {
                    // Use settled snapshot values
                    row.setFullDays(settled.getFullDays());
                    row.setHalfDays(settled.getHalfDays());
                    row.setTotalPresenties(settled.getEffectiveDays() != null ? settled.getEffectiveDays().doubleValue() : 0.0);
                    row.setRate(settled.getDailyRate());
                    row.setTotalEarnings(settled.getTotalEarnings());
                    row.setOpeningBalance(settled.getOpeningBalance());
                    row.setMonthlyAdvance(settled.getTotalAdvance().add(settled.getOpeningBalance()).setScale(2, RoundingMode.HALF_UP));
                    row.setTotalAdvance(settled.getTotalAdvance());
                    row.setFinalMoney(settled.getFinalMoney());
                    row.setSettlementAmount(settled.getSettlementAmount());
                    row.setCarryForward(settled.getCarryForward());
                    row.setStatus(settled.getStatus());
                    row.setPaymentMode(settled.getPaymentMode());
                    row.setSettledAt(settled.getSettledAt());
                    row.setNote(settled.getNote());
                } else {
                    // Dynamic live running hisab computation using month's active wage rate
                    List<Attendance> empAtts = attByEmp.getOrDefault(emp.getId(), Collections.emptyList());
                    int full = (int) empAtts.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
                    int half = (int) empAtts.stream().filter(a -> a.getStatus() == AttendanceStatus.HALF_DAY).count();
                    double effectivePresenties = full + (0.5 * half);
                    BigDecimal earnings = activeRate.multiply(BigDecimal.valueOf(effectivePresenties))
                            .setScale(2, RoundingMode.HALF_UP);

                    List<Advance> empAdvs = advByEmp.getOrDefault(emp.getId(), Collections.emptyList());
                    BigDecimal monthAdv = empAdvs.stream()
                            .map(Advance::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(2, RoundingMode.HALF_UP);

                    BigDecimal openingBal = openingBalanceMap.getOrDefault(emp.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal totalAdv = monthAdv.subtract(openingBal).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal finalMoney = earnings.subtract(totalAdv).setScale(2, RoundingMode.HALF_UP);

                    row.setFullDays(full);
                    row.setHalfDays(half);
                    row.setTotalPresenties(effectivePresenties);
                    row.setTotalEarnings(earnings);
                    row.setOpeningBalance(openingBal);
                    row.setMonthlyAdvance(monthAdv);
                    row.setTotalAdvance(totalAdv);
                    row.setFinalMoney(finalMoney);
                    row.setSettlementAmount(null);
                    row.setCarryForward(null);
                    row.setStatus(HisabStatus.RUNNING);
                }

                BigDecimal runningBal = runningAdvMap.getOrDefault(emp.getId(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                row.setRunningAdvanceBalance(runningBal);

                rows.add(row);
            }
        }

        // Overall organization month calculations
        List<Attendance> allMonthAttendances = attendanceRepository.findByOwnerIdAndAttendanceDateBetween(ownerId, startDate, endDate);
        int totalFull = (int) allMonthAttendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        int totalHalf = (int) allMonthAttendances.stream().filter(a -> a.getStatus() == AttendanceStatus.HALF_DAY).count();
        double totalEffectivePresenties = totalFull + (0.5 * totalHalf);

        // Calculate total earnings across all eligible employees in this month using their month-effective rates
        List<Employee> allEligibleEmployees = employeeRepository.findEligibleEmployees(ownerId, endDate);
        Map<Long, List<Attendance>> allAttByEmp = allMonthAttendances.stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

        List<Long> allEmpIds = allEligibleEmployees.stream().map(Employee::getId).toList();
        Map<Long, BigDecimal> allOrgEffectiveRates = new HashMap<>();
        if (!allEmpIds.isEmpty()) {
            List<EmployeeRateHistory> allRates = employeeRateHistoryRepository.findEffectiveRatesForEmployeesOnOrBefore(
                    ownerId, allEmpIds, endDate
            );
            for (EmployeeRateHistory rh : allRates) {
                allOrgEffectiveRates.putIfAbsent(rh.getEmployee().getId(), rh.getDailyRate().setScale(2, RoundingMode.HALF_UP));
            }
        }

        BigDecimal totalOrgEarnings = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (Employee e : allEligibleEmployees) {
            BigDecimal empRate = allOrgEffectiveRates.getOrDefault(e.getId(), e.getDailyRate().setScale(2, RoundingMode.HALF_UP));
            List<Attendance> atts = allAttByEmp.getOrDefault(e.getId(), Collections.emptyList());
            long f = atts.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long h = atts.stream().filter(a -> a.getStatus() == AttendanceStatus.HALF_DAY).count();
            double eff = f + (0.5 * h);
            BigDecimal empEarnings = empRate.multiply(BigDecimal.valueOf(eff)).setScale(2, RoundingMode.HALF_UP);
            totalOrgEarnings = totalOrgEarnings.add(empEarnings);
        }

        BigDecimal rawTotalOrgAdvance = advanceRepository.sumAdvancesDisbursedBetween(ownerId, startDate, endDate);
        BigDecimal totalOrgAdvance = rawTotalOrgAdvance != null
                ? rawTotalOrgAdvance.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalOrgFinalMoney = totalOrgEarnings.subtract(totalOrgAdvance).setScale(2, RoundingMode.HALF_UP);

        return new MonthlyHisabTableResponse(
                yearMonth.toString(),
                yearMonth.lengthOfMonth(),
                (int) employeePage.getTotalElements(),
                totalFull,
                totalHalf,
                totalEffectivePresenties,
                totalOrgEarnings,
                totalOrgAdvance,
                totalOrgFinalMoney,
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages(),
                employeePage.isFirst(),
                employeePage.isLast(),
                employeePage.hasNext(),
                employeePage.hasPrevious(),
                rows
        );
    }

    @Override
    @Transactional
    public HisabResponse settleHisab(Long ownerId, SettleHisabRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner tenant not found with ID: " + ownerId));

        Employee employee = employeeRepository.findByIdAndOwnerId(request.getEmployeeId(), ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new BadRequestException("Cannot settle hisab for inactive/blocked employee: " + employee.getEmployeeName());
        }

        YearMonth ym;
        try {
            ym = YearMonth.parse(request.getMonth().trim());
        } catch (Exception e) {
            throw new BadRequestException("Invalid month format [" + request.getMonth() + "]. Expected format is YYYY-MM");
        }

        if (ym.atEndOfMonth().isBefore(employee.getJoiningDate())) {
            throw new BadRequestException("Cannot settle hisab before employee joining date [" +
                    employee.getJoiningDate() + "]. Requested month: [" + request.getMonth() + "]");
        }

        if (hisabRepository.existsByOwnerIdAndEmployeeIdAndMonth(ownerId, employee.getId(), request.getMonth())) {
            throw new BadRequestException("Hisab already settled for employee [" +
                    employee.getEmployeeName() + "] in month [" + request.getMonth() + "]");
        }

        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        // 1. Determine effective wage rate for the month and calculate attendance earnings
        List<EmployeeRateHistory> rateList = employeeRateHistoryRepository.findEffectiveRatesOnOrBefore(
                ownerId, employee.getId(), endDate, PageRequest.of(0, 1)
        );
        BigDecimal effectiveRate = !rateList.isEmpty()
                ? rateList.get(0).getDailyRate().setScale(2, RoundingMode.HALF_UP)
                : employee.getDailyRate().setScale(2, RoundingMode.HALF_UP);

        List<Attendance> attendances = attendanceRepository.findByOwnerIdAndEmployeeIdAndAttendanceDateBetween(
                ownerId, employee.getId(), startDate, endDate
        );
        int full = (int) attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        int half = (int) attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.HALF_DAY).count();
        double effectiveDays = full + (0.5 * half);
        BigDecimal earnings = effectiveRate.multiply(BigDecimal.valueOf(effectiveDays))
                .setScale(2, RoundingMode.HALF_UP);

        // 2. Fetch opening advance balance from latest prior settled hisab
        List<MonthlyHisab> priorList = hisabRepository.findPriorSettledHisab(
                ownerId, employee.getId(), request.getMonth().trim(), PageRequest.of(0, 1)
        );
        BigDecimal openingBalance = !priorList.isEmpty()
                ? priorList.get(0).getCarryForward()
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // 3. Calculate month advances and mark them as settled
        List<Advance> advances = advanceRepository.findByOwnerIdAndEmployeeIdAndAdvanceDateBetween(
                ownerId, employee.getId(), startDate, endDate
        );
        BigDecimal monthAdvance = advances.stream()
                .map(Advance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        Instant now = Instant.now();
        for (Advance adv : advances) {
            adv.setSettled(true);
            adv.setSettledAt(now);
        }
        advanceRepository.saveAll(advances);

        BigDecimal totalAdvance = monthAdvance.subtract(openingBalance).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalMoney = earnings.subtract(totalAdvance).setScale(2, RoundingMode.HALF_UP);

        BigDecimal settlementAmount = request.getSettlementAmount() != null
                ? request.getSettlementAmount().setScale(2, RoundingMode.HALF_UP)
                : finalMoney;

        BigDecimal carryForward = finalMoney.subtract(settlementAmount).setScale(2, RoundingMode.HALF_UP);

        MonthlyHisab hisab = new MonthlyHisab(
                owner,
                employee,
                request.getMonth(),
                effectiveRate,
                full,
                half,
                effectiveDays,
                openingBalance,
                earnings,
                totalAdvance,
                finalMoney,
                settlementAmount,
                carryForward,
                HisabStatus.SETTLED,
                request.getPaymentMode(),
                now,
                request.getNote()
        );

        MonthlyHisab saved = hisabRepository.save(hisab);
        log.info("Settled monthly hisab ID [{}] for employee [{}] in month [{}] with final money [{}], settlement amount [{}], carry forward [{}]",
                saved.getId(), employee.getId(), request.getMonth(), finalMoney, settlementAmount, carryForward);

        return HisabResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeHisabHistoryResponse getEmployeeHisabHistory(Long ownerId, Long employeeId, Pageable pageable) {
        Employee employee = employeeRepository.findByIdAndOwnerId(employeeId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        Pageable safePageable = sanitizePageable(pageable, Set.of("id", "month", "settledAt"), "month", Sort.Direction.DESC);
        Page<MonthlyHisab> settlementsPage = hisabRepository.findEmployeeHisabHistory(ownerId, employeeId, safePageable);

        BigDecimal runningAdvanceBalance = advanceRepository.sumRunningAdvanceByEmployee(ownerId, employeeId)
                .setScale(2, RoundingMode.HALF_UP);

        Page<HisabResponse> responsePage = settlementsPage.map(HisabResponse::fromEntity);

        return new EmployeeHisabHistoryResponse(
                employee.getId(),
                employee.getEmployeeName(),
                employee.getMobileNumber(),
                employee.getDailyRate(),
                employee.getJoiningDate(),
                runningAdvanceBalance,
                responsePage
        );
    }

    private Pageable sanitizePageable(Pageable pageable, Set<String> allowedProperties, String defaultSortProp, Sort.Direction defaultDirection) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, 20, Sort.by(defaultDirection, defaultSortProp));
        }

        List<Sort.Order> validOrders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            if (allowedProperties.contains(order.getProperty())) {
                validOrders.add(order);
            }
        }

        Sort cleanSort = validOrders.isEmpty()
                ? Sort.by(defaultDirection, defaultSortProp)
                : Sort.by(validOrders);

        int pageNumber = Math.max(0, pageable.getPageNumber());
        int pageSize = pageable.getPageSize() > 0 && pageable.getPageSize() <= 100 ? pageable.getPageSize() : 20;

        return PageRequest.of(pageNumber, pageSize, cleanSort);
    }
}
