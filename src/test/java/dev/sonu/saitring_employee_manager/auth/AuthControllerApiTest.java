package dev.sonu.saitring_employee_manager.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sonu.saitring_employee_manager.advance.repository.AdvanceRepository;
import dev.sonu.saitring_employee_manager.attendance.repository.AttendanceRepository;
import dev.sonu.saitring_employee_manager.auth.dto.LoginRequest;
import dev.sonu.saitring_employee_manager.auth.dto.RegisterRequest;
import dev.sonu.saitring_employee_manager.auth.dto.VerifyOtpRequest;
import dev.sonu.saitring_employee_manager.auth.service.OtpChallengeData;
import dev.sonu.saitring_employee_manager.auth.service.OtpStorageService;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRateHistoryRepository;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRepository;
import dev.sonu.saitring_employee_manager.owner.dto.UpdateOwnerProfileRequest;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import dev.sonu.saitring_employee_manager.hisab.repository.HisabRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerApiTest {

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

    @BeforeEach
    void cleanUp() {
        hisabRepository.deleteAll();
        advanceRepository.deleteAll();
        attendanceRepository.deleteAll();
        employeeRateHistoryRepository.deleteAll();
        employeeRepository.deleteAll();
        ownerRepository.deleteAll();
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

    @Test
    @DisplayName("Complete API Flow: Register -> Email Login -> 2FA Verify -> Get Profile -> Update Profile")
    void fullAuthFlowWithEmail() throws Exception {
        // 1. Register Owner
        RegisterRequest registerReq = new RegisterRequest(
                "Vikram Singh",
                "Vikram Saitring Center",
                "vikram@saitring.com",
                "+919876543210",
                "Password@123",
                "GIDC Industrial Area, Surat"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email", is("vikram@saitring.com")))
                .andExpect(jsonPath("$.organizationName", is("Vikram Saitring Center")))
                .andExpect(jsonPath("$.role", is("ROLE_OWNER")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        // 2. Initiate Login with Email
        LoginRequest loginReq = new LoginRequest("vikram@saitring.com", "Password@123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challengeToken").isNotEmpty())
                .andExpect(jsonPath("$.deliveryChannel", is("EMAIL")))
                .andExpect(jsonPath("$.maskedDestination").isNotEmpty())
                .andReturn();

        JsonNode loginNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String challengeToken = loginNode.get("challengeToken").asText();

        // 3. Retrieve Generated OTP from OtpStorageService
        OtpChallengeData challenge = otpStorageService.getChallenge(challengeToken).orElse(null);
        assertNotNull(challenge, "Challenge should be stored in OtpStorageService");
        String otp = challenge.getOtp();

        // 4. Verify 2FA OTP
        VerifyOtpRequest verifyReq = new VerifyOtpRequest(challengeToken, otp);

        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.owner.email", is("vikram@saitring.com")))
                .andReturn();

        JsonNode verifyNode = objectMapper.readTree(verifyResult.getResponse().getContentAsString());
        String jwtToken = verifyNode.get("accessToken").asText();

        // 5. Access Protected Endpoint /api/v1/owners/profile using JWT
        mockMvc.perform(get("/api/v1/owners/profile")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName", is("Vikram Singh")))
                .andExpect(jsonPath("$.email", is("vikram@saitring.com")));

        // 6. Update Owner Profile with JWT
        UpdateOwnerProfileRequest updateReq = new UpdateOwnerProfileRequest(
                "Vikram S. Rathore",
                "Vikram Scaffolding & Formwork",
                "vikram.updated@saitring.com",
                "+919876543219",
                "New Address Road, Surat"
        );

        mockMvc.perform(put("/api/v1/owners/profile")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName", is("Vikram S. Rathore")))
                .andExpect(jsonPath("$.organizationName", is("Vikram Scaffolding & Formwork")))
                .andExpect(jsonPath("$.email", is("vikram.updated@saitring.com")));
    }

    @Test
    @DisplayName("Dynamic 2FA via Mobile Phone dispatches SMS channel")
    void dynamic2FaWithMobileNumber() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "Mahesh", "Mahesh Saitring", "mahesh@saitring.com", "+919876512345", "Pass@123", "Ahmedabad"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // Login using phone number directly
        LoginRequest loginReq = new LoginRequest("+919876512345", "Pass@123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryChannel", is("SMS")))
                .andExpect(jsonPath("$.challengeToken").isNotEmpty());
    }

    @Test
    @DisplayName("Register with Multipart Logo Upload succeeds")
    void registerWithMultipartLogo() throws Exception {
        MockMultipartFile logo = new MockMultipartFile(
                "logo", "brand-logo.png", "image/png", "fake-image-bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/auth/register")
                        .file(logo)
                        .param("ownerName", "Rajesh")
                        .param("organizationName", "Rajesh Formwork")
                        .param("email", "rajesh@saitring.com")
                        .param("mobileNumber", "+919876598765")
                        .param("password", "Pass@123")
                        .param("address", "Sector 4, Gandhinagar"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.logoUrl").isNotEmpty());
    }

    @Test
    @DisplayName("Register rejects duplicate email with 409 Conflict")
    void registerRejectsDuplicateEmail() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "Vikram", "Vikram Saitring", "dup@saitring.com", "+919876543201", "Password@123", "Surat"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        RegisterRequest duplicate = new RegisterRequest(
                "Other Name", "Other Saitring", "dup@saitring.com", "+919876543202", "Password@123", "Surat"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("Email is already registered")));
    }

    @Test
    @DisplayName("Register with Swagger UI multipart JSON part (-F request={...}) without logo")
    void registerWithSwaggerUiMultipartJsonPart() throws Exception {
        String requestJson = "{\"ownerName\":\"shivbachan sah\",\"organizationName\":\"sonu saitring\",\"email\":\"231fa04053@gmail.com\",\"mobileNumber\":\"+917294085931\",\"password\":\"Sonusaitring@31\",\"address\":\"awsanpur\"}";
        MockMultipartFile emptyLogo = new MockMultipartFile("logo", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/api/v1/auth/register")
                        .file(emptyLogo)
                        .param("request", requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ownerName", is("shivbachan sah")))
                .andExpect(jsonPath("$.organizationName", is("sonu saitring")))
                .andExpect(jsonPath("$.email", is("231fa04053@gmail.com")))
                .andExpect(jsonPath("$.phoneNumber", is("+917294085931")));
    }

    @Test
    @DisplayName("Update profile with Swagger UI multipart JSON part (-F request={...}) without logo")
    void updateProfileWithSwaggerUiMultipartJsonPart() throws Exception {
        // Register & login
        RegisterRequest registerReq = new RegisterRequest(
                "Shivbachan", "Sonu Saitring", "shiv@gmail.com", "+917294085999", "Pass@123", "Awsanpur"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("shiv@gmail.com", "Pass@123");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String challengeToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("challengeToken").asText();
        String otp = otpStorageService.getChallenge(challengeToken).orElseThrow().getOtp();

        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyOtpRequest(challengeToken, otp))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(verifyResult.getResponse().getContentAsString()).get("accessToken").asText();

        // Update profile using Swagger UI multipart style:
        String updateJson = "{\"ownerName\":\"Shivbachan Sah\",\"organizationName\":\"Sonu Saitring Works\",\"email\":\"shiv.updated@gmail.com\",\"phoneNumber\":\"+917294085999\",\"address\":\"Awsanpur Main Road\"}";
        MockMultipartFile emptyLogo = new MockMultipartFile("logo", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PUT, "/api/v1/owners/profile")
                        .file(emptyLogo)
                        .param("request", updateJson)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName", is("Shivbachan Sah")))
                .andExpect(jsonPath("$.organizationName", is("Sonu Saitring Works")))
                .andExpect(jsonPath("$.email", is("shiv.updated@gmail.com")));
    }

    @Test
    @DisplayName("Protected endpoint rejects request without Authorization header with 401")
    void protectedEndpointRejectsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/owners/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
