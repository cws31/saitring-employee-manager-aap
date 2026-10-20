package dev.sonu.saitring_employee_manager.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadLogo(MultipartFile file, Long ownerId);
    void deleteFile(String fileUrl);
}
