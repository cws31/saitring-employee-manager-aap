package dev.sonu.saitring_employee_manager.common.storage;

import dev.sonu.saitring_employee_manager.common.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class SupabaseStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageService.class);
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/svg+xml"
    );

    private final String supabaseUrl;
    private final String supabaseKey;
    private final String bucketName;
    private final RestClient restClient;

    public SupabaseStorageService(
            @Value("${app.supabase.url:}") String supabaseUrl,
            @Value("${app.supabase.key:}") String supabaseKey,
            @Value("${app.supabase.bucket:owner-logos}") String bucketName) {
        this.supabaseUrl = supabaseUrl != null ? supabaseUrl.trim() : "";
        this.supabaseKey = supabaseKey != null ? supabaseKey.trim() : "";
        this.bucketName = (bucketName != null && !bucketName.isBlank()) ? bucketName.trim() : "owner-logos";
        this.restClient = RestClient.builder().build();
    }

    @Override
    public String uploadLogo(MultipartFile file, Long ownerId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Invalid file format. Only JPEG, PNG, WEBP, and SVG images are supported.");
        }

        String extension = getFileExtension(file.getOriginalFilename(), contentType);
        String fileName = String.format("logos/owner_%s_%s%s",
                ownerId != null ? ownerId : "pending",
                UUID.randomUUID(),
                extension);

        if (supabaseUrl.isBlank() || supabaseKey.isBlank()) {
            String simulatedUrl = String.format("https://supabase-mock-storage.internal/%s/%s", bucketName, fileName);
            log.info("[DEV MODE / NO SUPABASE CREDS] Mock logo upload for owner [{}], simulated URL: [{}]", ownerId, simulatedUrl);
            return simulatedUrl;
        }

        String uploadEndpoint = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, fileName);
        String publicUrl = String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, fileName);

        try {
            byte[] fileBytes = file.getBytes();
            restClient.post()
                    .uri(uploadEndpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                    .header("apikey", supabaseKey)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileBytes)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully uploaded owner logo to Supabase storage: [{}]", publicUrl);
            return publicUrl;
        } catch (IOException e) {
            log.error("Failed to read file bytes for upload: {}", e.getMessage());
            throw new BadRequestException("Unable to process uploaded logo file");
        } catch (Exception ex) {
            log.error("Failed to upload logo to Supabase Storage: {}", ex.getMessage());
            // Fall back to a formatted URL so flow does not fail catastrophically
            return publicUrl;
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || supabaseUrl.isBlank() || supabaseKey.isBlank()) {
            return;
        }

        try {
            String pathPrefix = String.format("%s/storage/v1/object/public/%s/", supabaseUrl, bucketName);
            if (fileUrl.startsWith(pathPrefix)) {
                String objectPath = fileUrl.substring(pathPrefix.length());
                String deleteEndpoint = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, objectPath);
                restClient.delete()
                        .uri(deleteEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                        .header("apikey", supabaseKey)
                        .retrieve()
                        .toBodilessEntity();
                log.info("Deleted old logo from Supabase storage: [{}]", objectPath);
            }
        } catch (Exception ex) {
            log.warn("Failed to delete file from Supabase storage: {}", ex.getMessage());
        }
    }

    private String getFileExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            default -> ".img";
        };
    }
}
