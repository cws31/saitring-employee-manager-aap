package dev.sonu.saitring_employee_manager.hisab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sonu.saitring_employee_manager.advance.dto.CreateAdvanceRequest;
import dev.sonu.saitring_employee_manager.advance.repository.AdvanceRepository;
import dev.sonu.saitring_employee_manager.attendance.dto.MarkAttendanceRequest;
import dev.sonu.saitring_employee_manager.attendance.entity.AttendanceStatus;
import dev.sonu.saitring_employee_manager.attendance.repository.AttendanceRepository;
import dev.sonu.saitring_employee_manager.auth.dto.LoginRequest;
import dev.sonu.saitring_employee_manager.auth.dto.RegisterRequest;
import dev.sonu.saitring_employee_manager.auth.dto.VerifyOtpRequest;
import dev.sonu.saitring_employee_manager.auth.service.OtpChallengeData;
import dev.sonu.saitring_employee_manager.auth.service.OtpStorageService;
import dev.sonu.saitring_employee_manager.employee.dto.CreateEmployeeRequest;
import dev.sonu.saitring_employee_manager.employee.dto.RateRevisionRequest;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRateHistoryRepository;
import dev.sonu.saitring_employee_manager.employee.repository.EmployeeRepository;
import dev.sonu.saitring_employee_manager.hisab.dto.SettleHisabRequest;
import dev.sonu.saitring_employee_manager.hisab.entity.PaymentMode;
import dev.sonu.saitring_employee_manager.hisab.repository.HisabRepository;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import org.junit.jupiter.api.AfterEach;
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
class HisabControllerApiTest {

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

    private void markAttendance(String token, Long employeeId, LocalDate date, AttendanceStatus status, String note) throws Exception {
        MarkAttendanceRequest request = new MarkAttendanceRequest(employeeId, date, status, note);
        mockMvc.perform(post("/api/v1/attendance")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private void disburseAdvance(String token, Long employeeId, BigDecimal amount, LocalDate date, String note) throws Exception {
        CreateAdvanceRequest request = new CreateAdvanceRequest(employeeId, amount, date, note);
        mockMvc.perform(post("/api/v1/advances")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/v1/hisab: correctly calculates earnings, advance, and final_money with pagination")
    void getMonthlyHisabTable_calculationsAndPagination() throws Exception {
        Long emp1Id = createEmployee(jwtTokenOwner1, "Gopal Das", "+919100000001", new BigDecimal("600.00"), LocalDate.of(2026, 9, 1));
        Long emp2Id = createEmployee(jwtTokenOwner1, "Harish Verma", "+919100000002", new BigDecimal("700.00"), LocalDate.of(2026, 9, 15));

        // Emp1: 1 PRESENT (1.0) + 1 HALF_DAY (0.5) = 1.5 presenties. Rate = 600. Total earnings = 900.00
        markAttendance(jwtTokenOwner1, emp1Id, LocalDate.of(2026, 10, 1), AttendanceStatus.PRESENT, "Full site day");
        markAttendance(jwtTokenOwner1, emp1Id, LocalDate.of(2026, 10, 2), AttendanceStatus.HALF_DAY, "Half site day");
        // Emp1: Advance 200.00. Final money = 900.00 - 200.00 = 700.00
        disburseAdvance(jwtTokenOwner1, emp1Id, new BigDecimal("200.00"), LocalDate.of(2026, 10, 5), "Personal cash");

        // Emp2: 2 PRESENT = 2.0 presenties. Rate = 700. Total earnings = 1400.00
        markAttendance(jwtTokenOwner1, emp2Id, LocalDate.of(2026, 10, 1), AttendanceStatus.PRESENT, null);
        markAttendance(jwtTokenOwner1, emp2Id, LocalDate.of(2026, 10, 2), AttendanceStatus.PRESENT, null);
        // Emp2: Advance 500.00. Final money = 1400.00 - 500.00 = 900.00
        disburseAdvance(jwtTokenOwner1, emp2Id, new BigDecimal("500.00"), LocalDate.of(2026, 10, 10), "Medical");

        // Query hisab table
        mockMvc.perform(get("/api/v1/hisab?month=2026-10&page=0&size=10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month", is("2026-10")))
                .andExpect(jsonPath("$.daysInMonth", is(31)))
                .andExpect(jsonPath("$.totalEligibleEmployees", is(2)))
                .andExpect(jsonPath("$.totalFullDays", is(3)))
                .andExpect(jsonPath("$.totalHalfDays", is(1)))
                .andExpect(jsonPath("$.totalEffectivePresenties", is(3.5)))
                .andExpect(jsonPath("$.totalOrganizationEarnings", is(2300.00))) // 900 + 1400 = 2300.00
                .andExpect(jsonPath("$.totalOrganizationAdvance", is(700.00)))   // 200 + 500 = 700.00
                .andExpect(jsonPath("$.totalOrganizationFinalMoney", is(1600.00))) // 2300 - 700 = 1600.00
                .andExpect(jsonPath("$.employees", hasSize(2)))
                // Verify Employee 1 row
                .andExpect(jsonPath("$.employees[0].employeeName", is("Gopal Das")))
                .andExpect(jsonPath("$.employees[0].fullDays", is(1)))
                .andExpect(jsonPath("$.employees[0].halfDays", is(1)))
                .andExpect(jsonPath("$.employees[0].totalPresenties", is(1.5)))
                .andExpect(jsonPath("$.employees[0].rate", is(600.00)))
                .andExpect(jsonPath("$.employees[0].totalEarnings", is(900.00)))
                .andExpect(jsonPath("$.employees[0].totalAdvance", is(200.00)))
                .andExpect(jsonPath("$.employees[0].finalMoney", is(700.00)))
                .andExpect(jsonPath("$.employees[0].status", is("RUNNING")))
                // Verify Employee 2 row
                .andExpect(jsonPath("$.employees[1].employeeName", is("Harish Verma")))
                .andExpect(jsonPath("$.employees[1].fullDays", is(2)))
                .andExpect(jsonPath("$.employees[1].halfDays", is(0)))
                .andExpect(jsonPath("$.employees[1].totalPresenties", is(2.0)))
                .andExpect(jsonPath("$.employees[1].rate", is(700.00)))
                .andExpect(jsonPath("$.employees[1].totalEarnings", is(1400.00)))
                .andExpect(jsonPath("$.employees[1].totalAdvance", is(500.00)))
                .andExpect(jsonPath("$.employees[1].finalMoney", is(900.00)))
                .andExpect(jsonPath("$.employees[1].status", is("RUNNING")));
    }

    @Test
    @DisplayName("GET /api/v1/hisab: excludes blocked employees and future joiners")
    void getMonthlyHisabTable_strictBusinessRules() throws Exception {
        // Emp 1 joins in September -> should appear in October
        Long emp1Id = createEmployee(jwtTokenOwner1, "Valid Sept Joiner", "+919100000010", new BigDecimal("600.00"), LocalDate.of(2026, 9, 1));

        // Emp 2 joins in November -> should NOT appear in October
        createEmployee(jwtTokenOwner1, "November Joiner", "+919100000020", new BigDecimal("600.00"), LocalDate.of(2026, 11, 1));

        // Emp 3 joins in September but is BLOCKED -> should NOT appear
        Long emp3Id = createEmployee(jwtTokenOwner1, "Blocked Sept Joiner", "+919100000030", new BigDecimal("600.00"), LocalDate.of(2026, 9, 10));
        mockMvc.perform(patch("/api/v1/employees/" + emp3Id + "/block")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk());

        // Check October table: Only emp1 should appear!
        mockMvc.perform(get("/api/v1/hisab?month=2026-10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEligibleEmployees", is(1)))
                .andExpect(jsonPath("$.employees", hasSize(1)))
                .andExpect(jsonPath("$.employees[0].employeeId", is(emp1Id.intValue())))
                .andExpect(jsonPath("$.employees[0].employeeName", is("Valid Sept Joiner")));

        // Check September table: November joiner still excluded
        mockMvc.perform(get("/api/v1/hisab?month=2026-09")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEligibleEmployees", is(1)))
                .andExpect(jsonPath("$.employees", hasSize(1)))
                .andExpect(jsonPath("$.employees[0].employeeId", is(emp1Id.intValue())));

        // Check November table: November joiner appears, blocked worker still excluded
        mockMvc.perform(get("/api/v1/hisab?month=2026-11")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEligibleEmployees", is(2)))
                .andExpect(jsonPath("$.employees", hasSize(2)));
    }

    @Test
    @DisplayName("POST /api/v1/hisab/settle: settles hisab, marks advances as settled, and blocks duplicate settlement")
    void settleHisab_completeFlow() throws Exception {
        Long empId = createEmployee(jwtTokenOwner1, "Bholu Mistri", "+919100000050", new BigDecimal("800.00"), LocalDate.of(2026, 9, 1));

        // 3 full days worked in October: earnings = 2400.00
        markAttendance(jwtTokenOwner1, empId, LocalDate.of(2026, 10, 1), AttendanceStatus.PRESENT, null);
        markAttendance(jwtTokenOwner1, empId, LocalDate.of(2026, 10, 2), AttendanceStatus.PRESENT, null);
        markAttendance(jwtTokenOwner1, empId, LocalDate.of(2026, 10, 3), AttendanceStatus.PRESENT, null);

        // 500 advance taken: finalMoney = 1900.00
        disburseAdvance(jwtTokenOwner1, empId, new BigDecimal("500.00"), LocalDate.of(2026, 10, 4), "Festival advance");

        // Settle hisab
        SettleHisabRequest settleReq = new SettleHisabRequest(empId, "2026-10", PaymentMode.CASH, "Full cash settlement");
        mockMvc.perform(post("/api/v1/hisab/settle")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settleReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId", is(empId.intValue())))
                .andExpect(jsonPath("$.fullDays", is(3)))
                .andExpect(jsonPath("$.effectiveDays", is(3.0)))
                .andExpect(jsonPath("$.totalEarnings", is(2400.00)))
                .andExpect(jsonPath("$.totalAdvance", is(500.00)))
                .andExpect(jsonPath("$.finalMoney", is(1900.00)))
                .andExpect(jsonPath("$.status", is("SETTLED")))
                .andExpect(jsonPath("$.paymentMode", is("CASH")));

        // Calling settle again should return 400 Bad Request
        mockMvc.perform(post("/api/v1/hisab/settle")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settleReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already settled")));

        // View monthly hisab table: status should now be SETTLED
        mockMvc.perform(get("/api/v1/hisab?month=2026-10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees[0].status", is("SETTLED")))
                .andExpect(jsonPath("$.employees[0].paymentMode", is("CASH")));

        // View employee settlement history
        mockMvc.perform(get("/api/v1/hisab/employee/" + empId)
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId", is(empId.intValue())))
                .andExpect(jsonPath("$.settlements.content", hasSize(1)))
                .andExpect(jsonPath("$.settlements.content[0].month", is("2026-10")))
                .andExpect(jsonPath("$.settlements.content[0].finalMoney", is(1900.00)));
    }

    @Test
    @DisplayName("Multi-Tenant Security: Owner cannot see or settle another owner's hisab")
    void multiTenantSecurityIsolation() throws Exception {
        Long emp1Id = createEmployee(jwtTokenOwner1, "Owner1 Employee", "+919100000077", new BigDecimal("600.00"), LocalDate.of(2026, 9, 1));

        // Owner 2 queries hisab -> should see 0 employees
        mockMvc.perform(get("/api/v1/hisab?month=2026-10")
                        .header("Authorization", "Bearer " + jwtTokenOwner2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEligibleEmployees", is(0)))
                .andExpect(jsonPath("$.employees", hasSize(0)));

        // Owner 2 attempts to settle Owner 1's employee -> 404 Not Found
        SettleHisabRequest settleReq = new SettleHisabRequest(emp1Id, "2026-10", PaymentMode.CASH, "Unauthorized attempt");
        mockMvc.perform(post("/api/v1/hisab/settle")
                        .header("Authorization", "Bearer " + jwtTokenOwner2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settleReq)))
                .andExpect(status().isNotFound());

        // Owner 2 attempts to view Owner 1's employee hisab history -> 404 Not Found
        mockMvc.perform(get("/api/v1/hisab/employee/" + emp1Id)
                        .header("Authorization", "Bearer " + jwtTokenOwner2))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/hisab: invalid month format returns 400 Bad Request")
    void invalidMonthFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/hisab?month=2026-13")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid month format")));
    }

    @Test
    @DisplayName("GET /api/v1/hisab: historical wage rate versioning (September = 600, October = 650)")
    void getMonthlyHisabTable_wageRateVersioningAcrossMonths() throws Exception {
        // 1. Create employee in September with initial rate 600.00
        Long empId = createEmployee(jwtTokenOwner1, "Ramesh Mistri", "+919100000099", new BigDecimal("600.00"), LocalDate.of(2026, 9, 1));

        // Mark 2 full days in September (earnings = 1200.00)
        markAttendance(jwtTokenOwner1, empId, LocalDate.of(2026, 9, 1), AttendanceStatus.PRESENT, null);
        markAttendance(jwtTokenOwner1, empId, LocalDate.of(2026, 9, 2), AttendanceStatus.PRESENT, null);

        // 2. Revise rate to 650.00 effective from 2026-10-01
        RateRevisionRequest rateRevision = new RateRevisionRequest(new BigDecimal("650.00"), LocalDate.of(2026, 10, 1), "Skill revision");
        mockMvc.perform(post("/api/v1/employees/" + empId + "/rates")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rateRevision)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dailyRate", is(650.00)))
                .andExpect(jsonPath("$.effectiveFrom", is("2026-10-01")));

        // Mark 2 full days in October (earnings = 1300.00)
        markAttendance(jwtTokenOwner1, empId, LocalDate.of(2026, 10, 1), AttendanceStatus.PRESENT, null);
        markAttendance(jwtTokenOwner1, empId, LocalDate.of(2026, 10, 2), AttendanceStatus.PRESENT, null);

        // 3. Verify September hisab table uses rate 600.00 and calculates earnings 1200.00
        mockMvc.perform(get("/api/v1/hisab?month=2026-09")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month", is("2026-09")))
                .andExpect(jsonPath("$.employees[0].rate", is(600.00)))
                .andExpect(jsonPath("$.employees[0].totalEarnings", is(1200.00)))
                .andExpect(jsonPath("$.employees[0].finalMoney", is(1200.00)))
                .andExpect(jsonPath("$.totalOrganizationEarnings", is(1200.00)));

        // 4. Verify October hisab table uses rate 650.00 and calculates earnings 1300.00
        mockMvc.perform(get("/api/v1/hisab?month=2026-10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month", is("2026-10")))
                .andExpect(jsonPath("$.employees[0].rate", is(650.00)))
                .andExpect(jsonPath("$.employees[0].totalEarnings", is(1300.00)))
                .andExpect(jsonPath("$.employees[0].finalMoney", is(1300.00)))
                .andExpect(jsonPath("$.totalOrganizationEarnings", is(1300.00)));
    }

    @Test
    @DisplayName("Complete Hisab Clearance Flow: Partial clearance creates carry forward which propagates as opening advance into next month")
    void settlementClearanceAndCarryForwardPropagationAcrossMonths() throws Exception {
        // 1. Create Ramesh (rate 600, joined 2026-09-01)
        Long rameshId = createEmployee(jwtTokenOwner1, "Ramesh", "+919876599001", new BigDecimal("600.00"), LocalDate.of(2026, 9, 1));

        // 2. Ramesh works 20 days in September (20 * 600 = 12000.00)
        for (int day = 1; day <= 20; day++) {
            markAttendance(jwtTokenOwner1, rameshId, LocalDate.of(2026, 9, day), AttendanceStatus.PRESENT, null);
        }

        // 3. Ramesh takes 9000 advance in September
        disburseAdvance(jwtTokenOwner1, rameshId, new BigDecimal("9000.00"), LocalDate.of(2026, 9, 15), "Sept advance");

        // 4. Verify September Hisab Table before settlement: earnings=12000, totalAdvance=9000, finalMoney=3000
        mockMvc.perform(get("/api/v1/hisab?month=2026-09")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees[0].totalEarnings", is(12000.00)))
                .andExpect(jsonPath("$.employees[0].totalAdvance", is(9000.00)))
                .andExpect(jsonPath("$.employees[0].finalMoney", is(3000.00)))
                .andExpect(jsonPath("$.employees[0].status", is("RUNNING")));

        // 5. Owner clears/settles September Hisab with partial settlement amount Y = 2000.00
        SettleHisabRequest settleReq = new SettleHisabRequest(
                rameshId, "2026-09", PaymentMode.CASH, new BigDecimal("2000.00"), "Cleared 2000 cash; remaining 1000 carry forward"
        );
        mockMvc.perform(post("/api/v1/hisab/settle")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settleReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEarnings", is(12000.00)))
                .andExpect(jsonPath("$.totalAdvance", is(9000.00)))
                .andExpect(jsonPath("$.finalMoney", is(3000.00)))
                .andExpect(jsonPath("$.settlementAmount", is(2000.00)))
                .andExpect(jsonPath("$.carryForward", is(1000.00))) // X = 3000 - 2000 = 1000.00
                .andExpect(jsonPath("$.status", is("SETTLED")));

        // 6. In October, Ramesh takes advances: 2000, 3000, 1000 (total = 6000.00)
        disburseAdvance(jwtTokenOwner1, rameshId, new BigDecimal("2000.00"), LocalDate.of(2026, 10, 5), "Oct adv 1");
        disburseAdvance(jwtTokenOwner1, rameshId, new BigDecimal("3000.00"), LocalDate.of(2026, 10, 12), "Oct adv 2");
        disburseAdvance(jwtTokenOwner1, rameshId, new BigDecimal("1000.00"), LocalDate.of(2026, 10, 20), "Oct adv 3");

        // Ramesh works 20 days in October (20 * 600 = 12000.00)
        for (int day = 1; day <= 20; day++) {
            markAttendance(jwtTokenOwner1, rameshId, LocalDate.of(2026, 10, day), AttendanceStatus.PRESENT, null);
        }

        // 7. Verify October Hisab Table:
        // openingBalance = 1000.00 (from Sept carryForward credit)
        // monthlyAdvance = 6000.00 (2000 + 3000 + 1000)
        // totalAdvance = 5000.00 (6000 - 1000)
        // totalEarnings = 12000.00
        // finalMoney = 7000.00 (12000 - 5000)
        mockMvc.perform(get("/api/v1/hisab?month=2026-10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month", is("2026-10")))
                .andExpect(jsonPath("$.employees[0].openingBalance", is(1000.00)))
                .andExpect(jsonPath("$.employees[0].monthlyAdvance", is(6000.00)))
                .andExpect(jsonPath("$.employees[0].totalAdvance", is(5000.00)))
                .andExpect(jsonPath("$.employees[0].totalEarnings", is(12000.00)))
                .andExpect(jsonPath("$.employees[0].finalMoney", is(7000.00)))
                .andExpect(jsonPath("$.employees[0].status", is("RUNNING")));

        // 8. Verify Advance Register for October also reflects opening balance 1000.00 and monthly total 5000.00
        mockMvc.perform(get("/api/v1/advances/register?month=2026-10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees[0].openingBalance", is(1000.00)))
                .andExpect(jsonPath("$.employees[0].monthlyTotal", is(5000.00)));
    }

    @Test
    @DisplayName("Continuous Cyclic Recurrence: Settlement clearance and carry forward propagates seamlessly through Month 1 -> Month 2 -> Month 3 -> Month 4 -> Month 5")
    void multiMonthChainedSettlementCycleAcrossManyMonths() throws Exception {
        Long empId = createEmployee(jwtTokenOwner1, "Suresh", "+919876599002", new BigDecimal("600.00"), LocalDate.of(2026, 9, 1));

        // --- MONTH 1: September 2026 ---
        for (int d = 1; d <= 20; d++) {
            markAttendance(jwtTokenOwner1, empId, LocalDate.of(2026, 9, d), AttendanceStatus.PRESENT, null);
        }
        disburseAdvance(jwtTokenOwner1, empId, new BigDecimal("9000.00"), LocalDate.of(2026, 9, 10), "Sept adv");
        // Settle Sept: earnings=12000, adv=9000, final=3000. Owner pays 2000 -> carryForward = 1000.
        SettleHisabRequest settleSept = new SettleHisabRequest(empId, "2026-09", PaymentMode.CASH, new BigDecimal("2000.00"), "Sept clearance");
        mockMvc.perform(post("/api/v1/hisab/settle")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settleSept)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlementAmount", is(2000.00)))
                .andExpect(jsonPath("$.carryForward", is(1000.00)));

        // --- MONTH 2: October 2026 ---
        for (int d = 1; d <= 20; d++) {
            markAttendance(jwtTokenOwner1, empId, LocalDate.of(2026, 10, d), AttendanceStatus.PRESENT, null);
        }
        disburseAdvance(jwtTokenOwner1, empId, new BigDecimal("6000.00"), LocalDate.of(2026, 10, 15), "Oct adv");
        // Running hisab check: opening=1000 credit, totalAdv=6000-1000=5000, final=12000-5000=7000
        mockMvc.perform(get("/api/v1/hisab?month=2026-10")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees[0].openingBalance", is(1000.00)))
                .andExpect(jsonPath("$.employees[0].monthlyAdvance", is(6000.00)))
                .andExpect(jsonPath("$.employees[0].totalAdvance", is(5000.00)))
                .andExpect(jsonPath("$.employees[0].finalMoney", is(7000.00)));
        // Settle Oct: earnings=12000, totalAdv=5000, final=7000. Owner pays 4000 -> carryForward = 3000.
        SettleHisabRequest settleOct = new SettleHisabRequest(empId, "2026-10", PaymentMode.UPI, new BigDecimal("4000.00"), "Oct clearance");
        mockMvc.perform(post("/api/v1/hisab/settle")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settleOct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlementAmount", is(4000.00)))
                .andExpect(jsonPath("$.carryForward", is(3000.00)));

        // --- MONTH 3: November 2026 ---
        for (int d = 1; d <= 20; d++) {
            markAttendance(jwtTokenOwner1, empId, LocalDate.of(2026, 11, d), AttendanceStatus.PRESENT, null);
        }
        disburseAdvance(jwtTokenOwner1, empId, new BigDecimal("3000.00"), LocalDate.of(2026, 11, 10), "Nov adv");
        // Running hisab check: opening=3000 credit, monthAdv=3000, totalAdv=3000-3000=0, earnings=12000, final=12000
        mockMvc.perform(get("/api/v1/hisab?month=2026-11")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees[0].openingBalance", is(3000.00)))
                .andExpect(jsonPath("$.employees[0].monthlyAdvance", is(3000.00)))
                .andExpect(jsonPath("$.employees[0].totalAdvance", is(0.00)))
                .andExpect(jsonPath("$.employees[0].finalMoney", is(12000.00)));
        // Settle Nov: final=12000. Owner gives 14000 cash (2000 excess!) -> carryForward = -2000.
        SettleHisabRequest settleNov = new SettleHisabRequest(empId, "2026-11", PaymentMode.CASH, new BigDecimal("14000.00"), "Nov excess clearance");
        mockMvc.perform(post("/api/v1/hisab/settle")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settleNov)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlementAmount", is(14000.00)))
                .andExpect(jsonPath("$.carryForward", is(-2000.00)));

        // --- MONTH 4: December 2026 ---
        for (int d = 1; d <= 20; d++) {
            markAttendance(jwtTokenOwner1, empId, LocalDate.of(2026, 12, d), AttendanceStatus.PRESENT, null);
        }
        disburseAdvance(jwtTokenOwner1, empId, new BigDecimal("5000.00"), LocalDate.of(2026, 12, 10), "Dec adv");
        // Running hisab check: opening=-2000 (overdue), monthAdv=5000 -> totalAdv = 5000 - (-2000) = 7000. final = 12000 - 7000 = 5000.
        mockMvc.perform(get("/api/v1/hisab?month=2026-12")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees[0].openingBalance", is(-2000.00)))
                .andExpect(jsonPath("$.employees[0].monthlyAdvance", is(5000.00)))
                .andExpect(jsonPath("$.employees[0].totalAdvance", is(7000.00)))
                .andExpect(jsonPath("$.employees[0].finalMoney", is(5000.00)));
        // Settle Dec with exact 5000 -> carryForward = 0.00
        SettleHisabRequest settleDec = new SettleHisabRequest(empId, "2026-12", PaymentMode.BANK_TRANSFER, new BigDecimal("5000.00"), "Dec full clearance");
        mockMvc.perform(post("/api/v1/hisab/settle")
                        .header("Authorization", "Bearer " + jwtTokenOwner1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settleDec)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlementAmount", is(5000.00)))
                .andExpect(jsonPath("$.carryForward", is(0.00)));

        // --- MONTH 5: January 2027 ---
        // Verify January opens with exactly 0.00 opening balance
        mockMvc.perform(get("/api/v1/hisab?month=2027-01")
                        .header("Authorization", "Bearer " + jwtTokenOwner1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees[0].openingBalance", is(0.00)))
                .andExpect(jsonPath("$.employees[0].totalAdvance", is(0.00)));
    }
}
