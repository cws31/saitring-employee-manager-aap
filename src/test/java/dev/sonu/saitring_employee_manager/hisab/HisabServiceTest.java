package dev.sonu.saitring_employee_manager.hisab;

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
import dev.sonu.saitring_employee_manager.hisab.entity.PaymentMode;
import dev.sonu.saitring_employee_manager.hisab.repository.HisabRepository;
import dev.sonu.saitring_employee_manager.hisab.service.HisabServiceImpl;
import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HisabServiceTest {

    @Mock
    private HisabRepository hisabRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AdvanceRepository advanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private EmployeeRateHistoryRepository employeeRateHistoryRepository;

    private HisabServiceImpl hisabService;

    private Owner owner;
    private Employee emp1;
    private Employee emp2;
    private Employee blockedEmp;

    @BeforeEach
    void setUp() {
        hisabService = new HisabServiceImpl(
                hisabRepository,
                attendanceRepository,
                advanceRepository,
                employeeRepository,
                ownerRepository,
                employeeRateHistoryRepository
        );

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
    @DisplayName("getMonthlyHisabTable: correctly calculates earnings, advance, and final_money")
    void getMonthlyHisabTable_success() {
        YearMonth ym = YearMonth.of(2026, 10);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findEligibleEmployees(eq(1L), eq(LocalDate.of(2026, 10, 31)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(emp1), pageable, 1));

        // Emp1 has 1 FULL day and 1 HALF day
        Attendance a1 = new Attendance(owner, emp1, LocalDate.of(2026, 10, 1), AttendanceStatus.PRESENT, null);
        Attendance a2 = new Attendance(owner, emp1, LocalDate.of(2026, 10, 2), AttendanceStatus.HALF_DAY, null);
        when(attendanceRepository.findByOwnerIdAndAttendanceDateBetweenAndEmployeeIdIn(
                eq(1L), eq(LocalDate.of(2026, 10, 1)), eq(LocalDate.of(2026, 10, 31)), eq(List.of(101L))))
                .thenReturn(List.of(a1, a2));

        // Emp1 has advance of 300.00
        Advance adv = new Advance(owner, emp1, new BigDecimal("300.00"), LocalDate.of(2026, 10, 5), "Site advance");
        when(advanceRepository.findByOwnerIdAndAdvanceDateBetweenAndEmployeeIdIn(
                eq(1L), eq(LocalDate.of(2026, 10, 1)), eq(LocalDate.of(2026, 10, 31)), eq(List.of(101L))))
                .thenReturn(List.of(adv));

        // Running advance
        List<Object[]> runningAdvances = new ArrayList<>();
        runningAdvances.add(new Object[]{101L, new BigDecimal("300.00")});
        when(advanceRepository.sumRunningAdvanceByEmployeeIds(1L, List.of(101L)))
                .thenReturn(runningAdvances);

        when(hisabRepository.findByOwnerIdAndMonthAndEmployeeIdIn(1L, "2026-10", List.of(101L)))
                .thenReturn(Collections.emptyList());

        when(employeeRepository.findEligibleEmployees(1L, LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(emp1));
        when(attendanceRepository.findByOwnerIdAndAttendanceDateBetween(1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(a1, a2));
        when(advanceRepository.sumAdvancesDisbursedBetween(1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(new BigDecimal("300.00"));

        MonthlyHisabTableResponse response = hisabService.getMonthlyHisabTable(1L, ym, pageable);

        assertNotNull(response);
        assertEquals("2026-10", response.getMonth());
        assertEquals(31, response.getDaysInMonth());
        assertEquals(1, response.getTotalEligibleEmployees());
        assertEquals(1, response.getEmployees().size());

        HisabTableRow row = response.getEmployees().get(0);
        assertEquals(101L, row.getEmployeeId());
        assertEquals("Worker One", row.getEmployeeName());
        assertEquals(1, row.getFullDays());
        assertEquals(1, row.getHalfDays());
        assertEquals(1.5, row.getTotalPresenties()); // 1 + 0.5 = 1.5
        assertEquals(new BigDecimal("600.00"), row.getRate());
        assertEquals(new BigDecimal("900.00"), row.getTotalEarnings()); // 1.5 * 600 = 900.00
        assertEquals(new BigDecimal("300.00"), row.getTotalAdvance());
        assertEquals(new BigDecimal("600.00"), row.getFinalMoney()); // 900.00 - 300.00 = 600.00
        assertEquals(HisabStatus.RUNNING, row.getStatus());

        // Org totals
        assertEquals(new BigDecimal("900.00"), response.getTotalOrganizationEarnings());
        assertEquals(new BigDecimal("300.00"), response.getTotalOrganizationAdvance());
        assertEquals(new BigDecimal("600.00"), response.getTotalOrganizationFinalMoney());
    }

    @Test
    @DisplayName("getMonthlyHisabTable: returns settled status if already settled in DB")
    void getMonthlyHisabTable_alreadySettled() {
        YearMonth ym = YearMonth.of(2026, 10);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findEligibleEmployees(eq(1L), eq(LocalDate.of(2026, 10, 31)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(emp1), pageable, 1));

        when(attendanceRepository.findByOwnerIdAndAttendanceDateBetweenAndEmployeeIdIn(anyLong(), any(), any(), anyList()))
                .thenReturn(Collections.emptyList());
        when(advanceRepository.findByOwnerIdAndAdvanceDateBetweenAndEmployeeIdIn(anyLong(), any(), any(), anyList()))
                .thenReturn(Collections.emptyList());
        when(advanceRepository.sumRunningAdvanceByEmployeeIds(anyLong(), anyList()))
                .thenReturn(Collections.emptyList());

        MonthlyHisab settledHisab = new MonthlyHisab(
                owner, emp1, "2026-10", new BigDecimal("600.00"),
                20, 2, 21.0, new BigDecimal("12600.00"),
                new BigDecimal("2000.00"), new BigDecimal("10600.00"),
                HisabStatus.SETTLED, PaymentMode.CASH, Instant.now(), "Settled in full"
        );
        settledHisab.setId(10L);

        when(hisabRepository.findByOwnerIdAndMonthAndEmployeeIdIn(1L, "2026-10", List.of(101L)))
                .thenReturn(List.of(settledHisab));

        MonthlyHisabTableResponse response = hisabService.getMonthlyHisabTable(1L, ym, pageable);

        assertNotNull(response);
        HisabTableRow row = response.getEmployees().get(0);
        assertEquals(HisabStatus.SETTLED, row.getStatus());
        assertEquals(PaymentMode.CASH, row.getPaymentMode());
        assertEquals(new BigDecimal("10600.00"), row.getFinalMoney());
        assertEquals("Settled in full", row.getNote());
        assertNotNull(row.getSettledAt());
    }

    @Test
    @DisplayName("getMonthlyHisabTable: empty eligible employees returns empty rows and zero aggregates")
    void getMonthlyHisabTable_emptyEmployees() {
        YearMonth ym = YearMonth.of(2026, 9);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findEligibleEmployees(eq(1L), eq(LocalDate.of(2026, 9, 30)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        MonthlyHisabTableResponse response = hisabService.getMonthlyHisabTable(1L, ym, pageable);

        assertNotNull(response);
        assertTrue(response.getEmployees().isEmpty());
        assertEquals(0, response.getTotalEligibleEmployees());
        assertEquals(BigDecimal.ZERO.setScale(2), response.getTotalOrganizationEarnings());
        assertEquals(BigDecimal.ZERO.setScale(2), response.getTotalOrganizationAdvance());
        assertEquals(BigDecimal.ZERO.setScale(2), response.getTotalOrganizationFinalMoney());
    }

    @Test
    @DisplayName("settleHisab: successfully creates settled record and marks advances as settled")
    void settleHisab_success() {
        SettleHisabRequest request = new SettleHisabRequest(101L, "2026-10", PaymentMode.UPI, "Paid via PhonePe");

        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(emp1));
        when(hisabRepository.existsByOwnerIdAndEmployeeIdAndMonth(1L, 101L, "2026-10")).thenReturn(false);

        Attendance a1 = new Attendance(owner, emp1, LocalDate.of(2026, 10, 1), AttendanceStatus.PRESENT, null);
        Attendance a2 = new Attendance(owner, emp1, LocalDate.of(2026, 10, 2), AttendanceStatus.PRESENT, null);
        when(attendanceRepository.findByOwnerIdAndEmployeeIdAndAttendanceDateBetween(
                1L, 101L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(a1, a2));

        Advance adv = new Advance(owner, emp1, new BigDecimal("400.00"), LocalDate.of(2026, 10, 10), "Grocery");
        when(advanceRepository.findByOwnerIdAndEmployeeIdAndAdvanceDateBetween(
                1L, 101L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(adv));

        when(hisabRepository.save(any(MonthlyHisab.class))).thenAnswer(invocation -> {
            MonthlyHisab h = invocation.getArgument(0);
            h.setId(50L);
            return h;
        });

        HisabResponse response = hisabService.settleHisab(1L, request);

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals(101L, response.getEmployeeId());
        assertEquals("Worker One", response.getEmployeeName());
        assertEquals(2, response.getFullDays());
        assertEquals(0, response.getHalfDays());
        assertEquals(2.0, response.getEffectiveDays());
        assertEquals(new BigDecimal("600.00"), response.getDailyRate());
        assertEquals(new BigDecimal("1200.00"), response.getTotalEarnings()); // 2 * 600 = 1200
        assertEquals(new BigDecimal("400.00"), response.getTotalAdvance());
        assertEquals(new BigDecimal("800.00"), response.getFinalMoney()); // 1200 - 400 = 800
        assertEquals(HisabStatus.SETTLED, response.getStatus());
        assertEquals(PaymentMode.UPI, response.getPaymentMode());

        verify(advanceRepository).saveAll(anyList());
        verify(hisabRepository).save(any(MonthlyHisab.class));
    }

    @Test
    @DisplayName("settleHisab: throws BadRequestException when hisab already settled for that month")
    void settleHisab_alreadySettled_throwsException() {
        SettleHisabRequest request = new SettleHisabRequest(101L, "2026-10", PaymentMode.CASH, null);

        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(emp1));
        when(hisabRepository.existsByOwnerIdAndEmployeeIdAndMonth(1L, 101L, "2026-10")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> hisabService.settleHisab(1L, request));
        verify(hisabRepository, never()).save(any());
    }

    @Test
    @DisplayName("settleHisab: throws BadRequestException when employee is BLOCKED")
    void settleHisab_blockedEmployee_throwsException() {
        SettleHisabRequest request = new SettleHisabRequest(103L, "2026-10", PaymentMode.CASH, null);

        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(employeeRepository.findByIdAndOwnerId(103L, 1L)).thenReturn(Optional.of(blockedEmp));

        assertThrows(BadRequestException.class, () -> hisabService.settleHisab(1L, request));
    }

    @Test
    @DisplayName("settleHisab: throws BadRequestException when employee joined after target month")
    void settleHisab_joiningDateAfterMonth_throwsException() {
        // emp2 joined 2026-10-15, trying to settle September 2026-09
        SettleHisabRequest request = new SettleHisabRequest(102L, "2026-09", PaymentMode.CASH, null);

        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(employeeRepository.findByIdAndOwnerId(102L, 1L)).thenReturn(Optional.of(emp2));

        assertThrows(BadRequestException.class, () -> hisabService.settleHisab(1L, request));
    }

    @Test
    @DisplayName("settleHisab: throws ResourceNotFoundException when employee not found")
    void settleHisab_employeeNotFound_throwsException() {
        SettleHisabRequest request = new SettleHisabRequest(999L, "2026-10", PaymentMode.CASH, null);

        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(employeeRepository.findByIdAndOwnerId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> hisabService.settleHisab(1L, request));
    }

    @Test
    @DisplayName("getEmployeeHisabHistory: returns paginated history and running advance balance")
    void getEmployeeHisabHistory_success() {
        Pageable pageable = PageRequest.of(0, 12);

        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(emp1));

        MonthlyHisab h1 = new MonthlyHisab(
                owner, emp1, "2026-10", new BigDecimal("600.00"),
                20, 0, 20.0, new BigDecimal("12000.00"),
                new BigDecimal("2000.00"), new BigDecimal("10000.00"),
                HisabStatus.SETTLED, PaymentMode.CASH, Instant.now(), null
        );
        h1.setId(1L);

        when(hisabRepository.findEmployeeHisabHistory(eq(1L), eq(101L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(h1), pageable, 1));
        when(advanceRepository.sumRunningAdvanceByEmployee(1L, 101L))
                .thenReturn(new BigDecimal("1500.00"));

        EmployeeHisabHistoryResponse response = hisabService.getEmployeeHisabHistory(1L, 101L, pageable);

        assertNotNull(response);
        assertEquals(101L, response.getEmployeeId());
        assertEquals("Worker One", response.getEmployeeName());
        assertEquals(new BigDecimal("1500.00"), response.getRunningAdvanceBalance());
        assertEquals(1, response.getSettlements().getTotalElements());
        assertEquals("2026-10", response.getSettlements().getContent().get(0).getMonth());
        assertEquals(new BigDecimal("10000.00"), response.getSettlements().getContent().get(0).getFinalMoney());
    }

    @Test
    @DisplayName("getMonthlyHisabTable: dynamically applies historical wage rates (Sept=600, Oct=650)")
    void getMonthlyHisabTable_rateHistoryApplied() {
        YearMonth septYm = YearMonth.of(2026, 9);
        Pageable pageable = PageRequest.of(0, 10);

        EmployeeRateHistory rateSept = new EmployeeRateHistory(owner, emp1, new BigDecimal("600.00"), LocalDate.of(2026, 9, 1), "Initial");
        EmployeeRateHistory rateOct = new EmployeeRateHistory(owner, emp1, new BigDecimal("650.00"), LocalDate.of(2026, 10, 1), "Increment");

        // When querying September: effective rate on or before 2026-09-30 is 600.00
        when(employeeRepository.findEligibleEmployees(eq(1L), eq(LocalDate.of(2026, 9, 30)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(emp1), pageable, 1));
        when(employeeRateHistoryRepository.findEffectiveRatesForEmployeesOnOrBefore(1L, List.of(101L), LocalDate.of(2026, 9, 30)))
                .thenReturn(List.of(rateSept));

        Attendance a1 = new Attendance(owner, emp1, LocalDate.of(2026, 9, 10), AttendanceStatus.PRESENT, null);
        when(attendanceRepository.findByOwnerIdAndAttendanceDateBetweenAndEmployeeIdIn(
                eq(1L), eq(LocalDate.of(2026, 9, 1)), eq(LocalDate.of(2026, 9, 30)), eq(List.of(101L))))
                .thenReturn(List.of(a1));

        MonthlyHisabTableResponse septResponse = hisabService.getMonthlyHisabTable(1L, septYm, pageable);

        assertNotNull(septResponse);
        HisabTableRow septRow = septResponse.getEmployees().get(0);
        assertEquals(new BigDecimal("600.00"), septRow.getRate());
        assertEquals(new BigDecimal("600.00"), septRow.getTotalEarnings());

        // When querying October: effective rate on or before 2026-10-31 is 650.00
        YearMonth octYm = YearMonth.of(2026, 10);
        when(employeeRepository.findEligibleEmployees(eq(1L), eq(LocalDate.of(2026, 10, 31)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(emp1), pageable, 1));
        when(employeeRateHistoryRepository.findEffectiveRatesForEmployeesOnOrBefore(1L, List.of(101L), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(rateOct, rateSept)); // query returns latest first

        Attendance a2 = new Attendance(owner, emp1, LocalDate.of(2026, 10, 10), AttendanceStatus.PRESENT, null);
        when(attendanceRepository.findByOwnerIdAndAttendanceDateBetweenAndEmployeeIdIn(
                eq(1L), eq(LocalDate.of(2026, 10, 1)), eq(LocalDate.of(2026, 10, 31)), eq(List.of(101L))))
                .thenReturn(List.of(a2));

        MonthlyHisabTableResponse octResponse = hisabService.getMonthlyHisabTable(1L, octYm, pageable);

        assertNotNull(octResponse);
        HisabTableRow octRow = octResponse.getEmployees().get(0);
        assertEquals(new BigDecimal("650.00"), octRow.getRate());
        assertEquals(new BigDecimal("650.00"), octRow.getTotalEarnings());
    }
}
