package dev.sonu.saitring_employee_manager.employee.repository;

import dev.sonu.saitring_employee_manager.employee.entity.Employee;
import dev.sonu.saitring_employee_manager.employee.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByOwnerIdAndMobileNumber(Long ownerId, String mobileNumber);

    boolean existsByOwnerIdAndMobileNumberAndIdNot(Long ownerId, String mobileNumber, Long id);

    @Query("SELECT e FROM Employee e WHERE e.owner.id = :ownerId " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:query IS NULL OR LOWER(e.employeeName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR e.mobileNumber LIKE CONCAT('%', :query, '%'))")
    Page<Employee> searchEmployees(
            @Param("ownerId") Long ownerId,
            @Param("status") EmployeeStatus status,
            @Param("query") String query,
            Pageable pageable
    );

    @Query("SELECT e FROM Employee e WHERE e.owner.id = :ownerId " +
           "AND e.status = dev.sonu.saitring_employee_manager.employee.entity.EmployeeStatus.ACTIVE " +
           "AND e.joiningDate <= :cutoffDate " +
           "ORDER BY e.joiningDate ASC, e.employeeName ASC")
    List<Employee> findEligibleEmployees(
            @Param("ownerId") Long ownerId,
            @Param("cutoffDate") LocalDate cutoffDate
    );

    @Query("SELECT e FROM Employee e WHERE e.owner.id = :ownerId " +
           "AND e.status = dev.sonu.saitring_employee_manager.employee.entity.EmployeeStatus.ACTIVE " +
           "AND e.joiningDate <= :cutoffDate")
    Page<Employee> findEligibleEmployees(
            @Param("ownerId") Long ownerId,
            @Param("cutoffDate") LocalDate cutoffDate,
            Pageable pageable
    );

    @Query("SELECT e FROM Employee e WHERE e.owner.id = :ownerId AND e.id IN :ids")
    List<Employee> findAllByOwnerIdAndIdIn(
            @Param("ownerId") Long ownerId,
            @Param("ids") Collection<Long> ids
    );

    long countByOwnerId(Long ownerId);

    long countByOwnerIdAndStatus(Long ownerId, EmployeeStatus status);
}
