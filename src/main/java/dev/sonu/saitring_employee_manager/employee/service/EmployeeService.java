package dev.sonu.saitring_employee_manager.employee.service;

import dev.sonu.saitring_employee_manager.employee.dto.CreateEmployeeRequest;
import dev.sonu.saitring_employee_manager.employee.dto.EmployeeResponse;
import dev.sonu.saitring_employee_manager.employee.dto.UpdateEmployeeRequest;
import dev.sonu.saitring_employee_manager.employee.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    EmployeeResponse createEmployee(Long ownerId, CreateEmployeeRequest request);

    EmployeeResponse getEmployeeById(Long ownerId, Long employeeId);

    Page<EmployeeResponse> listEmployees(Long ownerId, EmployeeStatus status, String query, Pageable pageable);

    EmployeeResponse updateEmployee(Long ownerId, Long employeeId, UpdateEmployeeRequest request);

    void deleteEmployee(Long ownerId, Long employeeId);

    EmployeeResponse blockEmployee(Long ownerId, Long employeeId);

    EmployeeResponse unblockEmployee(Long ownerId, Long employeeId);
}
