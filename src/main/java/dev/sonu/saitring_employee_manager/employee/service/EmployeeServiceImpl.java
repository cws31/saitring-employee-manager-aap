package dev.sonu.saitring_employee_manager.employee.service;

import dev.sonu.saitring_employee_manager.common.exception.BadRequestException;
import dev.sonu.saitring_employee_manager.common.exception.ConflictException;
import dev.sonu.saitring_employee_manager.common.exception.ResourceNotFoundException;
import dev.sonu.saitring_employee_manager.employee.dto.*;
import dev.sonu.saitring_employee_manager.employee.entity.Employee;
import dev.sonu.saitring_employee_manager.employee.entity.EmployeeRateHistory;
import dev.sonu.saitring_employee_manager.employee.entity.EmployeeStatus;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRateHistoryRepository;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRepository;
import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "employeeName", "mobileNumber", "dailyRate", "joiningDate", "status", "createdAt", "updatedAt"
    );

    private static final Set<String> ALLOWED_RATE_SORT_PROPERTIES = Set.of(
            "id", "dailyRate", "effectiveFrom", "createdAt"
    );

    private final EmployeeRepository employeeRepository;
    private final OwnerRepository ownerRepository;
    private final EmployeeRateHistoryRepository employeeRateHistoryRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               OwnerRepository ownerRepository,
                               EmployeeRateHistoryRepository employeeRateHistoryRepository) {
        this.employeeRepository = employeeRepository;
        this.ownerRepository = ownerRepository;
        this.employeeRateHistoryRepository = employeeRateHistoryRepository;
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(Long ownerId, CreateEmployeeRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner tenant not found with ID: " + ownerId));

        String cleanMobile = request.getMobileNumber().trim();

        // Check mobile uniqueness strictly within this tenant
        if (employeeRepository.existsByOwnerIdAndMobileNumber(ownerId, cleanMobile)) {
            throw new ConflictException("An employee with mobile number " + cleanMobile + " is already registered in your organization.");
        }

        BigDecimal scaledDailyRate = request.getDailyRate().setScale(2, RoundingMode.HALF_UP);
        Employee employee = new Employee(
                owner,
                request.getEmployeeName().trim(),
                cleanMobile,
                scaledDailyRate,
                request.getJoiningDate()
        );

        Employee saved = employeeRepository.save(employee);
        log.info("Created employee [id: {}, name: {}] for owner tenant [{}]", saved.getId(), saved.getEmployeeName(), ownerId);

        // Automatically log initial wage rate effective from joining date
        EmployeeRateHistory initialRate = new EmployeeRateHistory(
                owner,
                saved,
                scaledDailyRate,
                request.getJoiningDate(),
                "Initial wage rate upon joining"
        );
        employeeRateHistoryRepository.save(initialRate);

        return EmployeeResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long ownerId, Long employeeId) {
        Employee employee = employeeRepository.findByIdAndOwnerId(employeeId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));
        return EmployeeResponse.fromEntity(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> listEmployees(Long ownerId, EmployeeStatus status, String query, Pageable pageable) {
        String cleanQuery = StringUtils.hasText(query) ? query.trim() : null;
        Pageable safePageable = sanitizePageable(pageable);
        Page<Employee> page = employeeRepository.searchEmployees(ownerId, status, cleanQuery, safePageable);
        return page.map(EmployeeResponse::fromEntity);
    }

    private Pageable sanitizePageable(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
        }

        List<Sort.Order> validOrders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            if (ALLOWED_SORT_PROPERTIES.contains(property)) {
                validOrders.add(order);
            }
        }

        Sort cleanSort = validOrders.isEmpty()
                ? Sort.by(Sort.Direction.DESC, "id")
                : Sort.by(validOrders);

        int pageNumber = Math.max(0, pageable.getPageNumber());
        int pageSize = pageable.getPageSize() > 0 && pageable.getPageSize() <= 100 ? pageable.getPageSize() : 20;

        return PageRequest.of(pageNumber, pageSize, cleanSort);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long ownerId, Long employeeId, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findByIdAndOwnerId(employeeId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        String cleanMobile = request.getMobileNumber().trim();

        if (employeeRepository.existsByOwnerIdAndMobileNumberAndIdNot(ownerId, cleanMobile, employeeId)) {
            throw new ConflictException("Mobile number " + cleanMobile + " is already used by another employee in your organization.");
        }

        BigDecimal newRate = request.getDailyRate().setScale(2, RoundingMode.HALF_UP);
        boolean rateChanged = newRate.compareTo(employee.getDailyRate()) != 0;

        employee.setEmployeeName(request.getEmployeeName().trim());
        employee.setMobileNumber(cleanMobile);
        employee.setDailyRate(newRate);
        employee.setJoiningDate(request.getJoiningDate());

        Employee updated = employeeRepository.save(employee);

        // If daily rate changed or specific effective date was requested, record in rate history
        if (rateChanged || request.getEffectiveFrom() != null) {
            LocalDate effFrom = request.getEffectiveFrom() != null ? request.getEffectiveFrom() : LocalDate.now();
            EmployeeRateHistory revision = new EmployeeRateHistory(
                    employee.getOwner(),
                    updated,
                    newRate,
                    effFrom,
                    "Updated via employee profile revision"
            );
            employeeRateHistoryRepository.save(revision);
            log.info("Logged wage rate change for employee [{}] to [{}] effective from [{}]", employeeId, newRate, effFrom);
        }

        log.info("Updated employee [id: {}] for owner tenant [{}]", updated.getId(), ownerId);

        return EmployeeResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long ownerId, Long employeeId) {
        Employee employee = employeeRepository.findByIdAndOwnerId(employeeId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        employeeRateHistoryRepository.deleteByOwnerIdAndEmployeeId(ownerId, employeeId);
        employeeRepository.delete(employee);
        log.info("Deleted employee [id: {}] for owner tenant [{}]", employeeId, ownerId);
    }

    @Override
    @Transactional
    public EmployeeResponse blockEmployee(Long ownerId, Long employeeId) {
        Employee employee = employeeRepository.findByIdAndOwnerId(employeeId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        employee.setStatus(EmployeeStatus.BLOCKED);
        Employee updated = employeeRepository.save(employee);
        log.info("Blocked employee [id: {}] for owner tenant [{}]", employeeId, ownerId);

        return EmployeeResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public EmployeeResponse unblockEmployee(Long ownerId, Long employeeId) {
        Employee employee = employeeRepository.findByIdAndOwnerId(employeeId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        employee.setStatus(EmployeeStatus.ACTIVE);
        Employee updated = employeeRepository.save(employee);
        log.info("Unblocked employee [id: {}] for owner tenant [{}]", employeeId, ownerId);

        return EmployeeResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public EmployeeRateResponse addRateRevision(Long ownerId, Long employeeId, RateRevisionRequest request) {
        Employee employee = employeeRepository.findByIdAndOwnerId(employeeId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (request.getEffectiveFrom().isBefore(employee.getJoiningDate())) {
            throw new BadRequestException("Rate effective date [" + request.getEffectiveFrom() +
                    "] cannot be before employee joining date [" + employee.getJoiningDate() + "]");
        }

        BigDecimal scaledRate = request.getDailyRate().setScale(2, RoundingMode.HALF_UP);
        EmployeeRateHistory rateHistory = new EmployeeRateHistory(
                employee.getOwner(),
                employee,
                scaledRate,
                request.getEffectiveFrom(),
                request.getNote() != null ? request.getNote().trim() : null
        );
        EmployeeRateHistory saved = employeeRateHistoryRepository.save(rateHistory);

        // If this rate is effective on or before today, update the current dailyRate on employee entity
        if (!request.getEffectiveFrom().isAfter(LocalDate.now())) {
            employee.setDailyRate(scaledRate);
            employeeRepository.save(employee);
        }

        log.info("Recorded wage rate revision ID [{}] for employee ID [{}] at [{}]/day effective from [{}]",
                saved.getId(), employeeId, scaledRate, request.getEffectiveFrom());

        return EmployeeRateResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeRateResponse> getEmployeeRateHistory(Long ownerId, Long employeeId, Pageable pageable) {
        if (!employeeRepository.existsByIdAndOwnerId(employeeId, ownerId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        Pageable safePageable = sanitizeRatePageable(pageable);
        Page<EmployeeRateHistory> page = employeeRateHistoryRepository.findEmployeeRateHistory(ownerId, employeeId, safePageable);
        return page.map(EmployeeRateResponse::fromEntity);
    }

    private Pageable sanitizeRatePageable(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "effectiveFrom", "id"));
        }

        List<Sort.Order> validOrders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            if (ALLOWED_RATE_SORT_PROPERTIES.contains(order.getProperty())) {
                validOrders.add(order);
            }
        }

        Sort cleanSort = validOrders.isEmpty()
                ? Sort.by(Sort.Direction.DESC, "effectiveFrom", "id")
                : Sort.by(validOrders);

        int pageNumber = Math.max(0, pageable.getPageNumber());
        int pageSize = pageable.getPageSize() > 0 && pageable.getPageSize() <= 100 ? pageable.getPageSize() : 20;

        return PageRequest.of(pageNumber, pageSize, cleanSort);
    }
}
