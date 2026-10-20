package dev.sonu.saitring_employee_manager.employee;

import dev.sonu.saitring_employee_manager.common.exception.ConflictException;
import dev.sonu.saitring_employee_manager.common.exception.ResourceNotFoundException;
import dev.sonu.saitring_employee_manager.employee.dto.CreateEmployeeRequest;
import dev.sonu.saitring_employee_manager.employee.dto.EmployeeResponse;
import dev.sonu.saitring_employee_manager.employee.dto.UpdateEmployeeRequest;
import dev.sonu.saitring_employee_manager.employee.entity.Employee;
import dev.sonu.saitring_employee_manager.employee.entity.EmployeeStatus;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRepository;
import dev.sonu.saitring_employee_manager.employee.service.EmployeeServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OwnerRepository ownerRepository;

    private EmployeeServiceImpl employeeService;

    private Owner ownerA;
    private Owner ownerB;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(employeeRepository, ownerRepository);

        ownerA = new Owner("Owner A", "Org A", "a@org.com", "+919800000001", "hash", "Address A", null);
        ownerA.setId(1L);

        ownerB = new Owner("Owner B", "Org B", "b@org.com", "+919800000002", "hash", "Address B", null);
        ownerB.setId(2L);
    }

    @Test
    @DisplayName("Create employee succeeds for valid owner and unique mobile inside tenant")
    void createEmployeeSuccess() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Santosh Patil",
                "+919876543210",
                new BigDecimal("750.00"),
                LocalDate.of(2026, 1, 10)
        );

        when(ownerRepository.findById(1L)).thenReturn(Optional.of(ownerA));
        when(employeeRepository.existsByOwnerIdAndMobileNumber(1L, "+919876543210")).thenReturn(false);

        Employee saved = new Employee(ownerA, "Santosh Patil", "+919876543210", new BigDecimal("750.00"), LocalDate.of(2026, 1, 10));
        saved.setId(101L);
        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);

        EmployeeResponse response = employeeService.createEmployee(1L, request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(1L, response.getOwnerId());
        assertEquals("Santosh Patil", response.getEmployeeName());
        assertEquals(new BigDecimal("750.00"), response.getDailyRate());
        assertEquals(EmployeeStatus.ACTIVE, response.getStatus());

        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create employee throws ConflictException when mobile number is already registered for this owner")
    void createEmployeeRejectsDuplicateMobileWithinTenant() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Santosh Patil",
                "+919876543210",
                new BigDecimal("750.00"),
                LocalDate.of(2026, 1, 10)
        );

        when(ownerRepository.findById(1L)).thenReturn(Optional.of(ownerA));
        when(employeeRepository.existsByOwnerIdAndMobileNumber(1L, "+919876543210")).thenReturn(true);

        assertThrows(ConflictException.class, () -> employeeService.createEmployee(1L, request));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get employee by ID succeeds when owned by the requesting owner")
    void getEmployeeByIdSuccess() {
        Employee employee = new Employee(ownerA, "Santosh", "+919876543210", new BigDecimal("750.00"), LocalDate.now());
        employee.setId(101L);

        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(employee));

        EmployeeResponse response = employeeService.getEmployeeById(1L, 101L);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Santosh", response.getEmployeeName());
    }

    @Test
    @DisplayName("Get employee by ID throws ResourceNotFoundException when employee belongs to another owner")
    void getEmployeeByIdCrossTenantIsolation() {
        when(employeeRepository.findByIdAndOwnerId(101L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(2L, 101L));
    }

    @Test
    @DisplayName("Update employee updates fields successfully")
    void updateEmployeeSuccess() {
        Employee employee = new Employee(ownerA, "Santosh", "+919876543210", new BigDecimal("750.00"), LocalDate.now());
        employee.setId(101L);

        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByOwnerIdAndMobileNumberAndIdNot(1L, "+919876543299", 101L)).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                "Santosh R. Patil",
                "+919876543299",
                new BigDecimal("800.00"),
                LocalDate.of(2026, 2, 1)
        );

        EmployeeResponse response = employeeService.updateEmployee(1L, 101L, request);

        assertNotNull(response);
        assertEquals("Santosh R. Patil", response.getEmployeeName());
        assertEquals("+919876543299", response.getMobileNumber());
        assertEquals(new BigDecimal("800.00"), response.getDailyRate());
    }

    @Test
    @DisplayName("Update employee throws ConflictException when new mobile number is taken inside tenant")
    void updateEmployeeRejectsDuplicateMobileWithinTenant() {
        Employee employee = new Employee(ownerA, "Santosh", "+919876543210", new BigDecimal("750.00"), LocalDate.now());
        employee.setId(101L);

        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByOwnerIdAndMobileNumberAndIdNot(1L, "+919876543999", 101L)).thenReturn(true);

        UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                "Santosh",
                "+919876543999",
                new BigDecimal("750.00"),
                LocalDate.now()
        );

        assertThrows(ConflictException.class, () -> employeeService.updateEmployee(1L, 101L, request));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update employee throws ResourceNotFoundException when employee belongs to another owner")
    void updateEmployeeCrossTenantIsolation() {
        when(employeeRepository.findByIdAndOwnerId(101L, 2L)).thenReturn(Optional.empty());

        UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                "Hacked", "+919876543999", new BigDecimal("999.00"), LocalDate.now()
        );

        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(2L, 101L, request));
    }

    @Test
    @DisplayName("Delete employee successfully deletes from repository")
    void deleteEmployeeSuccess() {
        Employee employee = new Employee(ownerA, "Santosh", "+919876543210", new BigDecimal("750.00"), LocalDate.now());
        employee.setId(101L);

        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(1L, 101L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    @DisplayName("Delete employee throws ResourceNotFoundException when employee belongs to another owner")
    void deleteEmployeeCrossTenantIsolation() {
        when(employeeRepository.findByIdAndOwnerId(101L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(2L, 101L));
        verify(employeeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Block employee transitions status to BLOCKED")
    void blockEmployeeSuccess() {
        Employee employee = new Employee(ownerA, "Santosh", "+919876543210", new BigDecimal("750.00"), LocalDate.now());
        employee.setId(101L);
        employee.setStatus(EmployeeStatus.ACTIVE);

        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse response = employeeService.blockEmployee(1L, 101L);

        assertNotNull(response);
        assertEquals(EmployeeStatus.BLOCKED, response.getStatus());
    }

    @Test
    @DisplayName("Unblock employee transitions status to ACTIVE")
    void unblockEmployeeSuccess() {
        Employee employee = new Employee(ownerA, "Santosh", "+919876543210", new BigDecimal("750.00"), LocalDate.now());
        employee.setId(101L);
        employee.setStatus(EmployeeStatus.BLOCKED);

        when(employeeRepository.findByIdAndOwnerId(101L, 1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse response = employeeService.unblockEmployee(1L, 101L);

        assertNotNull(response);
        assertEquals(EmployeeStatus.ACTIVE, response.getStatus());
    }

    @Test
    @DisplayName("List employees returns page of employees for owner")
    void listEmployeesSuccess() {
        Employee e1 = new Employee(ownerA, "Santosh", "+919876543210", new BigDecimal("750.00"), LocalDate.now());
        e1.setId(101L);
        Employee e2 = new Employee(ownerA, "Vijay", "+919876543211", new BigDecimal("800.00"), LocalDate.now());
        e2.setId(102L);

        Pageable pageable = PageRequest.of(0, 20);
        when(employeeRepository.searchEmployees(eq(1L), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(e1, e2), pageable, 2));

        Page<EmployeeResponse> page = employeeService.listEmployees(1L, null, null, pageable);

        assertNotNull(page);
        assertEquals(2, page.getTotalElements());
        assertEquals("Santosh", page.getContent().get(0).getEmployeeName());
        assertEquals("Vijay", page.getContent().get(1).getEmployeeName());
    }
}
