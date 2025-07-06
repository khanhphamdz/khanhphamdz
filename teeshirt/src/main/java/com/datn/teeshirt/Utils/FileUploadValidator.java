package com.datn.teeshirt.Utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class FileUploadValidator {
    
    // Các constant cho validation
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    
    // Danh sách các file extension nguy hiểm
    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList(
        ".exe", ".bat", ".cmd", ".com", ".pif", ".scr", ".vbs", ".js", ".jar", ".war", ".ear"
    );
    
    /**
     * Validate file upload
     */
    public static void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File không được để trống");
        }
        
        // Kiểm tra kích thước file
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File quá lớn. Kích thước tối đa: " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }
        
        // Kiểm tra extension nguy hiểm
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && hasDangerousExtension(originalFilename)) {
            throw new IOException("Loại file không được phép upload vì lý do bảo mật");
        }
        
        // Kiểm tra MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IOException("Loại file không được hỗ trợ. Chỉ chấp nhận: JPG, PNG, GIF, WEBP");
        }
        
        // Kiểm tra extension
        if (originalFilename == null || !hasAllowedExtension(originalFilename)) {
            throw new IOException("Extension file không hợp lệ. Chỉ chấp nhận: .jpg, .jpeg, .png, .gif, .webp");
        }
        
        // Kiểm tra file có phải là ảnh thật không
        if (!isValidImageFile(file)) {
            throw new IOException("File không phải là ảnh hợp lệ");
        }
    }
    
    /**
     * Kiểm tra extension có được phép không
     */
    private static boolean hasAllowedExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
    
    /**
     * Kiểm tra extension nguy hiểm
     */
    private static boolean hasDangerousExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return DANGEROUS_EXTENSIONS.contains(extension);
    }
    
    /**
     * Lấy extension từ filename
     */
    private static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    /**
     * Kiểm tra file có phải là ảnh hợp lệ không bằng magic bytes
     */
    private static boolean isValidImageFile(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length < 4) {
                return false;
            }
            
            // JPEG: FF D8 FF
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) {
                return true;
            }
            
            // PNG: 89 50 4E 47
            if (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 && 
                bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47) {
                return true;
            }
            
            // GIF: 47 49 46 38 (GIF8)
            if (bytes[0] == (byte) 0x47 && bytes[1] == (byte) 0x49 && 
                bytes[2] == (byte) 0x46 && bytes[3] == (byte) 0x38) {
                return true;
            }
            
            // WEBP: RIFF....WEBP
            if (bytes.length >= 12 && 
                bytes[0] == (byte) 0x52 && bytes[1] == (byte) 0x49 && 
                bytes[2] == (byte) 0x46 && bytes[3] == (byte) 0x46 &&
                bytes[8] == (byte) 0x57 && bytes[9] == (byte) 0x45 && 
                bytes[10] == (byte) 0x42 && bytes[11] == (byte) 0x50) {
                return true;
            }
            
            return false;
            
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Sanitize filename để tránh path traversal attack
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        
        // Loại bỏ các ký tự nguy hiểm
        String sanitized = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // Loại bỏ path traversal
        sanitized = sanitized.replaceAll("\\.\\.", "_");
        
        // Giới hạn độ dài
        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }
        
        return sanitized;
    }
} 