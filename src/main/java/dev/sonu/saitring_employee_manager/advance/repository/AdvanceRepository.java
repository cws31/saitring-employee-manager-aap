package dev.sonu.saitring_employee_manager.advance.repository;

import dev.sonu.saitring_employee_manager.advance.entity.Advance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdvanceRepository extends JpaRepository<Advance, Long> {

    Optional<Advance> findByIdAndOwnerId(Long id, Long ownerId);

    @Query("SELECT a FROM Advance a JOIN FETCH a.employee e " +
           "WHERE a.owner.id = :ownerId AND a.employee.id = :employeeId " +
           "AND (:startDate IS NULL OR a.advanceDate >= :startDate) " +
           "AND (:endDate IS NULL OR a.advanceDate <= :endDate)")
    Page<Advance> findEmployeeAdvances(
            @Param("ownerId") Long ownerId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT a FROM Advance a JOIN FETCH a.employee e " +
           "WHERE a.owner.id = :ownerId AND a.employee.id = :employeeId " +
           "AND a.advanceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.advanceDate DESC, a.id DESC")
    List<Advance> findByOwnerIdAndEmployeeIdAndAdvanceDateBetween(
            @Param("ownerId") Long ownerId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT a FROM Advance a JOIN FETCH a.employee e " +
           "WHERE a.owner.id = :ownerId AND a.advanceDate BETWEEN :startDate AND :endDate " +
           "AND a.employee.id IN :employeeIds " +
           "ORDER BY a.advanceDate DESC, a.id DESC")
    List<Advance> findByOwnerIdAndAdvanceDateBetweenAndEmployeeIdIn(
            @Param("ownerId") Long ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("employeeIds") Collection<Long> employeeIds
    );

    @Query("SELECT a FROM Advance a JOIN FETCH a.employee e " +
           "WHERE a.owner.id = :ownerId AND a.advanceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.advanceDate DESC, a.id DESC")
    List<Advance> findByOwnerIdAndAdvanceDateBetween(
            @Param("ownerId") Long ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM Advance a " +
           "WHERE a.owner.id = :ownerId AND a.employee.id = :employeeId AND a.isSettled = false")
    BigDecimal sumRunningAdvanceByEmployee(
            @Param("ownerId") Long ownerId,
            @Param("employeeId") Long employeeId
    );

    @Query("SELECT a.employee.id, COALESCE(SUM(a.amount), 0) FROM Advance a " +
           "WHERE a.owner.id = :ownerId AND a.employee.id IN :employeeIds AND a.isSettled = false " +
           "GROUP BY a.employee.id")
    List<Object[]> sumRunningAdvanceByEmployeeIds(
            @Param("ownerId") Long ownerId,
            @Param("employeeIds") Collection<Long> employeeIds
    );

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM Advance a " +
           "WHERE a.owner.id = :ownerId AND a.isSettled = false")
    BigDecimal sumTotalRunningAdvanceByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM Advance a " +
           "WHERE a.owner.id = :ownerId AND a.advanceDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAdvancesDisbursedBetween(
            @Param("ownerId") Long ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(DISTINCT a.employee.id) FROM Advance a " +
           "WHERE a.owner.id = :ownerId AND a.advanceDate BETWEEN :startDate AND :endDate")
    long countDistinctEmployeesWithAdvanceBetween(
            @Param("ownerId") Long ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(DISTINCT a.employee.id) FROM Advance a " +
           "WHERE a.owner.id = :ownerId AND a.isSettled = false")
    long countDistinctEmployeesWithUnsettledAdvance(@Param("ownerId") Long ownerId);

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM Advance a " +
           "WHERE a.owner.id = :ownerId AND a.advanceDate < :beforeDate AND a.isSettled = false")
    BigDecimal sumOverduePriorAdvances(
            @Param("ownerId") Long ownerId,
            @Param("beforeDate") LocalDate beforeDate
    );
}
