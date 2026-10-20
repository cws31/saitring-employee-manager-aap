package dev.sonu.saitring_employee_manager.employee.repository;

import dev.sonu.saitring_employee_manager.employee.entity.EmployeeRateHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface EmployeeRateHistoryRepository extends JpaRepository<EmployeeRateHistory, Long> {

    @Query("SELECT r FROM EmployeeRateHistory r " +
           "WHERE r.owner.id = :ownerId AND r.employee.id = :employeeId AND r.effectiveFrom <= :targetDate " +
           "ORDER BY r.effectiveFrom DESC, r.id DESC")
    List<EmployeeRateHistory> findEffectiveRatesOnOrBefore(
            @Param("ownerId") Long ownerId,
            @Param("employeeId") Long employeeId,
            @Param("targetDate") LocalDate targetDate,
            Pageable pageable
    );

    @Query("SELECT r FROM EmployeeRateHistory r " +
           "WHERE r.owner.id = :ownerId AND r.employee.id IN :employeeIds AND r.effectiveFrom <= :targetDate " +
           "ORDER BY r.effectiveFrom DESC, r.id DESC")
    List<EmployeeRateHistory> findEffectiveRatesForEmployeesOnOrBefore(
            @Param("ownerId") Long ownerId,
            @Param("employeeIds") Collection<Long> employeeIds,
            @Param("targetDate") LocalDate targetDate
    );

    @Query("SELECT r FROM EmployeeRateHistory r " +
           "WHERE r.owner.id = :ownerId AND r.employee.id = :employeeId " +
           "ORDER BY r.effectiveFrom DESC, r.id DESC")
    Page<EmployeeRateHistory> findEmployeeRateHistory(
            @Param("ownerId") Long ownerId,
            @Param("employeeId") Long employeeId,
            Pageable pageable
    );

    List<EmployeeRateHistory> findByOwnerIdAndEmployeeId(Long ownerId, Long employeeId);

    void deleteByOwnerIdAndEmployeeId(Long ownerId, Long employeeId);
}
