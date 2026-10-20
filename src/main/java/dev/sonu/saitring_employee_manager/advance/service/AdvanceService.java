package dev.sonu.saitring_employee_manager.advance.service;

import dev.sonu.saitring_employee_manager.advance.dto.*;
import org.springframework.data.domain.Pageable;

import java.time.YearMonth;

public interface AdvanceService {

    AdvanceResponse createAdvance(Long ownerId, CreateAdvanceRequest request);

    AdvanceResponse updateAdvance(Long ownerId, Long id, UpdateAdvanceRequest request);

    AdvanceResponse getAdvanceById(Long ownerId, Long id);

    void deleteAdvance(Long ownerId, Long id);

    AdvanceRegisterResponse getAdvanceRegister(Long ownerId, YearMonth yearMonth, Pageable pageable);

    EmployeeAdvanceHistoryResponse getEmployeeAdvanceHistory(Long ownerId, Long employeeId, YearMonth yearMonth, Pageable pageable);

    MonthlyAdvanceSummaryResponse getMonthlyAdvanceSummary(Long ownerId, YearMonth yearMonth);

    OwnerAdvanceSummaryResponse getOwnerAdvanceSummary(Long ownerId);
}
