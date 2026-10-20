package dev.sonu.saitring_employee_manager.attendance.service;

import dev.sonu.saitring_employee_manager.attendance.dto.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;

public interface AttendanceService {

    AttendanceResponse markAttendance(Long ownerId, MarkAttendanceRequest request);

    AttendanceResponse updateAttendance(Long ownerId, Long id, UpdateAttendanceRequest request);

    AttendanceResponse getAttendanceById(Long ownerId, Long id);

    void deleteAttendance(Long ownerId, Long id);

    BulkAttendanceResponse bulkMarkAttendance(Long ownerId, BulkMarkAttendanceRequest request);

    MonthlyRegisterResponse getMonthlyRegister(Long ownerId, YearMonth yearMonth, Pageable pageable);

    default MonthlyRegisterResponse getMonthlyRegister(Long ownerId, YearMonth yearMonth) {
        return getMonthlyRegister(ownerId, yearMonth, Pageable.unpaged());
    }

    DailyAttendanceSummaryResponse getDailySummary(Long ownerId, LocalDate date);

    MonthlyAttendanceSummaryResponse getMonthlySummary(Long ownerId, YearMonth yearMonth);

    EmployeeMonthlySummaryResponse getEmployeeMonthlySummary(Long ownerId, Long employeeId, YearMonth yearMonth);
}
