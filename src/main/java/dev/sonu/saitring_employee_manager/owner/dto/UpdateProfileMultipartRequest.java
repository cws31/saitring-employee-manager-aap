package dev.sonu.saitring_employee_manager.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public class UpdateProfileMultipartRequest {

    @Schema(description = "Owner profile update details (JSON object)")
    private UpdateOwnerProfileRequest request;

    @Schema(description = "Optional organization logo image file (PNG, JPG, WEBP, SVG)")
    private MultipartFile logo;

    public UpdateProfileMultipartRequest() {
    }

    public UpdateOwnerProfileRequest getRequest() {
        return request;
    }

    public void setRequest(UpdateOwnerProfileRequest request) {
        this.request = request;
    }

    public MultipartFile getLogo() {
        return logo;
    }

    public void setLogo(MultipartFile logo) {
        this.logo = logo;
    }
}
