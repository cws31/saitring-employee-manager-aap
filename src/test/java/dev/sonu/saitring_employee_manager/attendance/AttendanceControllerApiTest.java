package dev.sonu.saitring_employee_manager.attendance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sonu.saitring_employee_manager.advance.repository.AdvanceRepository;
import dev.sonu.saitring_employee_manager.attendance.dto.BulkAttendanceItem;
import dev.sonu.saitring_employee_manager.attendance.dto.BulkMarkAttendanceRequest;
import dev.sonu.saitring_employee_manager.attendance.dto.MarkAttendanceRequest;
import dev.sonu.saitring_employee_manager.attendance.dto.UpdateAttendanceRequest;
import dev.sonu.saitring_employee_manager.attendance.entity.AttendanceStatus;
import dev.sonu.saitring_employee_manager.attendance.repository.AttendanceRepository;
import dev.sonu.saitring_employee_manager.auth.dto.LoginRequest;
import dev.sonu.saitring_employee_manager.auth.dto.RegisterRequest;
import dev.sonu.saitring_employee_manager.auth.dto.VerifyOtpRequest;
import dev.sonu.saitring_employee_manager.auth.service.OtpChallengeData;
import dev.sonu.saitring_employee_manager.auth.service.OtpStorageService;
import dev.sonu.saitring_employee_manager.employee.dto.CreateEmployeeRequest;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRateHistoryRepository;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRepository;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import org.junit.jupiter.api.AfterEach;
import dev.sonu.saitring_employee_manager.hisab.repository.HisabRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AttendanceControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HisabRepository hisabRepository;

    @Autowired
    private AdvanceRepository advanceRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRateHistoryRepository employeeRateHistoryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private OtpStorageService otpStorageService;

    private String jwtTokenOwner1;
    private String jwtTokenOwner2;

    @BeforeEach
    void setupData() throws Exception {
        hisabRepository.deleteAll();
        advanceRepository.deleteAll();
        attendanceRepository.deleteAll();
        employeeRateHistoryRepository.deleteAll();
        employeeRepository.deleteAll();
        ownerRepository.deleteAll();

        jwtTokenOwner1 = registerAndLogin(
                "Ramesh Builder",
                "Ramesh Scaffolding",
                "ramesh.owner1@saitring.com",
                "+919811111111",
                "Pass@123"
        );

        jwtTokenOwner2 = registerAndLogin(
                "Suresh Builder",
                "Suresh Formwork",
                "suresh.owner2@saitring.com",
                "+919822222222",
                "Pass@123"
        );
    }

    @AfterEach
    void tearDown() {
        hisabRepository.deleteAll();
        advanceRepository.deleteAll();
        attendanceRepository.deleteAll();
        employeeRateHistoryRepository.deleteAll();
        employeeRepository.deleteAll();
        ownerRepository.deleteAll();
    }

    private String registerAndLogin(String ownerName, String orgName, String email, String mobile, String password) throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                ownerName, orgName, email, mobile, password, "Sample Address"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest(email, password);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String challengeToken = loginNode.get("challengeToken").asText();

        OtpChallengeData challenge = otpStorageService.getChallenge(challengeToken).orElseThrow();
        VerifyOtpRequest verifyReq = new VerifyOtpRequest(challengeToken, challenge.getOtp());

        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode verifyNode = objectMapper.readTree(verifyResult.getResponse().getContentAsString());
        return verifyNode.get("accessToken").asText();
    }

    private Long createEmployee(String token, String name, String mobile, BigDecimal dailyRate, LocalDate joiningDate) throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest(name, mobile, dailyRate, joiningDate);
        MvcResult result = mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    @Test
    @DisplayName("Single Attendance CRUD: Mark -> Get -> Update -> Delete")
    void singleAttendanceCrudFlow() throws Exception {
        Long empId = createEmployee(jwtTokenOwner1, "Raju Shinde", "+919900000001", new BigDecimal("600.00"), LocalDate.of(2026, 10, 1));

        // 1. Mark attendance
        LocalDate attDate = LocalDate.of(2026, 10, 5);
        MarkAttendanceRequest markReq = new MarkAttendanceRequest(empId, attDate, AttendanceStatus.PRESENT, "Full day work");

        MvcResult markResult = mockMvc.perform(post("/api/v1/attendance")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(markReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.employeeId", is(empId.intValue())))
                .andExpect(jsonPath("$.status", is("PRESENT")))
                .andExpect(jsonPath("$.note", is("Full day work")))
                .andReturn();

        Long attId = objectMapper.readTree(markResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Get by ID
        mockMvc.perform(get("/api/v1/attendance/" + attId)
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(attId.intValue())))
                .andExpect(jsonPath("$.status", is("PRESENT")));

        // 3. Update attendance
        UpdateAttendanceRequest updateReq = new UpdateAttendanceRequest(AttendanceStatus.HALF_DAY, "Half day shift");
        mockMvc.perform(put("/api/v1/attendance/" + attId)
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("HALF_DAY")))
                .andExpect(jsonPath("$.note", is("Half day shift")));

        // 4. Delete attendance
        mockMvc.perform(delete("/api/v1/attendance/" + attId)
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isNoContent());

        // 5. Verify deleted
        mockMvc.perform(get("/api/v1/attendance/" + attId)
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cannot mark attendance before employee's joining date")
    void rejectAttendanceBeforeJoiningDate() throws Exception {
        // Employee joins on 2026-10-15
        Long empId = createEmployee(jwtTokenOwner1, "Santosh", "+919900000002", new BigDecimal("700.00"), LocalDate.of(2026, 10, 15));

        // Attempting to mark attendance for 2026-10-10 (before joining date)
        MarkAttendanceRequest markReq = new MarkAttendanceRequest(empId, LocalDate.of(2026, 10, 10), AttendanceStatus.PRESENT, "Attempt before joining");

        mockMvc.perform(post("/api/v1/attendance")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(markReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot mark attendance before employee joining date")));

        // Marking on or after joining date succeeds
        MarkAttendanceRequest validReq = new MarkAttendanceRequest(empId, LocalDate.of(2026, 10, 15), AttendanceStatus.PRESENT, "First day");
        mockMvc.perform(post("/api/v1/attendance")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PRESENT")));
    }

    @Test
    @DisplayName("Bulk Mark Attendance: Multiple employees processed in single request")
    void bulkMarkAttendanceSuccess() throws Exception {
        Long emp1 = createEmployee(jwtTokenOwner1, "Worker A", "+919900000010", new BigDecimal("600.00"), LocalDate.of(2026, 10, 1));
        Long emp2 = createEmployee(jwtTokenOwner1, "Worker B", "+919900000011", new BigDecimal("700.00"), LocalDate.of(2026, 10, 1));

        LocalDate date = LocalDate.of(2026, 10, 20);
        BulkMarkAttendanceRequest bulkReq = new BulkMarkAttendanceRequest(date, List.of(
                new BulkAttendanceItem(emp1, AttendanceStatus.PRESENT, "Site 1"),
                new BulkAttendanceItem(emp2, AttendanceStatus.HALF_DAY, "Site 2")
        ));

        mockMvc.perform(post("/api/v1/attendance/bulk")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendanceDate", is("2026-10-20")))
                .andExpect(jsonPath("$.processedCount", is(2)))
                .andExpect(jsonPath("$.records", hasSize(2)));
    }

    @Test
    @DisplayName("Monthly Register: Joining date filter rule (Oct joiner not in Sept register) and only active employees")
    void monthlyRegisterJoiningDateAndActiveRule() throws Exception {
        // Emp 1 joins in September (active)
        Long emp1 = createEmployee(jwtTokenOwner1, "Sep Active Worker", "+919900000021", new BigDecimal("600.00"), LocalDate.of(2026, 9, 10));

        // Emp 2 joins in October (active)
        Long emp2 = createEmployee(jwtTokenOwner1, "Oct Active Worker", "+919900000022", new BigDecimal("700.00"), LocalDate.of(2026, 10, 5));

        // Emp 3 joins in September, but gets blocked
        Long emp3 = createEmployee(jwtTokenOwner1, "Sep Blocked Worker", "+919900000023", new BigDecimal("500.00"), LocalDate.of(2026, 9, 1));
        mockMvc.perform(patch("/api/v1/employees/" + emp3 + "/block")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk());

        // 1. Check September Register (2026-09):
        // Only Emp 1 should appear! Emp 2 is excluded (joined in Oct). Emp 3 is excluded (BLOCKED).
        MvcResult septResult = mockMvc.perform(get("/api/v1/attendance/register?month=2026-09")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month", is("2026-09")))
                .andExpect(jsonPath("$.daysInMonth", is(30)))
                .andExpect(jsonPath("$.totalEligibleEmployees", is(1)))
                .andExpect(jsonPath("$.employees", hasSize(1)))
                .andExpect(jsonPath("$.employees[0].employeeId", is(emp1.intValue())))
                .andReturn();

        // 2. Check October Register (2026-10):
        // Both Emp 1 and Emp 2 appear! Emp 3 is still excluded (BLOCKED).
        mockMvc.perform(get("/api/v1/attendance/register?month=2026-10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month", is("2026-10")))
                .andExpect(jsonPath("$.daysInMonth", is(31)))
                .andExpect(jsonPath("$.totalEligibleEmployees", is(2)))
                .andExpect(jsonPath("$.employees", hasSize(2)));
    }

    @Test
    @DisplayName("Daily and Monthly Summaries and Individual History Calculations")
    void attendanceSummariesAndHistory() throws Exception {
        Long empId = createEmployee(jwtTokenOwner1, "Kiran Rao", "+919900000031", new BigDecimal("600.00"), LocalDate.of(2026, 10, 1));

        // Mark 2026-10-01 as PRESENT
        mockMvc.perform(post("/api/v1/attendance")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MarkAttendanceRequest(empId, LocalDate.of(2026, 10, 1), AttendanceStatus.PRESENT, "Day 1"))))
                .andExpect(status().isOk());

        // Mark 2026-10-02 as HALF_DAY
        mockMvc.perform(post("/api/v1/attendance")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MarkAttendanceRequest(empId, LocalDate.of(2026, 10, 2), AttendanceStatus.HALF_DAY, "Day 2"))))
                .andExpect(status().isOk());

        // Mark 2026-10-03 as ABSENT
        mockMvc.perform(post("/api/v1/attendance")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MarkAttendanceRequest(empId, LocalDate.of(2026, 10, 3), AttendanceStatus.ABSENT, "Day 3"))))
                .andExpect(status().isOk());

        // 1. Daily summary for 2026-10-01
        mockMvc.perform(get("/api/v1/attendance/summary/daily?date=2026-10-01")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendanceDate", is("2026-10-01")))
                .andExpect(jsonPath("$.totalEligibleEmployees", is(1)))
                .andExpect(jsonPath("$.totalMarked", is(1)))
                .andExpect(jsonPath("$.totalUnmarked", is(0)))
                .andExpect(jsonPath("$.presentCount", is(1)))
                .andExpect(jsonPath("$.halfDayCount", is(0)))
                .andExpect(jsonPath("$.absentCount", is(0)))
                .andExpect(jsonPath("$.totalEffectiveDays", is(1.0)));

        // 2. Monthly summary for 2026-10
        mockMvc.perform(get("/api/v1/attendance/summary/monthly?month=2026-10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month", is("2026-10")))
                .andExpect(jsonPath("$.totalPresentRecords", is(1)))
                .andExpect(jsonPath("$.totalHalfDayRecords", is(1)))
                .andExpect(jsonPath("$.totalAbsentRecords", is(1)))
                .andExpect(jsonPath("$.totalEffectiveDays", is(1.5)))
                .andExpect(jsonPath("$.totalEstimatedWages", is(900.00)));

        // 3. Employee Monthly History
        mockMvc.perform(get("/api/v1/attendance/employee/" + empId + "?month=2026-10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId", is(empId.intValue())))
                .andExpect(jsonPath("$.totalPresent", is(1)))
                .andExpect(jsonPath("$.totalHalfDay", is(1)))
                .andExpect(jsonPath("$.totalAbsent", is(1)))
                .andExpect(jsonPath("$.effectiveDays", is(1.5)))
                .andExpect(jsonPath("$.estimatedSalary", is(900.00)))
                .andExpect(jsonPath("$.attendanceHistory", hasSize(3)));
    }

    @Test
    @DisplayName("Multi-Tenancy Isolation: Owner 2 cannot access or mutate Owner 1's attendance")
    void multiTenantIsolation() throws Exception {
        Long emp1 = createEmployee(jwtTokenOwner1, "Owner 1 Worker", "+919900000041", new BigDecimal("600.00"), LocalDate.of(2026, 10, 1));

        MvcResult markResult = mockMvc.perform(post("/api/v1/attendance")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MarkAttendanceRequest(emp1, LocalDate.of(2026, 10, 10), AttendanceStatus.PRESENT, "Site A"))))
                .andExpect(status().isOk())
                .andReturn();

        Long attId = objectMapper.readTree(markResult.getResponse().getContentAsString()).get("id").asLong();

        // 1. Owner 2 tries to GET Owner 1's attendance
        mockMvc.perform(get("/api/v1/attendance/" + attId)
                        .header("Authorization", "Bearer " + jwtTokenOwner2))
                .andExpect(status().isNotFound());

        // 2. Owner 2 tries to UPDATE Owner 1's attendance
        mockMvc.perform(put("/api/v1/attendance/" + attId)
                        .header("Authorization", "Bearer " + jwtTokenOwner2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateAttendanceRequest(AttendanceStatus.ABSENT, "Hacked"))))
                .andExpect(status().isNotFound());

        // 3. Owner 2 tries to DELETE Owner 1's attendance
        mockMvc.perform(delete("/api/v1/attendance/" + attId)
                        .header("Authorization", "Bearer " + jwtTokenOwner2))
                .andExpect(status().isNotFound());

        // 4. Owner 2 tries to mark attendance for Owner 1's employee
        mockMvc.perform(post("/api/v1/attendance")
                        .header("Authorization", "Bearer " + jwtTokenOwner2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MarkAttendanceRequest(emp1, LocalDate.of(2026, 10, 11), AttendanceStatus.PRESENT, "Unauthorized mark"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Monthly Register: Pagination support (page, size, totalPages, totalElements)")
    void monthlyRegisterPagination() throws Exception {
        createEmployee(jwtTokenOwner1, "Worker 1", "+919900000051", new BigDecimal("600.00"), LocalDate.of(2026, 10, 1));
        createEmployee(jwtTokenOwner1, "Worker 2", "+919900000052", new BigDecimal("700.00"), LocalDate.of(2026, 10, 2));
        createEmployee(jwtTokenOwner1, "Worker 3", "+919900000053", new BigDecimal("800.00"), LocalDate.of(2026, 10, 3));

        // Request page 0 with size 2
        mockMvc.perform(get("/api/v1/attendance/register?month=2026-10&page=0&size=2")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.pageSize", is(2)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(false)))
                .andExpect(jsonPath("$.hasNext", is(true)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.employees", hasSize(2)));

        // Request page 1 with size 2
        mockMvc.perform(get("/api/v1/attendance/register?month=2026-10&page=1&size=2")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.pageSize", is(2)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.first", is(false)))
                .andExpect(jsonPath("$.last", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(true)))
                .andExpect(jsonPath("$.employees", hasSize(1)));
    }
}
