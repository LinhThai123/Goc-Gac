package com.ecommerce.gocgac.service.storage;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name:gocgac}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * Khởi tạo bucket nếu chưa tồn tại
     * Sử dụng @PostConstruct để đảm bảo @Value đã được inject
     * Không throw exception để ứng dụng vẫn có thể khởi động được khi MINIO chưa sẵn sàng
     */
    @PostConstruct
    public void initializeBucket() {
        try {
            // Validate bucket name
            if (bucketName == null || bucketName.trim().isEmpty()) {
                log.warn("⚠️ MINIO bucket name is not configured. Please check application.yml");
                return;
            }

            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("✅ Created bucket: {}", bucketName);
            } else {
                log.info("✅ Bucket already exists: {}", bucketName);
            }
        } catch (io.minio.errors.ErrorResponseException e) {
            log.error("❌ MINIO Error Response: {}. Please check:", e.getMessage());
            log.error("   - MINIO is running: docker ps | grep minio");
            log.error("   - MINIO endpoint: {}", minioUrl);
            log.error("   - MINIO credentials in application.yml");
            log.warn("⚠️ Application will continue to start, but file upload features may not work until MINIO is available.");
        } catch (java.net.ConnectException | java.net.UnknownHostException e) {
            log.error("❌ Cannot connect to MINIO at {}. Please ensure MINIO is running:", minioUrl);
            log.error("   - Start MINIO: docker-compose up -d minio");
            log.warn("⚠️ Application will continue to start, but file upload features may not work until MINIO is available.");
        } catch (Exception e) {
            log.error("❌ Error initializing MINIO bucket: {}", e.getMessage());
            log.error("   Error type: {}", e.getClass().getSimpleName());
            log.warn("⚠️ Application will continue to start, but file upload features may not work until MINIO is available.");
        }
    }

    /**
     * Upload file lên MINIO
     * 
     * @param file File cần upload
     * @param folder Thư mục lưu trữ (ví dụ: "products", "users", "documents")
     * @return URL của file đã upload
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File không được để trống");
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = folder + "/" + UUID.randomUUID() + extension;

            // Upload file
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // Return file URL
            String fileUrl = minioUrl + "/" + bucketName + "/" + fileName;
            log.info("✅ File uploaded successfully: {}", fileUrl);
            return fileUrl;

        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("❌ Error uploading file to MINIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to MINIO", e);
        }
    }

    /**
     * Upload file với custom filename
     */
    public String uploadFile(MultipartFile file, String folder, String customFileName) throws IOException {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File không được để trống");
            }

            String fileName = folder + "/" + customFileName;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String fileUrl = minioUrl + "/" + bucketName + "/" + fileName;
            log.info("✅ File uploaded successfully: {}", fileUrl);
            return fileUrl;

        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("❌ Error uploading file to MINIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to MINIO", e);
        }
    }

    /**
     * Download file từ MINIO
     */
    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            log.error("❌ Error downloading file from MINIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file from MINIO", e);
        }
    }

    /**
     * Xóa file từ MINIO
     */
    public void deleteFile(String fileName) {
        try {
            // Extract object name from URL if full URL is provided
            String objectName = fileName;
            if (fileName.contains(bucketName + "/")) {
                objectName = fileName.substring(fileName.indexOf(bucketName + "/") + bucketName.length() + 1);
            }

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("✅ File deleted successfully: {}", objectName);
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            log.error("❌ Error deleting file from MINIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from MINIO", e);
        }
    }

    /**
     * Tạo presigned URL để truy cập file (có thời hạn)
     */
    public String getPresignedUrl(String fileName, Duration duration) {
        try {
            // Extract object name from URL if full URL is provided
            String objectName = fileName;
            if (fileName.contains(bucketName + "/")) {
                objectName = fileName.substring(fileName.indexOf(bucketName + "/") + bucketName.length() + 1);
            }

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry((int) duration.getSeconds())
                            .build()
            );
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            log.error("❌ Error generating presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    /**
     * Kiểm tra file có tồn tại không
     */
    public boolean fileExists(String fileName) {
        try {
            // Extract object name from URL if full URL is provided
            String objectName = fileName;
            if (fileName.contains(bucketName + "/")) {
                objectName = fileName.substring(fileName.indexOf(bucketName + "/") + bucketName.length() + 1);
            }

            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            return false;
        }
    }
}

