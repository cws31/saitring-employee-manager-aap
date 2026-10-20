package dev.sonu.saitring_employee_manager.employee;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sonu.saitring_employee_manager.auth.dto.LoginRequest;
import dev.sonu.saitring_employee_manager.auth.dto.RegisterRequest;
import dev.sonu.saitring_employee_manager.auth.dto.VerifyOtpRequest;
import dev.sonu.saitring_employee_manager.auth.service.OtpChallengeData;
import dev.sonu.saitring_employee_manager.auth.service.OtpStorageService;
import dev.sonu.saitring_employee_manager.employee.dto.CreateEmployeeRequest;
import dev.sonu.saitring_employee_manager.employee.dto.UpdateEmployeeRequest;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRepository;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private OtpStorageService otpStorageService;

    private String jwtTokenOwner1;
    private String jwtTokenOwner2;

    @BeforeEach
    void setupOwners() throws Exception {
        employeeRepository.deleteAll();
        ownerRepository.deleteAll();

        // 1. Register and login Owner 1
        jwtTokenOwner1 = registerAndLogin(
                "Ramesh Builder",
                "Ramesh Scaffolding",
                "ramesh.owner1@saitring.com",
                "+919811111111",
                "Pass@123"
        );

        // 2. Register and login Owner 2
        jwtTokenOwner2 = registerAndLogin(
                "Suresh Builder",
                "Suresh Formwork",
                "suresh.owner2@saitring.com",
                "+919822222222",
                "Pass@123"
        );
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

    @Test
    @DisplayName("Complete Employee Flow: Create -> List -> Update -> Block -> Unblock -> Delete")
    void fullEmployeeCrudLifecycle() throws Exception {
        // 1. Owner 1 creates Employee
        CreateEmployeeRequest createReq = new CreateEmployeeRequest(
                "Ganesh Shinde",
                "+919876500001",
                new BigDecimal("650.00"),
                LocalDate.of(2026, 3, 1)
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.employeeName", is("Ganesh Shinde")))
                .andExpect(jsonPath("$.mobileNumber", is("+919876500001")))
                .andExpect(jsonPath("$.dailyRate", is(650.0)))
                .andExpect(jsonPath("$.joiningDate", is("2026-03-01")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andReturn();

        JsonNode createNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long employeeId = createNode.get("id").asLong();

        // 2. Owner 1 fetches Employee by ID
        mockMvc.perform(get("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) employeeId)))
                .andExpect(jsonPath("$.employeeName", is("Ganesh Shinde")));

        // 3. Owner 1 lists Employees
        mockMvc.perform(get("/api/v1/employees")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].employeeName", is("Ganesh Shinde")));

        // 4. Owner 1 updates Employee
        UpdateEmployeeRequest updateReq = new UpdateEmployeeRequest(
                "Ganesh R. Shinde",
                "+919876500001",
                new BigDecimal("700.00"),
                LocalDate.of(2026, 3, 1)
        );

        mockMvc.perform(put("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName", is("Ganesh R. Shinde")))
                .andExpect(jsonPath("$.dailyRate", is(700.0)));

        // 5. Owner 1 blocks Employee
        mockMvc.perform(patch("/api/v1/employees/" + employeeId + "/block")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("BLOCKED")));

        // 6. Owner 1 unblocks Employee
        mockMvc.perform(patch("/api/v1/employees/" + employeeId + "/unblock")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        // 7. Owner 1 deletes Employee
        mockMvc.perform(delete("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isNoContent());

        // 8. Confirm Employee is deleted
        mockMvc.perform(get("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Strict Multi-Tenant Isolation: Owner 2 cannot view, update, block, or delete Owner 1's Employee")
    void strictMultiTenantIsolation() throws Exception {
        // Owner 1 creates employee
        CreateEmployeeRequest createReq = new CreateEmployeeRequest(
                "Mahesh Worker",
                "+919876500002",
                new BigDecimal("600.00"),
                LocalDate.of(2026, 2, 1)
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long employeeId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 1. Owner 2 tries to GET Owner 1's employee -> 404 Not Found
        mockMvc.perform(get("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + jwtTokenOwner2))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));

        // 2. Owner 2 tries to UPDATE Owner 1's employee -> 404 Not Found
        UpdateEmployeeRequest updateReq = new UpdateEmployeeRequest(
                "Hacked Name", "+919876500002", new BigDecimal("1000.00"), LocalDate.of(2026, 2, 1)
        );
        mockMvc.perform(put("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + jwtTokenOwner2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // 3. Owner 2 tries to BLOCK Owner 1's employee -> 404 Not Found
        mockMvc.perform(patch("/api/v1/employees/" + employeeId + "/block")
                        .header("Authorization", "Bearer " + jwtTokenOwner2))
                .andExpect(status().isNotFound());

        // 4. Owner 2 tries to DELETE Owner 1's employee -> 404 Not Found
        mockMvc.perform(delete("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + jwtTokenOwner2))
                .andExpect(status().isNotFound());

        // 5. Owner 2 lists employees -> sees 0 employees
        mockMvc.perform(get("/api/v1/employees")
                        .header("Authorization", "Bearer " + jwtTokenOwner2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Tenant-Scoped Mobile Uniqueness: Two different owners CAN have employees with same mobile number, but one owner CANNOT")
    void tenantScopedMobileUniqueness() throws Exception {
        String sharedMobile = "+919876599999";

        // Owner 1 creates employee with sharedMobile
        CreateEmployeeRequest req1 = new CreateEmployeeRequest(
                "Worker One", sharedMobile, new BigDecimal("550.00"), LocalDate.of(2026, 1, 1)
        );
        mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        // Owner 2 ALSO creates employee with the SAME sharedMobile -> SUCCEEDS!
        CreateEmployeeRequest req2 = new CreateEmployeeRequest(
                "Worker Two", sharedMobile, new BigDecimal("600.00"), LocalDate.of(2026, 1, 1)
        );
        mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + jwtTokenOwner2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        // Owner 1 tries to create ANOTHER employee with the same sharedMobile -> 409 CONFLICT!
        CreateEmployeeRequest reqDuplicate = new CreateEmployeeRequest(
                "Worker Three", sharedMobile, new BigDecimal("550.00"), LocalDate.of(2026, 1, 1)
        );
        mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDuplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("already registered in your organization")));
    }

    @Test
    @DisplayName("List employees with Swagger UI default sort parameter (?sort=string) gracefully defaults without crashing")
    void listEmployeesWithSwaggerDefaultSortParam() throws Exception {
        CreateEmployeeRequest createReq = new CreateEmployeeRequest(
                "Santosh", "+919876543999", new BigDecimal("650.00"), LocalDate.of(2026, 3, 1)
        );
        mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        // Call with Swagger UI's default parameter sort=string
        mockMvc.perform(get("/api/v1/employees?sort=string")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].employeeName", is("Santosh")));
    }

    @Test
    @DisplayName("Unauthenticated request to employee endpoint returns 401 Unauthorized")
    void unauthenticatedRequestRejected() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }
}
