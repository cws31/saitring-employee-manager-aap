package dev.sonu.saitring_employee_manager.advance.service;

import dev.sonu.saitring_employee_manager.advance.dto.*;
import dev.sonu.saitring_employee_manager.advance.entity.Advance;
import dev.sonu.saitring_employee_manager.advance.repository.AdvanceRepository;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdvanceServiceImpl implements AdvanceService {

    private static final Logger log = LoggerFactory.getLogger(AdvanceServiceImpl.class);

    private static final Set<String> ALLOWED_EMPLOYEE_SORT_PROPERTIES = Set.of(
            "id", "employeeName", "mobileNumber", "dailyRate", "joiningDate"
    );

    private static final Set<String> ALLOWED_ADVANCE_SORT_PROPERTIES = Set.of(
            "id", "amount", "advanceDate", "createdAt"
    );

    private final AdvanceRepository advanceRepository;
    private final EmployeeRepository employeeRepository;
    private final OwnerRepository ownerRepository;

    public AdvanceServiceImpl(AdvanceRepository advanceRepository,
                              EmployeeRepository employeeRepository,
                              OwnerRepository ownerRepository) {
        this.advanceRepository = advanceRepository;
        this.employeeRepository = employeeRepository;
        this.ownerRepository = ownerRepository;
    }

    @Override
    @Transactional
    public AdvanceResponse createAdvance(Long ownerId, CreateAdvanceRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner tenant not found with ID: " + ownerId));

        Employee employee = employeeRepository.findByIdAndOwnerId(request.getEmployeeId(), ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new BadRequestException("Cannot disburse advance to inactive/blocked employee: " + employee.getEmployeeName());
        }

        if (request.getAdvanceDate().isBefore(employee.getJoiningDate())) {
            throw new BadRequestException("Cannot disburse advance before employee joining date [" +
                    employee.getJoiningDate() + "]. Requested advance date: [" + request.getAdvanceDate() + "]");
        }

        BigDecimal scaledAmount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        Advance advance = new Advance(owner, employee, scaledAmount, request.getAdvanceDate(), request.getNote());
        Advance saved = advanceRepository.save(advance);

        log.info("Disbursed advance ID [{}] of amount [{}] to employee [{}] for owner [{}]",
                saved.getId(), saved.getAmount(), employee.getId(), ownerId);

        return AdvanceResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public AdvanceResponse updateAdvance(Long ownerId, Long id, UpdateAdvanceRequest request) {
        Advance advance = advanceRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Advance record not found with ID: " + id));

        if (request.getAdvanceDate().isBefore(advance.getEmployee().getJoiningDate())) {
            throw new BadRequestException("Cannot update advance to a date before employee joining date [" +
                    advance.getEmployee().getJoiningDate() + "]. Requested date: [" + request.getAdvanceDate() + "]");
        }

        advance.setAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));
        advance.setAdvanceDate(request.getAdvanceDate());
        advance.setNote(request.getNote());

        Advance saved = advanceRepository.save(advance);
        log.info("Updated advance record ID [{}] for owner [{}]", id, ownerId);

        return AdvanceResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AdvanceResponse getAdvanceById(Long ownerId, Long id) {
        Advance advance = advanceRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Advance record not found with ID: " + id));
        return AdvanceResponse.fromEntity(advance);
    }

    @Override
    @Transactional
    public void deleteAdvance(Long ownerId, Long id) {
        Advance advance = advanceRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Advance record not found with ID: " + id));
        advanceRepository.delete(advance);
        log.info("Deleted advance record ID [{}] for owner [{}]", id, ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public AdvanceRegisterResponse getAdvanceRegister(Long ownerId, YearMonth yearMonth, Pageable pageable) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Pageable safePageable = sanitizePageable(pageable, ALLOWED_EMPLOYEE_SORT_PROPERTIES, "joiningDate", Sort.Direction.ASC);
        Page<Employee> employeePage = employeeRepository.findEligibleEmployees(ownerId, endDate, safePageable);

        List<AdvanceRegisterEmployeeRow> rows = new ArrayList<>();
        if (!employeePage.isEmpty()) {
            List<Long> employeeIds = employeePage.getContent().stream().map(Employee::getId).toList();

            // 1. Fetch advances in target month for employees on this page
            List<Advance> monthAdvances = advanceRepository.findByOwnerIdAndAdvanceDateBetweenAndEmployeeIdIn(
                    ownerId, startDate, endDate, employeeIds
            );
            Map<Long, List<Advance>> advanceByEmp = monthAdvances.stream()
                    .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

            // 2. Fetch running unsettled advance balance for employees on this page
            List<Object[]> runningBalanceRows = advanceRepository.sumRunningAdvanceByEmployeeIds(ownerId, employeeIds);
            Map<Long, BigDecimal> runningBalanceMap = new HashMap<>();
            for (Object[] row : runningBalanceRows) {
                Long empId = (Long) row[0];
                BigDecimal bal = (BigDecimal) row[1];
                runningBalanceMap.put(empId, bal != null ? bal.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            }

            for (Employee emp : employeePage.getContent()) {
                List<Advance> empAdvances = advanceByEmp.getOrDefault(emp.getId(), Collections.emptyList());
                List<AdvanceResponse> advanceDtos = empAdvances.stream().map(AdvanceResponse::fromEntity).toList();

                BigDecimal monthlyTotal = empAdvances.stream()
                        .map(Advance::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal runningBalance = runningBalanceMap.getOrDefault(
                        emp.getId(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                );

                AdvanceRegisterEmployeeRow row = new AdvanceRegisterEmployeeRow();
                row.setEmployeeId(emp.getId());
                row.setEmployeeName(emp.getEmployeeName());
                row.setMobileNumber(emp.getMobileNumber());
                row.setDailyRate(emp.getDailyRate());
                row.setJoiningDate(emp.getJoiningDate());
                row.setMonthlyAdvances(advanceDtos);
                row.setMonthlyTotal(monthlyTotal);
                row.setRunningAdvanceBalance(runningBalance);

                rows.add(row);
            }
        }

        BigDecimal totalMonthDisbursed = advanceRepository.sumAdvancesDisbursedBetween(ownerId, startDate, endDate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalRunningAdvance = advanceRepository.sumTotalRunningAdvanceByOwner(ownerId)
                .setScale(2, RoundingMode.HALF_UP);

        return new AdvanceRegisterResponse(
                yearMonth.toString(),
                (int) employeePage.getTotalElements(),
                totalMonthDisbursed,
                totalRunningAdvance,
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
    @Transactional(readOnly = true)
    public EmployeeAdvanceHistoryResponse getEmployeeAdvanceHistory(Long ownerId, Long employeeId, YearMonth yearMonth, Pageable pageable) {
        Employee employee = employeeRepository.findByIdAndOwnerId(employeeId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        LocalDate startDate = yearMonth != null ? yearMonth.atDay(1) : null;
        LocalDate endDate = yearMonth != null ? yearMonth.atEndOfMonth() : null;

        Pageable safePageable = sanitizePageable(pageable, ALLOWED_ADVANCE_SORT_PROPERTIES, "advanceDate", Sort.Direction.DESC);
        Page<Advance> advancesPage = advanceRepository.findEmployeeAdvances(ownerId, employeeId, startDate, endDate, safePageable);

        BigDecimal periodTotal = BigDecimal.ZERO;
        if (startDate != null && endDate != null) {
            periodTotal = advanceRepository.findByOwnerIdAndEmployeeIdAndAdvanceDateBetween(ownerId, employeeId, startDate, endDate)
                    .stream()
                    .map(Advance::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            periodTotal = advancesPage.getContent().stream()
                    .map(Advance::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        periodTotal = periodTotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal runningBalance = advanceRepository.sumRunningAdvanceByEmployee(ownerId, employeeId)
                .setScale(2, RoundingMode.HALF_UP);

        Page<AdvanceResponse> dtoPage = advancesPage.map(AdvanceResponse::fromEntity);

        return new EmployeeAdvanceHistoryResponse(
                employee.getId(),
                employee.getEmployeeName(),
                employee.getMobileNumber(),
                employee.getDailyRate(),
                employee.getJoiningDate(),
                runningBalance,
                yearMonth != null ? yearMonth.toString() : null,
                periodTotal,
                dtoPage
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyAdvanceSummaryResponse getMonthlyAdvanceSummary(Long ownerId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        BigDecimal disbursed = advanceRepository.sumAdvancesDisbursedBetween(ownerId, startDate, endDate)
                .setScale(2, RoundingMode.HALF_UP);

        long employeesTookAdvance = advanceRepository.countDistinctEmployeesWithAdvanceBetween(ownerId, startDate, endDate);

        long transactions = advanceRepository.findByOwnerIdAndAdvanceDateBetween(ownerId, startDate, endDate).size();

        BigDecimal overdue = advanceRepository.sumOverduePriorAdvances(ownerId, startDate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalOutstanding = advanceRepository.sumTotalRunningAdvanceByOwner(ownerId)
                .setScale(2, RoundingMode.HALF_UP);

        return new MonthlyAdvanceSummaryResponse(
                yearMonth.toString(),
                disbursed,
                employeesTookAdvance,
                transactions,
                overdue,
                totalOutstanding
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OwnerAdvanceSummaryResponse getOwnerAdvanceSummary(Long ownerId) {
        BigDecimal totalOutstanding = advanceRepository.sumTotalRunningAdvanceByOwner(ownerId)
                .setScale(2, RoundingMode.HALF_UP);

        long totalActiveEmployees = employeeRepository.countByOwnerIdAndStatus(ownerId, EmployeeStatus.ACTIVE);
        long employeesWithAdvanceCount = advanceRepository.countDistinctEmployeesWithUnsettledAdvance(ownerId);

        List<Employee> activeEmployees = employeeRepository.findEligibleEmployees(ownerId, LocalDate.now());
        List<EmployeeRunningAdvanceItem> advanceItems = new ArrayList<>();

        if (!activeEmployees.isEmpty()) {
            List<Long> empIds = activeEmployees.stream().map(Employee::getId).toList();
            List<Object[]> balances = advanceRepository.sumRunningAdvanceByEmployeeIds(ownerId, empIds);
            Map<Long, BigDecimal> balanceMap = new HashMap<>();
            for (Object[] row : balances) {
                Long empId = (Long) row[0];
                BigDecimal bal = (BigDecimal) row[1];
                if (bal != null && bal.compareTo(BigDecimal.ZERO) > 0) {
                    balanceMap.put(empId, bal.setScale(2, RoundingMode.HALF_UP));
                }
            }

            for (Employee emp : activeEmployees) {
                BigDecimal bal = balanceMap.get(emp.getId());
                if (bal != null && bal.compareTo(BigDecimal.ZERO) > 0) {
                    advanceItems.add(new EmployeeRunningAdvanceItem(
                            emp.getId(), emp.getEmployeeName(), emp.getMobileNumber(),
                            emp.getDailyRate(), emp.getJoiningDate(), bal
                    ));
                }
            }

            // Sort descending by running advance
            advanceItems.sort((a, b) -> b.getRunningAdvance().compareTo(a.getRunningAdvance()));
        }

        // Take top 10 advance holders
        List<EmployeeRunningAdvanceItem> topAdvanceHolders = advanceItems.stream().limit(10).toList();

        return new OwnerAdvanceSummaryResponse(
                totalOutstanding,
                totalActiveEmployees,
                employeesWithAdvanceCount,
                topAdvanceHolders
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
