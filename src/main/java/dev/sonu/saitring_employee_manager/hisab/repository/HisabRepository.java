package dev.sonu.saitring_employee_manager.hisab.repository;

import dev.sonu.saitring_employee_manager.hisab.entity.MonthlyHisab;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface HisabRepository extends JpaRepository<MonthlyHisab, Long> {

    Optional<MonthlyHisab> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<MonthlyHisab> findByOwnerIdAndEmployeeIdAndMonth(Long ownerId, Long employeeId, String month);

    List<MonthlyHisab> findByOwnerIdAndMonth(Long ownerId, String month);

    List<MonthlyHisab> findByOwnerIdAndMonthAndEmployeeIdIn(Long ownerId, String month, Collection<Long> employeeIds);

    @Query("SELECT h FROM MonthlyHisab h JOIN FETCH h.employee e " +
           "WHERE h.owner.id = :ownerId AND h.employee.id = :employeeId " +
           "ORDER BY h.month DESC")
    Page<MonthlyHisab> findEmployeeHisabHistory(
            @Param("ownerId") Long ownerId,
            @Param("employeeId") Long employeeId,
            Pageable pageable
    );

    boolean existsByOwnerIdAndEmployeeIdAndMonth(Long ownerId, Long employeeId, String month);
}
