package dev.sonu.saitring_employee_manager.attendance;

import dev.sonu.saitring_employee_manager.attendance.dto.*;
import dev.sonu.saitring_employee_manager.attendance.entity.Attendance;
import dev.sonu.saitring_employee_manager.attendance.entity.AttendanceStatus;
import dev.sonu.saitring_employee_manager.attendance.repository.AttendanceRepository;
import dev.sonu.saitring_employee_manager.attendance.service.AttendanceServiceImpl;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OwnerRepository ownerRepository;

    private AttendanceServiceImpl attendanceService;

    private Owner owner;
    private Employee emp1;
    private Employee emp2;
    private Employee blockedEmp;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceServiceImpl(attendanceRepository, employeeRepository, ownerRepository);

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
    @DisplayName("markAttendance: creates new record when none exists")
    void markAttendance_createsNewRecord() {
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(emp1));
        when(attendanceRepository.findByOwnerIdAndEmployeeIdAndAttendanceDate(1L, 101L, LocalDate.of(2026, 10, 20)))
                .thenReturn(Optional.empty());

        Attendance savedAttendance = new Attendance(owner, emp1, LocalDate.of(2026, 10, 20), AttendanceStatus.PRESENT, "Site A");
        savedAttendance.setId(501L);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(savedAttendance);

        MarkAttendanceRequest request = new MarkAttendanceRequest(101L, LocalDate.of(2026, 10, 20), AttendanceStatus.PRESENT, "Site A");
        AttendanceResponse response = attendanceService.markAttendance(1L, request);

        assertNotNull(response);
        assertEquals(501L, response.getId());
        assertEquals(101L, response.getEmployeeId());
        assertEquals("Worker One", response.getEmployeeName());
        assertEquals(AttendanceStatus.PRESENT, response.getStatus());
        assertEquals("Site A", response.getNote());
    }

    @Test
    @DisplayName("markAttendance: updates existing record on same date (upsert)")
    void markAttendance_updatesExistingRecord() {
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(emp1));

        Attendance existing = new Attendance(owner, emp1, LocalDate.of(2026, 10, 20), AttendanceStatus.PRESENT, "Initial");
        existing.setId(501L);
        when(attendanceRepository.findByOwnerIdAndEmployeeIdAndAttendanceDate(1L, 101L, LocalDate.of(2026, 10, 20)))
                .thenReturn(Optional.of(existing));

        when(attendanceRepository.save(existing)).thenReturn(existing);

        MarkAttendanceRequest request = new MarkAttendanceRequest(101L, LocalDate.of(2026, 10, 20), AttendanceStatus.HALF_DAY, "Left early");
        AttendanceResponse response = attendanceService.markAttendance(1L, request);

        assertEquals(501L, response.getId());
        assertEquals(AttendanceStatus.HALF_DAY, response.getStatus());
        assertEquals("Left early", response.getNote());
    }

    @Test
    @DisplayName("markAttendance: throws BadRequestException if employee is blocked")
    void markAttendance_throwsIfBlocked() {
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(employeeRepository.findByIdAndOwnerId(103L, 1L)).thenReturn(Optional.of(blockedEmp));

        MarkAttendanceRequest request = new MarkAttendanceRequest(103L, LocalDate.of(2026, 10, 20), AttendanceStatus.PRESENT, null);
        assertThrows(BadRequestException.class, () -> attendanceService.markAttendance(1L, request));
    }

    @Test
    @DisplayName("markAttendance: throws BadRequestException if marking before employee joining date")
    void markAttendance_throwsIfBeforeJoiningDate() {
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        // emp2 joined on 2026-10-15
        when(employeeRepository.findByIdAndOwnerId(102L, 1L)).thenReturn(Optional.of(emp2));

        MarkAttendanceRequest request = new MarkAttendanceRequest(102L, LocalDate.of(2026, 10, 10), AttendanceStatus.PRESENT, null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> attendanceService.markAttendance(1L, request));
        assertTrue(ex.getMessage().contains("Cannot mark attendance before employee joining date"));
    }

    @Test
    @DisplayName("updateAttendance: updates status and note")
    void updateAttendance_updatesFields() {
        Attendance attendance = new Attendance(owner, emp1, LocalDate.of(2026, 10, 20), AttendanceStatus.PRESENT, "Old");
        attendance.setId(501L);
        when(attendanceRepository.findByIdAndOwnerId(501L, 1L)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(attendance)).thenReturn(attendance);

        UpdateAttendanceRequest request = new UpdateAttendanceRequest(AttendanceStatus.ABSENT, "Sick leave");
        AttendanceResponse response = attendanceService.updateAttendance(1L, 501L, request);

        assertEquals(AttendanceStatus.ABSENT, response.getStatus());
        assertEquals("Sick leave", response.getNote());
    }

    @Test
    @DisplayName("deleteAttendance: successfully deletes attendance record")
    void deleteAttendance_deletesRecord() {
        Attendance attendance = new Attendance(owner, emp1, LocalDate.of(2026, 10, 20), AttendanceStatus.PRESENT, null);
        attendance.setId(501L);
        when(attendanceRepository.findByIdAndOwnerId(501L, 1L)).thenReturn(Optional.of(attendance));

        attendanceService.deleteAttendance(1L, 501L);
        verify(attendanceRepository, times(1)).delete(attendance);
    }

    @Test
    @DisplayName("bulkMarkAttendance: marks attendance for multiple employees in one transaction")
    void bulkMarkAttendance_processesBatch() {
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(employeeRepository.findAllByOwnerIdAndIdIn(eq(1L), anySet())).thenReturn(List.of(emp1, emp2));

        LocalDate date = LocalDate.of(2026, 10, 20);
        when(attendanceRepository.findByOwnerIdAndAttendanceDateAndEmployeeIdIn(eq(1L), eq(date), anySet()))
                .thenReturn(List.of());

        Attendance saved1 = new Attendance(owner, emp1, date, AttendanceStatus.PRESENT, "Shift 1");
        saved1.setId(1L);
        Attendance saved2 = new Attendance(owner, emp2, date, AttendanceStatus.HALF_DAY, "Shift 2");
        saved2.setId(2L);
        when(attendanceRepository.saveAll(anyList())).thenReturn(List.of(saved1, saved2));

        BulkMarkAttendanceRequest request = new BulkMarkAttendanceRequest(date, List.of(
                new BulkAttendanceItem(101L, AttendanceStatus.PRESENT, "Shift 1"),
                new BulkAttendanceItem(102L, AttendanceStatus.HALF_DAY, "Shift 2")
        ));

        BulkAttendanceResponse response = attendanceService.bulkMarkAttendance(1L, request);

        assertEquals(date, response.getAttendanceDate());
        assertEquals(2, response.getProcessedCount());
        assertEquals(2, response.getRecords().size());
    }

    @Test
    @DisplayName("getMonthlyRegister: displays employees based on joining date (Oct joiner excluded from Sept register)")
    void getMonthlyRegister_enforcesJoiningDateFilter() {
        YearMonth septMonth = YearMonth.of(2026, 9);
        LocalDate septEnd = septMonth.atEndOfMonth();

        // Repository findEligibleEmployees for September only returns emp1 (joined Sep 1), NOT emp2 (joined Oct 15)
        when(employeeRepository.findEligibleEmployees(1L, septEnd)).thenReturn(List.of(emp1));
        when(attendanceRepository.findByOwnerIdAndAttendanceDateBetween(1L, septMonth.atDay(1), septEnd))
                .thenReturn(List.of(
                        new Attendance(owner, emp1, LocalDate.of(2026, 9, 1), AttendanceStatus.PRESENT, null),
                        new Attendance(owner, emp1, LocalDate.of(2026, 9, 2), AttendanceStatus.HALF_DAY, null),
                        new Attendance(owner, emp1, LocalDate.of(2026, 9, 3), AttendanceStatus.ABSENT, null)
                ));

        MonthlyRegisterResponse septRegister = attendanceService.getMonthlyRegister(1L, septMonth);

        assertEquals("2026-09", septRegister.getMonth());
        assertEquals(30, septRegister.getDaysInMonth());
        assertEquals(1, septRegister.getTotalEligibleEmployees());
        assertEquals(1, septRegister.getEmployees().size());

        RegisterEmployeeRow row = septRegister.getEmployees().get(0);
        assertEquals(101L, row.getEmployeeId());
        assertEquals("Worker One", row.getEmployeeName());
        assertEquals(1, row.getPresentCount());
        assertEquals(1, row.getHalfDayCount());
        assertEquals(1, row.getAbsentCount());
        assertEquals(1.5, row.getTotalEffectiveDays()); // 1 + 0.5 * 1
        assertEquals(new BigDecimal("900.00"), row.getEstimatedWages()); // 1.5 * 600.00
        assertEquals(AttendanceStatus.PRESENT, row.getDailyStatus().get(1));
        assertEquals(AttendanceStatus.HALF_DAY, row.getDailyStatus().get(2));
        assertEquals(AttendanceStatus.ABSENT, row.getDailyStatus().get(3));
        assertNull(row.getDailyStatus().get(4)); // unmarked day
    }

    @Test
    @DisplayName("getMonthlyRegister: October register includes both employees who joined on or before Oct 31")
    void getMonthlyRegister_includesEligibleOctoberEmployees() {
        YearMonth octMonth = YearMonth.of(2026, 10);
        LocalDate octEnd = octMonth.atEndOfMonth();

        when(employeeRepository.findEligibleEmployees(1L, octEnd)).thenReturn(List.of(emp1, emp2));
        when(attendanceRepository.findByOwnerIdAndAttendanceDateBetween(1L, octMonth.atDay(1), octEnd))
                .thenReturn(List.of());

        MonthlyRegisterResponse octRegister = attendanceService.getMonthlyRegister(1L, octMonth);

        assertEquals("2026-10", octRegister.getMonth());
        assertEquals(31, octRegister.getDaysInMonth());
        assertEquals(2, octRegister.getTotalEligibleEmployees());
        assertEquals(2, octRegister.getEmployees().size());
    }

    @Test
    @DisplayName("getDailySummary: calculates present, half-day, absent, and unmarked employees")
    void getDailySummary_calculatesCorrectStats() {
        LocalDate date = LocalDate.of(2026, 10, 20);
        when(employeeRepository.findEligibleEmployees(1L, date)).thenReturn(List.of(emp1, emp2));

        Attendance att1 = new Attendance(owner, emp1, date, AttendanceStatus.PRESENT, "Site A");
        att1.setId(10L);
        when(attendanceRepository.findByOwnerIdAndAttendanceDateWithEmployee(1L, date)).thenReturn(List.of(att1));

        DailyAttendanceSummaryResponse summary = attendanceService.getDailySummary(1L, date);

        assertEquals(date, summary.getAttendanceDate());
        assertEquals(2, summary.getTotalEligibleEmployees());
        assertEquals(1, summary.getTotalMarked());
        assertEquals(1, summary.getTotalUnmarked());
        assertEquals(1, summary.getPresentCount());
        assertEquals(0, summary.getHalfDayCount());
        assertEquals(0, summary.getAbsentCount());
        assertEquals(1.0, summary.getTotalEffectiveDays());

        // Check records list
        assertEquals(2, summary.getRecords().size());
        DailyEmployeeAttendanceRecord rec1 = summary.getRecords().stream()
                .filter(r -> r.getEmployeeId().equals(101L)).findFirst().orElseThrow();
        assertTrue(rec1.isMarked());
        assertEquals(AttendanceStatus.PRESENT, rec1.getStatus());

        DailyEmployeeAttendanceRecord rec2 = summary.getRecords().stream()
                .filter(r -> r.getEmployeeId().equals(102L)).findFirst().orElseThrow();
        assertFalse(rec2.isMarked());
        assertNull(rec2.getStatus());
    }

    @Test
    @DisplayName("getEmployeeMonthlySummary: returns correct calculations and history")
    void getEmployeeMonthlySummary_computesHistory() {
        YearMonth month = YearMonth.of(2026, 10);
        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(emp1));

        Attendance att1 = new Attendance(owner, emp1, LocalDate.of(2026, 10, 1), AttendanceStatus.PRESENT, null);
        att1.setId(1L);
        Attendance att2 = new Attendance(owner, emp1, LocalDate.of(2026, 10, 2), AttendanceStatus.HALF_DAY, null);
        att2.setId(2L);
        Attendance att3 = new Attendance(owner, emp1, LocalDate.of(2026, 10, 3), AttendanceStatus.ABSENT, null);
        att3.setId(3L);

        when(attendanceRepository.findByOwnerIdAndEmployeeIdAndAttendanceDateBetween(
                1L, 101L, month.atDay(1), month.atEndOfMonth()
        )).thenReturn(List.of(att1, att2, att3));

        EmployeeMonthlySummaryResponse response = attendanceService.getEmployeeMonthlySummary(1L, 101L, month);

        assertEquals(101L, response.getEmployeeId());
        assertEquals("Worker One", response.getEmployeeName());
        assertEquals(1, response.getTotalPresent());
        assertEquals(1, response.getTotalHalfDay());
        assertEquals(1, response.getTotalAbsent());
        assertEquals(1.5, response.getEffectiveDays());
        assertEquals(new BigDecimal("900.00"), response.getEstimatedSalary());
        assertEquals(3, response.getAttendanceHistory().size());
    }

    @Test
    @DisplayName("getMonthlyRegister with pagination: returns paginated metadata and rows")
    void getMonthlyRegister_withPagination() {
        YearMonth octMonth = YearMonth.of(2026, 10);
        LocalDate octEnd = octMonth.atEndOfMonth();

        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<Employee> page = new PageImpl<>(List.of(emp1), pageRequest, 2);

        when(employeeRepository.findEligibleEmployees(eq(1L), eq(octEnd), any(Pageable.class))).thenReturn(page);
        when(attendanceRepository.findByOwnerIdAndAttendanceDateBetweenAndEmployeeIdIn(
                eq(1L), eq(octMonth.atDay(1)), eq(octEnd), eq(List.of(101L))
        )).thenReturn(List.of());

        MonthlyRegisterResponse response = attendanceService.getMonthlyRegister(1L, octMonth, pageRequest);

        assertEquals("2026-10", response.getMonth());
        assertEquals(0, response.getPageNumber());
        assertEquals(1, response.getPageSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
        assertEquals(1, response.getEmployees().size());
        assertEquals(101L, response.getEmployees().get(0).getEmployeeId());
    }
}
