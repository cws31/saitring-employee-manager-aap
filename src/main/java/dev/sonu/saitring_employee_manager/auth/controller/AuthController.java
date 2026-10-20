
package dev.sonu.saitring_employee_manager.auth.controller;

import dev.sonu.saitring_employee_manager.auth.dto.*;
import dev.sonu.saitring_employee_manager.auth.service.AuthService;
import dev.sonu.saitring_employee_manager.common.exception.ErrorResponse;
import dev.sonu.saitring_employee_manager.owner.dto.OwnerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
@Tag(name = "Authentication", description = "Owner registration, two-factor login, and OTP verification")
public class AuthController {

        private final AuthService authService;

        public AuthController(AuthService authService) {
                this.authService = authService;
        }

        /**
         * Register an owner using JSON only.
         * No logo upload is supported by this endpoint.
         */
        @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Register an owner without a logo", description = "Registers a business owner using a JSON request.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Owner successfully registered", content = @Content(schema = @Schema(implementation = OwnerResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid registration request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Email or mobile number already registered", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<OwnerResponse> registerJson(
                        @Valid @org.springframework.web.bind.annotation.RequestBody RegisterRequest request) {

                OwnerResponse response = authService.register(request, null);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        /**
         * Register an owner using multipart/form-data.
         * The request part must contain JSON.
         * The logo part is optional.
         */
        @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Register an owner with an optional logo", description = """
                        Send a multipart/form-data request.
                        The 'request' part must be application/json.
                        The 'logo' part is optional.
                        """)
        @RequestBody(required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, encoding = {
                        @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE),
                        @Encoding(name = "logo", contentType = "image/png, image/jpeg, image/webp")
        }, schema = @Schema(implementation = MultipartRegistrationSchema.class)))
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Owner successfully registered", content = @Content(schema = @Schema(implementation = OwnerResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid registration request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Email or mobile number already registered", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "413", description = "Request exceeds upload limits", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "415", description = "Unsupported media type", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<OwnerResponse> registerWithMultipart(
                        @Valid @org.springframework.web.bind.annotation.RequestPart("request") RegisterRequest request,

                        @org.springframework.web.bind.annotation.RequestPart(value = "logo", required = false) MultipartFile logo) {

                OwnerResponse response = authService.register(request, logo);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        @PostMapping("/login")
        @Operation(summary = "Initiate login with dynamic two-factor authentication", description = "Validates credentials and sends an OTP to the appropriate channel.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "OTP challenge created", content = @Content(schema = @Schema(implementation = OtpChallengeResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials or inactive account", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<OtpChallengeResponse> login(
                        @Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest request) {

                return ResponseEntity.ok(authService.initiateLogin(request));
        }

        @PostMapping("/verify-otp")
        @Operation(summary = "Verify the OTP code")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "OTP successfully verified", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid or expired OTP", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<AuthResponse> verifyOtp(
                        @Valid @org.springframework.web.bind.annotation.RequestBody VerifyOtpRequest request) {

                return ResponseEntity.ok(authService.verifyOtp(request));
        }

        @PostMapping("/resend-otp")
        @Operation(summary = "Resend the OTP code")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "New OTP dispatched", content = @Content(schema = @Schema(implementation = OtpChallengeResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Cooldown active or challenge expired", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<OtpChallengeResponse> resendOtp(
                        @Valid @org.springframework.web.bind.annotation.RequestBody ResendOtpRequest request) {

                return ResponseEntity.ok(authService.resendOtp(request));
        }

        @Schema(name = "MultipartRegistrationRequest")
        public static class MultipartRegistrationSchema {

                @Schema(description = "Owner registration data encoded as JSON", implementation = RegisterRequest.class, requiredMode = Schema.RequiredMode.REQUIRED)
                public RegisterRequest request;

                @Schema(description = "Optional organization logo", type = "string", format = "binary", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                public String logo;
        }
}
