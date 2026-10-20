package dev.sonu.saitring_employee_manager.hisab.service;

import dev.sonu.saitring_employee_manager.hisab.dto.*;
import org.springframework.data.domain.Pageable;

import java.time.YearMonth;

public interface HisabService {

    MonthlyHisabTableResponse getMonthlyHisabTable(Long ownerId, YearMonth yearMonth, Pageable pageable);

    HisabResponse settleHisab(Long ownerId, SettleHisabRequest request);

    EmployeeHisabHistoryResponse getEmployeeHisabHistory(Long ownerId, Long employeeId, Pageable pageable);
}
