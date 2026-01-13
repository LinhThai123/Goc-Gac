# Hướng dẫn cài đặt Elasticsearch cho sàn Gốc Gác

## Tổng quan

Elasticsearch đã được tích hợp vào dự án để hỗ trợ tìm kiếm sản phẩm, danh mục và các tính năng search nâng cao.

## Cài đặt

### 1. Dependencies

Đã thêm Spring Data Elasticsearch vào `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

### 2. Docker Compose

Elasticsearch service đã được thêm vào `docker-compose.yml`:
- **Image**: `docker.elastic.co/elasticsearch/elasticsearch:8.15.0`
- **Ports**: 
  - `9200`: HTTP API
  - `9300`: Transport protocol
- **Memory**: 512MB heap size (có thể điều chỉnh)
- **Security**: Tắt xpack.security cho development (không khuyến nghị cho production)

### 3. Configuration

#### Development (`application-dev.yml`)
```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
    connection-timeout: 10s
    socket-timeout: 30s
```

#### Production (`application-prod.yml`)
```yaml
spring:
  elasticsearch:
    uris: ${ELASTICSEARCH_URIS}
    connection-timeout: 10s
    socket-timeout: 30s
```

### 4. Config Class

Đã tạo `ElasticsearchConfig.java` để enable Elasticsearch repositories:
- Package: `com.ecommerce.gocgac.config`
- Enable repositories tại: `com.ecommerce.gocgac.repository.elasticsearch`

## Cách sử dụng

### 1. Khởi động Elasticsearch

```bash
# Khởi động tất cả services (bao gồm Elasticsearch)
docker-compose up -d

# Hoặc chỉ khởi động Elasticsearch
docker-compose up -d elasticsearch
```

### 2. Kiểm tra Elasticsearch đang chạy

```bash
# Kiểm tra health
curl http://localhost:9200/_cluster/health

# Hoặc xem thông tin cluster
curl http://localhost:9200
```

### 3. Tạo Elasticsearch Document Entity

Để tìm kiếm sản phẩm, bạn cần tạo Elasticsearch document entity:

```java
package com.ecommerce.gocgac.entity.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
public class ProductDocument {
    
    @Id
    private Long id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String productName;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String slug;
    
    @Field(type = FieldType.Long)
    private Long categoryId;
    
    @Field(type = FieldType.Double)
    private Double price;
    
    // ... các trường khác
}
```

### 4. Tạo Elasticsearch Repository

```java
package com.ecommerce.gocgac.repository.elasticsearch;

import com.ecommerce.gocgac.entity.elasticsearch.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
    
    List<ProductDocument> findByProductNameContaining(String keyword);
    
    List<ProductDocument> findByDescriptionContaining(String keyword);
}
```

### 5. Sync dữ liệu từ Database sang Elasticsearch

Tạo service để sync dữ liệu:

```java
@Service
@RequiredArgsConstructor
public class ElasticsearchSyncService {
    
    private final ProductRepository productRepository;
    private final ProductElasticsearchRepository productElasticsearchRepository;
    
    @Transactional
    public void syncProductsToElasticsearch() {
        List<Product> products = productRepository.findAll();
        List<ProductDocument> documents = products.stream()
            .map(this::convertToDocument)
            .collect(Collectors.toList());
        
        productElasticsearchRepository.saveAll(documents);
    }
    
    private ProductDocument convertToDocument(Product product) {
        // Convert logic
    }
}
```

## Lưu ý

1. **Memory**: Elasticsearch mặc định sử dụng 512MB heap. Nếu có nhiều dữ liệu, nên tăng lên 1GB hoặc 2GB.

2. **Security**: Trong production, nên bật xpack.security và cấu hình authentication.

3. **Data Persistence**: Dữ liệu được lưu trong Docker volume `elasticsearch_data`.

4. **Index Management**: Nên tạo index template và mapping trước khi index dữ liệu.

5. **Sync Strategy**: 
   - Có thể sync toàn bộ dữ liệu khi khởi động ứng dụng
   - Hoặc sync real-time khi có thay đổi (sử dụng event listener)

## Environment Variables

Các biến môi trường Elasticsearch đã được thêm vào file `env.example`:

```env
ELASTICSEARCH_PORT=9200
ELASTICSEARCH_TRANSPORT_PORT=9300
ELASTICSEARCH_URIS=http://localhost:9200
```

**Lưu ý**: Copy file `env.example` thành `.env` và cập nhật các giá trị thực tế:

```bash
cp env.example .env
```

File `.env` đã được ignore trong `.gitignore` để bảo mật.

## Troubleshooting

1. **Elasticsearch không start**: Kiểm tra memory và disk space
2. **Connection refused**: Đảm bảo Elasticsearch đã start và port đúng
3. **Index not found**: Cần tạo index trước khi index dữ liệu

## Tài liệu tham khảo

- [Spring Data Elasticsearch Documentation](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/)
- [Elasticsearch Official Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)

