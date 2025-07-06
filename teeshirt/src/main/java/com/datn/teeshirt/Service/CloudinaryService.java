package com.datn.teeshirt.Service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.datn.teeshirt.Utils.FileUploadValidator;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Upload file lên Cloudinary với validation
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        // Validation sử dụng FileUploadValidator
        FileUploadValidator.validateFile(file);

        try {
            // Sanitize filename
            String originalFilename = file.getOriginalFilename();
            String sanitizedFilename = FileUploadValidator.sanitizeFilename(originalFilename);
            String extension = getFileExtension(sanitizedFilename);
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Upload lên Cloudinary với security settings
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", folder + "/" + uniqueFilename,
                            "resource_type", "image",
                            "transformation", "f_auto,q_auto", // Auto format và quality
                            "access_mode", "authenticated", // Chỉ cho phép authenticated access
                            "invalidate", true // Invalidate cache
                    ));

            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new IOException("Lỗi khi upload file lên Cloudinary: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi không xác định khi upload file: " + e.getMessage());
        }
    }

    /**
     * Xóa file từ Cloudinary
     */
    public boolean deleteFile(String imageUrl) {
        try {
            // Extract public_id từ URL
            String publicId = extractPublicIdFromUrl(imageUrl);

            if (publicId != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> deleteResult = cloudinary.uploader().destroy(
                        publicId,
                        ObjectUtils.asMap("resource_type", "image"));

                return "ok".equals(deleteResult.get("result"));
            }

            return false;

        } catch (Exception e) {
            // Log error nhưng không throw exception để tránh crash
            System.err.println("Lỗi khi xóa file từ Cloudinary: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy extension từ filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Extract public_id từ Cloudinary URL
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            // Cloudinary URL format:
            // https://res.cloudinary.com/cloud_name/image/upload/v1234567890/folder/filename.jpg
            String[] parts = imageUrl.split("/upload/");
            if (parts.length == 2) {
                String afterUpload = parts[1];
                // Remove version if exists
                if (afterUpload.contains("/v")) {
                    afterUpload = afterUpload.substring(afterUpload.indexOf("/v") + 2);
                    afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
                }
                // Remove extension
                return afterUpload.substring(0, afterUpload.lastIndexOf("."));
            }
        } catch (Exception e) {
            // Log error
            System.err.println("Không thể extract public_id từ URL: " + imageUrl);
        }
        return null;
    }
}