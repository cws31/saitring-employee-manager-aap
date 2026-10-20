package dev.sonu.saitring_employee_manager.owner.controller;

import dev.sonu.saitring_employee_manager.common.exception.ErrorResponse;
import dev.sonu.saitring_employee_manager.owner.dto.OwnerResponse;
import dev.sonu.saitring_employee_manager.owner.dto.UpdateOwnerProfileRequest;
import dev.sonu.saitring_employee_manager.owner.service.OwnerService;
import dev.sonu.saitring_employee_manager.security.OwnerPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/owners")
@Tag(name = "Owner Profile", description = "Endpoints for authenticated owner tenants to manage their account profile and organization branding")
@SecurityRequirement(name = "bearerAuth")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get Authenticated Owner Profile",
            description = "Retrieves the profile details of the currently authenticated business owner tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OwnerResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OwnerResponse> getProfile(@AuthenticationPrincipal OwnerPrincipal principal) {
        OwnerResponse response = ownerService.getProfile(principal.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update Owner Profile with Logo (Multipart/Form-Data)",
            description = "Updates profile information and optionally replaces the organization logo in Supabase storage.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = OwnerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email or phone number conflict with existing tenant",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OwnerResponse> updateProfileMultipart(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Valid @ModelAttribute UpdateOwnerProfileRequest request,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        OwnerResponse response = ownerService.updateProfile(principal.getId(), request, logo);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update Owner Profile (JSON)",
            description = "Updates profile information without changing the organization logo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = OwnerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email or phone number conflict with existing tenant",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OwnerResponse> updateProfileJson(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Valid @RequestBody UpdateOwnerProfileRequest request) {
        OwnerResponse response = ownerService.updateProfile(principal.getId(), request, null);
        return ResponseEntity.ok(response);
    }
}
