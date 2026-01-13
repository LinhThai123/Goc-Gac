package com.ecommerce.gocgac.controller.storage;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.storage.FileUploadResponse;
import com.ecommerce.gocgac.service.storage.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Tag(name = "File Storage", description = "API upload và quản lý file với MINIO")
@SecurityRequirement(name = "bearerAuth")
public class FileUploadController {

    private final MinioService minioService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file đơn lẻ", 
               description = "Upload một file lên MINIO. Yêu cầu authentication từ Keycloak.")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        
        try {
            // Validate file
            if (file.isEmpty()) {
                MessageResponse response = new MessageResponse();
                response.setMessage("File không được để trống");
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return ResponseEntity.badRequest().body(response);
            }

            // Validate file size (max 10MB)
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxSize) {
                MessageResponse response = new MessageResponse();
                response.setMessage("File vượt quá kích thước cho phép (10MB)");
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return ResponseEntity.badRequest().body(response);
            }

            // Upload file
            String fileUrl = minioService.uploadFile(file, folder);

            // Create response
            FileUploadResponse uploadResponse = new FileUploadResponse();
            uploadResponse.setFileName(file.getOriginalFilename());
            uploadResponse.setFileUrl(fileUrl);
            uploadResponse.setFileSize(file.getSize());
            uploadResponse.setContentType(file.getContentType());
            uploadResponse.setFolder(folder);

            MessageResponse response = new MessageResponse();
            response.setMessage("Upload file thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(uploadResponse);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("❌ Error uploading file: {}", e.getMessage(), e);
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi upload file: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            log.error("❌ Unexpected error uploading file: {}", e.getMessage(), e);
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi không mong muốn: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload nhiều file", 
               description = "Upload nhiều file cùng lúc lên MINIO. Yêu cầu authentication từ Keycloak.")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        
        try {
            if (files == null || files.length == 0) {
                MessageResponse response = new MessageResponse();
                response.setMessage("Không có file nào được chọn");
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return ResponseEntity.badRequest().body(response);
            }

            List<FileUploadResponse> uploadResponses = new ArrayList<>();
            long maxSize = 10 * 1024 * 1024; // 10MB

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                // Validate file size
                if (file.getSize() > maxSize) {
                    log.warn("⚠️ File {} vượt quá kích thước cho phép, bỏ qua", file.getOriginalFilename());
                    continue;
                }

                try {
                    String fileUrl = minioService.uploadFile(file, folder);

                    FileUploadResponse uploadResponse = new FileUploadResponse();
                    uploadResponse.setFileName(file.getOriginalFilename());
                    uploadResponse.setFileUrl(fileUrl);
                    uploadResponse.setFileSize(file.getSize());
                    uploadResponse.setContentType(file.getContentType());
                    uploadResponse.setFolder(folder);

                    uploadResponses.add(uploadResponse);
                } catch (Exception e) {
                    log.error("❌ Error uploading file {}: {}", file.getOriginalFilename(), e.getMessage());
                }
            }

            MessageResponse response = new MessageResponse();
            response.setMessage("Đã upload " + uploadResponses.size() + "/" + files.length + " file thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(uploadResponses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Unexpected error uploading files: {}", e.getMessage(), e);
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi không mong muốn: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Xóa file", 
               description = "Xóa file từ MINIO. Yêu cầu authentication từ Keycloak.")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        try {
            minioService.deleteFile(fileUrl);

            MessageResponse response = new MessageResponse();
            response.setMessage("Xóa file thành công");
            response.setStatus(HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error deleting file: {}", e.getMessage(), e);
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi xóa file: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/presigned-url")
    @Operation(summary = "Lấy presigned URL", 
               description = "Lấy presigned URL để truy cập file với thời hạn. Yêu cầu authentication từ Keycloak.")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> getPresignedUrl(
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam(value = "expiryMinutes", defaultValue = "60") int expiryMinutes) {
        
        try {
            String presignedUrl = minioService.getPresignedUrl(
                    fileUrl, 
                    java.time.Duration.ofMinutes(expiryMinutes)
            );

            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy presigned URL thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(Map.of("presignedUrl", presignedUrl, "expiryMinutes", expiryMinutes));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error generating presigned URL: {}", e.getMessage(), e);
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi tạo presigned URL: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/check")
    @Operation(summary = "Kiểm tra file tồn tại", 
               description = "Kiểm tra file có tồn tại trong MINIO không. Yêu cầu authentication từ Keycloak.")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> checkFileExists(@RequestParam("fileUrl") String fileUrl) {
        try {
            boolean exists = minioService.fileExists(fileUrl);

            MessageResponse response = new MessageResponse();
            response.setMessage(exists ? "File tồn tại" : "File không tồn tại");
            response.setStatus(HttpStatus.OK.value());
            response.setData(Map.of("exists", exists));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error checking file existence: {}", e.getMessage(), e);
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi kiểm tra file: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

