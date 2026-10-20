package dev.sonu.saitring_employee_manager.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public class RegisterMultipartRequest {

    @Schema(description = "Owner registration details (JSON object)")
    private RegisterRequest request;

    @Schema(description = "Optional organization logo image file (PNG, JPG, WEBP, SVG)")
    private MultipartFile logo;

    public RegisterMultipartRequest() {
    }

    public RegisterRequest getRequest() {
        return request;
    }

    public void setRequest(RegisterRequest request) {
        this.request = request;
    }

    public MultipartFile getLogo() {
        return logo;
    }

    public void setLogo(MultipartFile logo) {
        this.logo = logo;
    }
}
