package dev.sonu.saitring_employee_manager.advance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sonu.saitring_employee_manager.advance.dto.CreateAdvanceRequest;
import dev.sonu.saitring_employee_manager.advance.dto.UpdateAdvanceRequest;
import dev.sonu.saitring_employee_manager.advance.repository.AdvanceRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdvanceControllerApiTest {

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
    @DisplayName("Complete Advance CRUD: Create -> Get -> Update -> Delete")
    void advanceCrudLifecycle() throws Exception {
        Long empId = createEmployee(jwtTokenOwner1, "Raju Shinde", "+919900000101", new BigDecimal("600.00"), LocalDate.of(2026, 10, 1));

        // 1. Create Advance
        CreateAdvanceRequest createReq = new CreateAdvanceRequest(empId, new BigDecimal("1000.00"), LocalDate.of(2026, 10, 5), "Personal need");
        MvcResult createResult = mockMvc.perform(post("/api/v1/advances")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.employeeId", is(empId.intValue())))
                .andExpect(jsonPath("$.amount", is(1000.00)))
                .andExpect(jsonPath("$.advanceDate", is("2026-10-05")))
                .andExpect(jsonPath("$.note", is("Personal need")))
                .andReturn();

        Long advId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Get by ID
        mockMvc.perform(get("/api/v1/advances/" + advId)
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(advId.intValue())))
                .andExpect(jsonPath("$.amount", is(1000.00)));

        // 3. Update Advance
        UpdateAdvanceRequest updateReq = new UpdateAdvanceRequest(new BigDecimal("1200.00"), LocalDate.of(2026, 10, 6), "Updated note");
        mockMvc.perform(put("/api/v1/advances/" + advId)
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(1200.00)))
                .andExpect(jsonPath("$.advanceDate", is("2026-10-06")))
                .andExpect(jsonPath("$.note", is("Updated note")));

        // 4. Delete Advance
        mockMvc.perform(delete("/api/v1/advances/" + advId)
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isNoContent());

        // 5. Verify 404
        mockMvc.perform(get("/api/v1/advances/" + advId)
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cannot disburse advance before employee's joining date")
    void rejectAdvanceBeforeJoiningDate() throws Exception {
        // Joined on Oct 15
        Long empId = createEmployee(jwtTokenOwner1, "Santosh", "+919900000102", new BigDecimal("700.00"), LocalDate.of(2026, 10, 15));

        // Attempt advance on Oct 10
        CreateAdvanceRequest request = new CreateAdvanceRequest(empId, new BigDecimal("500.00"), LocalDate.of(2026, 10, 10), "Early advance");

        mockMvc.perform(post("/api/v1/advances")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot disburse advance before employee joining date")));

        // Advance on or after joining date succeeds
        CreateAdvanceRequest validReq = new CreateAdvanceRequest(empId, new BigDecimal("500.00"), LocalDate.of(2026, 10, 15), "Joining day advance");
        mockMvc.perform(post("/api/v1/advances")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount", is(500.00)));
    }

    @Test
    @DisplayName("Advance Register: Joining date filter rule and pagination support")
    void advanceRegisterJoiningDateAndPagination() throws Exception {
        // Emp 1 joins in September
        Long emp1 = createEmployee(jwtTokenOwner1, "Sep Worker", "+919900000111", new BigDecimal("600.00"), LocalDate.of(2026, 9, 1));
        // Emp 2 joins in October
        Long emp2 = createEmployee(jwtTokenOwner1, "Oct Worker", "+919900000112", new BigDecimal("700.00"), LocalDate.of(2026, 10, 1));
        // Emp 3 joins in September, but gets blocked
        Long emp3 = createEmployee(jwtTokenOwner1, "Blocked Worker", "+919900000113", new BigDecimal("500.00"), LocalDate.of(2026, 9, 1));
        mockMvc.perform(patch("/api/v1/employees/" + emp3 + "/block")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk());

        // Disburse advance to Emp 1 in Sep
        mockMvc.perform(post("/api/v1/advances")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAdvanceRequest(emp1, new BigDecimal("1000.00"), LocalDate.of(2026, 9, 5), "Sep adv"))))
                .andExpect(status().isCreated());

        // 1. September Register (2026-09): only Emp 1 should appear!
        mockMvc.perform(get("/api/v1/advances/register?month=2026-09&page=0&size=10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month", is("2026-09")))
                .andExpect(jsonPath("$.totalEligibleEmployees", is(1)))
                .andExpect(jsonPath("$.employees", hasSize(1)))
                .andExpect(jsonPath("$.employees[0].employeeId", is(emp1.intValue())))
                .andExpect(jsonPath("$.employees[0].monthlyTotal", is(1000.00)))
                .andExpect(jsonPath("$.employees[0].runningAdvanceBalance", is(1000.00)));

        // 2. October Register (2026-10): both Emp 1 and Emp 2 appear! Emp 3 is excluded (BLOCKED).
        mockMvc.perform(get("/api/v1/advances/register?month=2026-10&page=0&size=10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month", is("2026-10")))
                .andExpect(jsonPath("$.totalEligibleEmployees", is(2)))
                .andExpect(jsonPath("$.employees", hasSize(2)));
    }

    @Test
    @DisplayName("Employee Advance History with Month Filter and Running Balance")
    void employeeAdvanceHistory() throws Exception {
        Long empId = createEmployee(jwtTokenOwner1, "Kiran Rao", "+919900000121", new BigDecimal("600.00"), LocalDate.of(2026, 10, 1));

        // Advance 1 in Oct
        mockMvc.perform(post("/api/v1/advances")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAdvanceRequest(empId, new BigDecimal("500.00"), LocalDate.of(2026, 10, 2), "Adv 1"))))
                .andExpect(status().isCreated());

        // Advance 2 in Oct
        mockMvc.perform(post("/api/v1/advances")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAdvanceRequest(empId, new BigDecimal("800.00"), LocalDate.of(2026, 10, 15), "Adv 2"))))
                .andExpect(status().isCreated());

        // Fetch history for Oct
        mockMvc.perform(get("/api/v1/advances/employee/" + empId + "?month=2026-10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId", is(empId.intValue())))
                .andExpect(jsonPath("$.periodTotal", is(1300.00)))
                .andExpect(jsonPath("$.runningAdvanceBalance", is(1300.00)))
                .andExpect(jsonPath("$.advances.totalElements", is(2)));
    }

    @Test
    @DisplayName("Monthly and Running Advance Summaries")
    void advanceSummaries() throws Exception {
        Long empId = createEmployee(jwtTokenOwner1, "Sunil Joshi", "+919900000131", new BigDecimal("600.00"), LocalDate.of(2026, 10, 1));

        mockMvc.perform(post("/api/v1/advances")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAdvanceRequest(empId, new BigDecimal("2000.00"), LocalDate.of(2026, 10, 10), "Equipment advance"))))
                .andExpect(status().isCreated());

        // 1. Monthly Summary
        mockMvc.perform(get("/api/v1/advances/summary/monthly?month=2026-10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month", is("2026-10")))
                .andExpect(jsonPath("$.totalAdvanceDisbursed", is(2000.00)))
                .andExpect(jsonPath("$.totalEmployeesTookAdvance", is(1)))
                .andExpect(jsonPath("$.totalTransactions", is(1)))
                .andExpect(jsonPath("$.totalOutstandingRunningAdvance", is(2000.00)));

        // 2. Running Summary (Owner overall view)
        mockMvc.perform(get("/api/v1/advances/summary/running")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOutstandingRunningAdvance", is(2000.00)))
                .andExpect(jsonPath("$.totalActiveEmployees", is(1)))
                .andExpect(jsonPath("$.employeesWithAdvanceCount", is(1)))
                .andExpect(jsonPath("$.topAdvanceHolders", hasSize(1)))
                .andExpect(jsonPath("$.topAdvanceHolders[0].employeeId", is(empId.intValue())))
                .andExpect(jsonPath("$.topAdvanceHolders[0].runningAdvance", is(2000.00)));
    }

    @Test
    @DisplayName("Multi-Tenant Isolation: Owner 2 cannot access or mutate Owner 1's advances")
    void multiTenantIsolation() throws Exception {
        Long emp1 = createEmployee(jwtTokenOwner1, "Owner 1 Worker", "+919900000141", new BigDecimal("600.00"), LocalDate.of(2026, 10, 1));

        MvcResult createResult = mockMvc.perform(post("/api/v1/advances")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAdvanceRequest(emp1, new BigDecimal("1500.00"), LocalDate.of(2026, 10, 5), "Private"))))
                .andExpect(status().isCreated())
                .andReturn();

        Long advId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 1. Owner 2 tries to GET Owner 1's advance -> 404
        mockMvc.perform(get("/api/v1/advances/" + advId)
                        .header("Authorization", "Bearer " + jwtTokenOwner2))
                .andExpect(status().isNotFound());

        // 2. Owner 2 tries to UPDATE Owner 1's advance -> 404
        mockMvc.perform(put("/api/v1/advances/" + advId)
                        .header("Authorization", "Bearer " + jwtTokenOwner2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateAdvanceRequest(new BigDecimal("100.00"), LocalDate.of(2026, 10, 6), "Hacked"))))
                .andExpect(status().isNotFound());

        // 3. Owner 2 tries to DELETE Owner 1's advance -> 404
        mockMvc.perform(delete("/api/v1/advances/" + advId)
                        .header("Authorization", "Bearer " + jwtTokenOwner2))
                .andExpect(status().isNotFound());

        // 4. Owner 2 tries to create advance for Owner 1's employee -> 404
        mockMvc.perform(post("/api/v1/advances")
                        .header("Authorization", "Bearer " + jwtTokenOwner2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAdvanceRequest(emp1, new BigDecimal("500.00"), LocalDate.of(2026, 10, 10), "Unauthorized"))))
                .andExpect(status().isNotFound());
    }
}
