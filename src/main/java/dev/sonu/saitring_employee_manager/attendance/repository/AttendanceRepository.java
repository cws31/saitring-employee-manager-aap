package dev.sonu.saitring_employee_manager.attendance.repository;

import dev.sonu.saitring_employee_manager.attendance.entity.Attendance;
import dev.sonu.saitring_employee_manager.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<Attendance> findByOwnerIdAndEmployeeIdAndAttendanceDate(Long ownerId, Long employeeId, LocalDate attendanceDate);

    List<Attendance> findByOwnerIdAndAttendanceDate(Long ownerId, LocalDate attendanceDate);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e " +
           "WHERE a.owner.id = :ownerId AND a.attendanceDate = :attendanceDate")
    List<Attendance> findByOwnerIdAndAttendanceDateWithEmployee(
            @Param("ownerId") Long ownerId,
            @Param("attendanceDate") LocalDate attendanceDate
    );

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e " +
           "WHERE a.owner.id = :ownerId AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.attendanceDate ASC")
    List<Attendance> findByOwnerIdAndAttendanceDateBetween(
            @Param("ownerId") Long ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT a FROM Attendance a " +
           "WHERE a.owner.id = :ownerId AND a.employee.id = :employeeId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.attendanceDate ASC")
    List<Attendance> findByOwnerIdAndEmployeeIdAndAttendanceDateBetween(
            @Param("ownerId") Long ownerId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<Attendance> findByOwnerIdAndAttendanceDateAndEmployeeIdIn(
            Long ownerId,
            LocalDate attendanceDate,
            Collection<Long> employeeIds
    );

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e " +
           "WHERE a.owner.id = :ownerId AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND a.employee.id IN :employeeIds " +
           "ORDER BY a.attendanceDate ASC")
    List<Attendance> findByOwnerIdAndAttendanceDateBetweenAndEmployeeIdIn(
            @Param("ownerId") Long ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("employeeIds") Collection<Long> employeeIds
    );

    long countByOwnerIdAndAttendanceDateAndStatus(Long ownerId, LocalDate attendanceDate, AttendanceStatus status);

    long countByOwnerIdAndAttendanceDate(Long ownerId, LocalDate attendanceDate);
}
