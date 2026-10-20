package dev.sonu.saitring_employee_manager.auth.controller;

import dev.sonu.saitring_employee_manager.auth.dto.*;
import dev.sonu.saitring_employee_manager.auth.service.AuthService;
import dev.sonu.saitring_employee_manager.common.exception.ErrorResponse;
import dev.sonu.saitring_employee_manager.owner.dto.OwnerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for Owner tenant registration, 2FA dynamic login, and OTP verification")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Register a new Owner tenant (Multipart/Form-Data)",
            description = "Registers a new business owner tenant with mandatory details and optional organization logo upload to Supabase.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Owner successfully registered",
                    content = @Content(schema = @Schema(implementation = OwnerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email or mobile number already registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OwnerResponse> registerWithMultipart(
            @Valid @ModelAttribute RegisterRequest request,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        OwnerResponse response = authService.register(request, logo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register a new Owner tenant (JSON)",
            description = "Registers a new business owner tenant without logo file upload.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Owner successfully registered",
                    content = @Content(schema = @Schema(implementation = OwnerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email or mobile number already registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OwnerResponse> registerJson(@Valid @RequestBody RegisterRequest request) {
        OwnerResponse response = authService.register(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Initiate Login with Dynamic 2FA",
            description = "Validates credentials. If owner logged in with email, sends OTP via Brevo to email. If owner logged in with mobile number, sends OTP via Twilio to mobile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "2FA OTP Challenge generated successfully",
                    content = @Content(schema = @Schema(implementation = OtpChallengeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or inactive account",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OtpChallengeResponse> login(@Valid @RequestBody LoginRequest request) {
        OtpChallengeResponse response = authService.initiateLogin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify 2FA OTP Code",
            description = "Verifies 6-digit OTP code against the challenge token. Upon success, returns JWT access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP successfully verified; JWT token returned",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend 2FA OTP Code",
            description = "Resends a fresh OTP code to the challenge's destination with a minimum 30s cooldown.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New OTP dispatched",
                    content = @Content(schema = @Schema(implementation = OtpChallengeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cooldown active or expired challenge",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OtpChallengeResponse> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        OtpChallengeResponse response = authService.resendOtp(request);
        return ResponseEntity.ok(response);
    }
}
