package dev.sonu.saitring_employee_manager.advance;

import dev.sonu.saitring_employee_manager.advance.dto.*;
import dev.sonu.saitring_employee_manager.advance.entity.Advance;
import dev.sonu.saitring_employee_manager.advance.repository.AdvanceRepository;
import dev.sonu.saitring_employee_manager.advance.service.AdvanceServiceImpl;
import dev.sonu.saitring_employee_manager.common.exception.BadRequestException;
import dev.sonu.saitring_employee_manager.common.exception.ResourceNotFoundException;
import dev.sonu.saitring_employee_manager.employee.entity.Employee;
import dev.sonu.saitring_employee_manager.employee.entity.EmployeeStatus;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRepository;
import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvanceServiceTest {

    @Mock
    private AdvanceRepository advanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OwnerRepository ownerRepository;

    private AdvanceServiceImpl advanceService;

    private Owner owner;
    private Employee emp1;
    private Employee emp2;
    private Employee blockedEmp;

    @BeforeEach
    void setUp() {
        advanceService = new AdvanceServiceImpl(advanceRepository, employeeRepository, ownerRepository);

        owner = new Owner("Ramesh", "Ramesh Saitring", "ramesh@saitring.com", "+919876543210", "pwdHash", "Pune", null);
        owner.setId(1L);

        emp1 = new Employee(owner, "Worker One", "+919111111111", new BigDecimal("600.00"), LocalDate.of(2026, 9, 1));
        emp1.setId(101L);

        emp2 = new Employee(owner, "Worker Two", "+919222222222", new BigDecimal("700.00"), LocalDate.of(2026, 10, 15));
        emp2.setId(102L);

        blockedEmp = new Employee(owner, "Blocked Worker", "+919333333333", new BigDecimal("500.00"), LocalDate.of(2026, 8, 1));
        blockedEmp.setId(103L);
        blockedEmp.setStatus(EmployeeStatus.BLOCKED);
    }

    @Test
    @DisplayName("createAdvance: successfully records advance when valid")
    void createAdvance_success() {
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(emp1));

        Advance savedAdvance = new Advance(owner, emp1, new BigDecimal("1000.00"), LocalDate.of(2026, 10, 5), "Personal");
        savedAdvance.setId(1L);
        when(advanceRepository.save(any(Advance.class))).thenReturn(savedAdvance);

        CreateAdvanceRequest request = new CreateAdvanceRequest(101L, new BigDecimal("1000.00"), LocalDate.of(2026, 10, 5), "Personal");
        AdvanceResponse response = advanceService.createAdvance(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(101L, response.getEmployeeId());
        assertEquals("Worker One", response.getEmployeeName());
        assertEquals(new BigDecimal("1000.00"), response.getAmount());
        assertEquals(LocalDate.of(2026, 10, 5), response.getAdvanceDate());
        assertEquals("Personal", response.getNote());
        assertFalse(response.isSettled());
    }

    @Test
    @DisplayName("createAdvance: throws BadRequestException if employee is blocked")
    void createAdvance_throwsIfBlocked() {
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(employeeRepository.findByIdAndOwnerId(103L, 1L)).thenReturn(Optional.of(blockedEmp));

        CreateAdvanceRequest request = new CreateAdvanceRequest(103L, new BigDecimal("500.00"), LocalDate.of(2026, 10, 5), null);
        assertThrows(BadRequestException.class, () -> advanceService.createAdvance(1L, request));
    }

    @Test
    @DisplayName("createAdvance: throws BadRequestException if advance date is before employee joining date")
    void createAdvance_throwsIfBeforeJoiningDate() {
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        // emp2 joined on 2026-10-15
        when(employeeRepository.findByIdAndOwnerId(102L, 1L)).thenReturn(Optional.of(emp2));

        CreateAdvanceRequest request = new CreateAdvanceRequest(102L, new BigDecimal("500.00"), LocalDate.of(2026, 10, 10), null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> advanceService.createAdvance(1L, request));
        assertTrue(ex.getMessage().contains("Cannot disburse advance before employee joining date"));
    }

    @Test
    @DisplayName("updateAdvance: updates amount, date, and note")
    void updateAdvance_success() {
        Advance advance = new Advance(owner, emp1, new BigDecimal("1000.00"), LocalDate.of(2026, 10, 5), "Old");
        advance.setId(10L);
        when(advanceRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(advance));
        when(advanceRepository.save(advance)).thenReturn(advance);

        UpdateAdvanceRequest request = new UpdateAdvanceRequest(new BigDecimal("1200.00"), LocalDate.of(2026, 10, 6), "Corrected amount");
        AdvanceResponse response = advanceService.updateAdvance(1L, 10L, request);

        assertEquals(new BigDecimal("1200.00"), response.getAmount());
        assertEquals(LocalDate.of(2026, 10, 6), response.getAdvanceDate());
        assertEquals("Corrected amount", response.getNote());
    }

    @Test
    @DisplayName("updateAdvance: rejects date before employee joining date")
    void updateAdvance_rejectsDateBeforeJoiningDate() {
        Advance advance = new Advance(owner, emp2, new BigDecimal("500.00"), LocalDate.of(2026, 10, 16), "Site");
        advance.setId(11L);
        when(advanceRepository.findByIdAndOwnerId(11L, 1L)).thenReturn(Optional.of(advance));

        UpdateAdvanceRequest request = new UpdateAdvanceRequest(new BigDecimal("500.00"), LocalDate.of(2026, 10, 10), "Invalid");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> advanceService.updateAdvance(1L, 11L, request));
        assertTrue(ex.getMessage().contains("Cannot update advance to a date before employee joining date"));
    }

    @Test
    @DisplayName("deleteAdvance: successfully removes record")
    void deleteAdvance_success() {
        Advance advance = new Advance(owner, emp1, new BigDecimal("500.00"), LocalDate.of(2026, 10, 5), null);
        advance.setId(12L);
        when(advanceRepository.findByIdAndOwnerId(12L, 1L)).thenReturn(Optional.of(advance));

        advanceService.deleteAdvance(1L, 12L);
        verify(advanceRepository, times(1)).delete(advance);
    }

    @Test
    @DisplayName("getAdvanceRegister: enforces joining date rule and returns pagination metadata")
    void getAdvanceRegister_paginationAndJoiningDate() {
        YearMonth septMonth = YearMonth.of(2026, 9);
        LocalDate septEnd = septMonth.atEndOfMonth();

        PageRequest pageRequest = PageRequest.of(0, 10);
        // Only emp1 joined in September; emp2 (joined Oct) is excluded by repository
        Page<Employee> employeePage = new PageImpl<>(List.of(emp1), pageRequest, 1);

        when(employeeRepository.findEligibleEmployees(eq(1L), eq(septEnd), any(Pageable.class))).thenReturn(employeePage);

        Advance adv1 = new Advance(owner, emp1, new BigDecimal("800.00"), LocalDate.of(2026, 9, 10), "Advance 1");
        adv1.setId(1L);

        when(advanceRepository.findByOwnerIdAndAdvanceDateBetweenAndEmployeeIdIn(
                eq(1L), eq(septMonth.atDay(1)), eq(septEnd), eq(List.of(101L))
        )).thenReturn(List.of(adv1));

        when(advanceRepository.sumRunningAdvanceByEmployeeIds(eq(1L), eq(List.of(101L))))
                .thenReturn(Collections.singletonList(new Object[]{101L, new BigDecimal("800.00")}));

        when(advanceRepository.sumAdvancesDisbursedBetween(eq(1L), eq(septMonth.atDay(1)), eq(septEnd)))
                .thenReturn(new BigDecimal("800.00"));

        when(advanceRepository.sumTotalRunningAdvanceByOwner(1L))
                .thenReturn(new BigDecimal("800.00"));

        AdvanceRegisterResponse response = advanceService.getAdvanceRegister(1L, septMonth, pageRequest);

        assertEquals("2026-09", response.getMonth());
        assertEquals(1, response.getTotalEligibleEmployees());
        assertEquals(new BigDecimal("800.00"), response.getTotalMonthlyAdvanceDisbursed());
        assertEquals(new BigDecimal("800.00"), response.getTotalRunningAdvance());
        assertEquals(0, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());

        assertEquals(1, response.getEmployees().size());
        AdvanceRegisterEmployeeRow row = response.getEmployees().get(0);
        assertEquals(101L, row.getEmployeeId());
        assertEquals("Worker One", row.getEmployeeName());
        assertEquals(new BigDecimal("800.00"), row.getMonthlyTotal());
        assertEquals(new BigDecimal("800.00"), row.getRunningAdvanceBalance());
        assertEquals(1, row.getMonthlyAdvances().size());
    }

    @Test
    @DisplayName("getEmployeeAdvanceHistory: returns history with period total and running balance")
    void getEmployeeAdvanceHistory_success() {
        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(emp1));

        YearMonth month = YearMonth.of(2026, 10);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Advance adv1 = new Advance(owner, emp1, new BigDecimal("500.00"), LocalDate.of(2026, 10, 2), "Adv 1");
        adv1.setId(1L);
        Advance adv2 = new Advance(owner, emp1, new BigDecimal("700.00"), LocalDate.of(2026, 10, 15), "Adv 2");
        adv2.setId(2L);

        Page<Advance> page = new PageImpl<>(List.of(adv2, adv1), pageRequest, 2);

        when(advanceRepository.findEmployeeAdvances(
                eq(1L), eq(101L), eq(month.atDay(1)), eq(month.atEndOfMonth()), any(Pageable.class)
        )).thenReturn(page);

        when(advanceRepository.findByOwnerIdAndEmployeeIdAndAdvanceDateBetween(
                1L, 101L, month.atDay(1), month.atEndOfMonth()
        )).thenReturn(List.of(adv2, adv1));

        when(advanceRepository.sumRunningAdvanceByEmployee(1L, 101L))
                .thenReturn(new BigDecimal("1200.00"));

        EmployeeAdvanceHistoryResponse response = advanceService.getEmployeeAdvanceHistory(1L, 101L, month, pageRequest);

        assertEquals(101L, response.getEmployeeId());
        assertEquals("Worker One", response.getEmployeeName());
        assertEquals("2026-10", response.getMonth());
        assertEquals(new BigDecimal("1200.00"), response.getPeriodTotal());
        assertEquals(new BigDecimal("1200.00"), response.getRunningAdvanceBalance());
        assertEquals(2, response.getAdvances().getTotalElements());
    }

    @Test
    @DisplayName("getMonthlyAdvanceSummary: returns monthly disbursements, employee count, and overdue")
    void getMonthlyAdvanceSummary_success() {
        YearMonth month = YearMonth.of(2026, 10);
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        when(advanceRepository.sumAdvancesDisbursedBetween(1L, start, end)).thenReturn(new BigDecimal("5000.00"));
        when(advanceRepository.countDistinctEmployeesWithAdvanceBetween(1L, start, end)).thenReturn(3L);
        when(advanceRepository.findByOwnerIdAndAdvanceDateBetween(1L, start, end))
                .thenReturn(List.of(new Advance(), new Advance(), new Advance(), new Advance()));
        when(advanceRepository.sumOverduePriorAdvances(1L, start)).thenReturn(new BigDecimal("2000.00"));
        when(advanceRepository.sumTotalRunningAdvanceByOwner(1L)).thenReturn(new BigDecimal("7000.00"));

        MonthlyAdvanceSummaryResponse response = advanceService.getMonthlyAdvanceSummary(1L, month);

        assertEquals("2026-10", response.getMonth());
        assertEquals(new BigDecimal("5000.00"), response.getTotalAdvanceDisbursed());
        assertEquals(3L, response.getTotalEmployeesTookAdvance());
        assertEquals(4L, response.getTotalTransactions());
        assertEquals(new BigDecimal("2000.00"), response.getOverduePriorAdvance());
        assertEquals(new BigDecimal("7000.00"), response.getTotalOutstandingRunningAdvance());
    }

    @Test
    @DisplayName("getOwnerAdvanceSummary: returns overall outstanding running advance and top advance holders")
    void getOwnerAdvanceSummary_success() {
        when(advanceRepository.sumTotalRunningAdvanceByOwner(1L)).thenReturn(new BigDecimal("6000.00"));
        when(employeeRepository.countByOwnerIdAndStatus(1L, EmployeeStatus.ACTIVE)).thenReturn(5L);
        when(advanceRepository.countDistinctEmployeesWithUnsettledAdvance(1L)).thenReturn(2L);

        when(employeeRepository.findEligibleEmployees(eq(1L), any(LocalDate.class))).thenReturn(List.of(emp1, emp2));

        when(advanceRepository.sumRunningAdvanceByEmployeeIds(eq(1L), anyCollection()))
                .thenReturn(List.of(
                        new Object[]{101L, new BigDecimal("4000.00")},
                        new Object[]{102L, new BigDecimal("2000.00")}
                ));

        OwnerAdvanceSummaryResponse response = advanceService.getOwnerAdvanceSummary(1L);

        assertEquals(new BigDecimal("6000.00"), response.getTotalOutstandingRunningAdvance());
        assertEquals(5L, response.getTotalActiveEmployees());
        assertEquals(2L, response.getEmployeesWithAdvanceCount());
        assertEquals(2, response.getTopAdvanceHolders().size());

        EmployeeRunningAdvanceItem top1 = response.getTopAdvanceHolders().get(0);
        assertEquals(101L, top1.getEmployeeId());
        assertEquals(new BigDecimal("4000.00"), top1.getRunningAdvance());
    }
}
