# Hướng dẫn cài đặt và sử dụng MINIO với Keycloak Authentication

## 📋 Tổng Quan

MINIO đã được tích hợp vào hệ thống GocGac để thực hiện việc upload và quản lý file. Tất cả các API upload file đều yêu cầu authentication từ Keycloak.

## 🚀 Cài Đặt

### 1. Khởi động MINIO với Docker Compose

MINIO đã được thêm vào `docker-compose.yml`. Có 2 cách khởi động:

#### Cách 1: Sử dụng Script Tự Động (Khuyến nghị)

**Windows:**
```powershell
.\start-minio.ps1
```

**Linux/Mac:**
```bash
chmod +x start-minio.sh
./start-minio.sh
```

Script sẽ tự động:
- Kiểm tra Docker đang chạy
- Kiểm tra port có bị chiếm không
- Dừng container cũ nếu có
- Khởi động MINIO mới

#### Cách 2: Khởi động Thủ Công

```bash
# Khởi động chỉ MINIO
docker-compose up -d minio

# Hoặc khởi động tất cả services
docker-compose up -d
```

**Kiểm tra MINIO đã chạy:**
```bash
docker ps | grep minio
docker logs gocgac-minio
```

MINIO sẽ chạy tại:
- **API Endpoint**: http://localhost:9000
- **Console**: http://localhost:9001

> **Lưu ý**: Nếu gặp lỗi khi khởi động, xem file `MINIO_TROUBLESHOOTING.md` để biết cách khắc phục.

### 2. Đăng nhập vào MINIO Console

1. Truy cập: http://localhost:9001
2. Đăng nhập với:
   - **Username**: `minioadmin` (mặc định)
   - **Password**: `minioadmin123` (mặc định)

> **Lưu ý**: Trong production, hãy thay đổi credentials trong file `.env`

### 3. Cấu hình MINIO trong Application

Cấu hình MINIO đã được thêm vào `application-dev.yml` và `application-prod.yml`:

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin123
  bucket-name: gocgac
  url: http://localhost:9000
```

Bucket `gocgac` sẽ được tự động tạo khi ứng dụng khởi động.

## 📡 API Endpoints

Tất cả các endpoint đều yêu cầu **Bearer Token** từ Keycloak trong header:

```
Authorization: Bearer <your-access-token>
```

### 1. Upload File Đơn Lẻ

**Endpoint**: `POST /api/storage/upload`

**Request**:
- Content-Type: `multipart/form-data`
- Body:
  - `file`: File cần upload (required)
  - `folder`: Thư mục lưu trữ (optional, default: "general")

**Response**:
```json
{
  "message": "Upload file thành công",
  "status": 200,
  "data": {
    "fileName": "example.jpg",
    "fileUrl": "http://localhost:9000/gocgac/products/uuid.jpg",
    "fileSize": 102400,
    "contentType": "image/jpeg",
    "folder": "products"
  }
}
```

**Ví dụ với cURL**:
```bash
curl -X POST http://localhost:8086/api/storage/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@/path/to/image.jpg" \
  -F "folder=products"
```

### 2. Upload Nhiều File

**Endpoint**: `POST /api/storage/upload/multiple`

**Request**:
- Content-Type: `multipart/form-data`
- Body:
  - `files`: Mảng các file (required)
  - `folder`: Thư mục lưu trữ (optional, default: "general")

**Response**:
```json
{
  "message": "Đã upload 3/3 file thành công",
  "status": 200,
  "data": [
    {
      "fileName": "image1.jpg",
      "fileUrl": "http://localhost:9000/gocgac/products/uuid1.jpg",
      "fileSize": 102400,
      "contentType": "image/jpeg",
      "folder": "products"
    },
    ...
  ]
}
```

### 3. Xóa File

**Endpoint**: `DELETE /api/storage/delete`

**Request**:
- Query Parameter: `fileUrl` (required)

**Response**:
```json
{
  "message": "Xóa file thành công",
  "status": 200
}
```

**Ví dụ**:
```bash
curl -X DELETE "http://localhost:8086/api/storage/delete?fileUrl=http://localhost:9000/gocgac/products/uuid.jpg" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. Lấy Presigned URL

**Endpoint**: `GET /api/storage/presigned-url`

**Request**:
- Query Parameters:
  - `fileUrl`: URL của file (required)
  - `expiryMinutes`: Thời hạn URL (optional, default: 60 phút)

**Response**:
```json
{
  "message": "Lấy presigned URL thành công",
  "status": 200,
  "data": {
    "presignedUrl": "http://localhost:9000/gocgac/products/uuid.jpg?X-Amz-Algorithm=...",
    "expiryMinutes": 60
  }
}
```

### 5. Kiểm Tra File Tồn Tại

**Endpoint**: `GET /api/storage/check`

**Request**:
- Query Parameter: `fileUrl` (required)

**Response**:
```json
{
  "message": "File tồn tại",
  "status": 200,
  "data": {
    "exists": true
  }
}
```

## 📁 Cấu Trúc Thư Mục

Các thư mục được khuyến nghị sử dụng:

- `products/` - Ảnh sản phẩm
- `users/` - Avatar người dùng
- `documents/` - Tài liệu, giấy tờ
- `cooperatives/` - Ảnh và tài liệu HTX
- `stores/` - Ảnh cửa hàng
- `general/` - File chung (mặc định)

## 🔒 Bảo Mật

1. **Authentication**: Tất cả endpoints yêu cầu JWT token từ Keycloak
2. **File Size Limit**: Mặc định giới hạn 10MB mỗi file
3. **Content Type**: Nên validate content type phù hợp với loại file

## 🛠️ Sử Dụng Trong Code

### Ví dụ: Upload ảnh sản phẩm

```java
@Autowired
private MinioService minioService;

public void uploadProductImage(MultipartFile image, Long productId) {
    try {
        String fileUrl = minioService.uploadFile(image, "products");
        // Lưu fileUrl vào database
        productImageRepository.save(new ProductImage(productId, fileUrl));
    } catch (IOException e) {
        log.error("Error uploading product image", e);
    }
}
```

### Ví dụ: Xóa file khi xóa sản phẩm

```java
public void deleteProduct(Long productId) {
    List<ProductImage> images = productImageRepository.findByProductId(productId);
    for (ProductImage image : images) {
        minioService.deleteFile(image.getImageUrl());
    }
    productRepository.deleteById(productId);
}
```

## 🔧 Cấu Hình Production

Trong production, cần cấu hình các biến môi trường:

```bash
MINIO_ENDPOINT=https://minio.yourdomain.com
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
MINIO_BUCKET_NAME=gocgac
MINIO_URL=https://minio.yourdomain.com
```

## 📝 Lưu Ý

1. **Bucket tự động tạo**: Bucket sẽ được tự động tạo khi ứng dụng khởi động
2. **File naming**: File được đặt tên tự động bằng UUID để tránh trùng lặp
3. **URL format**: URL có format: `{minio.url}/{bucket-name}/{folder}/{filename}`
4. **Presigned URL**: Sử dụng presigned URL để chia sẻ file với thời hạn nhất định

## 🐛 Troubleshooting

### Lỗi: "Failed to initialize MINIO Client"
- Kiểm tra MINIO đã khởi động chưa: `docker ps | grep minio`
- Kiểm tra endpoint và credentials trong `application.yml`

### Lỗi: "Bucket not found"
- Bucket sẽ được tự động tạo, nhưng nếu lỗi, kiểm tra quyền truy cập

### Lỗi: "File vượt quá kích thước cho phép"
- Mặc định giới hạn 10MB, có thể điều chỉnh trong `FileUploadController`

## 📚 Tài Liệu Tham Khảo

- [MINIO Documentation](https://min.io/docs/)
- [MINIO Java Client](https://min.io/docs/minio/linux/developers/java/API.html)

