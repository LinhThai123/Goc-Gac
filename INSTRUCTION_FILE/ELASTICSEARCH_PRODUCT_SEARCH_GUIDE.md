# Hướng dẫn sử dụng Elasticsearch cho tìm kiếm sản phẩm

## Mục lục
1. [Giới thiệu](#giới-thiệu)
2. [Cài đặt và cấu hình](#cài-đặt-và-cấu-hình)
3. [Kiến trúc hệ thống](#kiến-trúc-hệ-thống)
4. [API Endpoints](#api-endpoints)
5. [Ví dụ sử dụng](#ví-dụ-sử-dụng)
6. [Re-index dữ liệu](#re-index-dữ-liệu)
7. [Troubleshooting](#troubleshooting)
8. [Best Practices](#best-practices)

---

## Giới thiệu

Hệ thống Elasticsearch được tích hợp để cung cấp khả năng tìm kiếm sản phẩm mạnh mẽ với các tính năng:

- ✅ **Full-text search**: Tìm kiếm trong tên sản phẩm, mô tả, mã sản phẩm, SKU codes
- ✅ **Advanced filtering**: Lọc theo category, store, price range, sizes, colors, materials, và nhiều tiêu chí khác
- ✅ **Smart sorting**: Sắp xếp theo relevance, price, rating, sold count, created date
- ✅ **Highlighting**: Highlight keyword trong kết quả tìm kiếm
- ✅ **Relevance scoring**: Tính điểm relevance dựa trên rating, sold count, view count, verified status
- ✅ **Auto-indexing**: Tự động index khi tạo/cập nhật/xóa sản phẩm

---

## Cài đặt và cấu hình

### 1. Cài đặt Elasticsearch

#### Option 1: Sử dụng Docker (Khuyến nghị)

```bash
# Chạy Elasticsearch với Docker
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

#### Option 2: Cài đặt trực tiếp

Tải và cài đặt Elasticsearch từ [https://www.elastic.co/downloads/elasticsearch](https://www.elastic.co/downloads/elasticsearch)

### 2. Kiểm tra Elasticsearch đang chạy

```bash
# Kiểm tra health
curl http://localhost:9200

# Kết quả mong đợi:
# {
#   "name" : "...",
#   "cluster_name" : "elasticsearch",
#   "cluster_uuid" : "...",
#   "version" : { ... }
# }
```

### 3. Cấu hình trong application.yml

File `application-dev.yml` đã được cấu hình sẵn:

```yaml
spring:
  elasticsearch:
    uris: ${ELASTICSEARCH_URIS:http://localhost:9200}
    connection-timeout: 10s
    socket-timeout: 30s
```

Bạn có thể override bằng environment variable:

```bash
export ELASTICSEARCH_URIS=http://your-elasticsearch-host:9200
```

---

## Kiến trúc hệ thống

### 1. Components

```
┌─────────────────┐
│ ProductService  │ ──► Auto-index khi CRUD
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ProductSearch    │ ──► Index, Search, Re-index
│Service          │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ProductSearch    │ ──► Elasticsearch Repository
│Repository       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Elasticsearch  │
│   (Index:       │
│   products)     │
└─────────────────┘
```

### 2. Data Flow

1. **Create/Update Product** → `ProductService` → Auto-index vào Elasticsearch
2. **Delete Product** → `ProductService` → Auto-delete từ Elasticsearch
3. **Search Request** → `ProductSearchController` → `ProductSearchService` → Elasticsearch → Results

### 3. ProductDocument Structure

Document được lưu trong Elasticsearch với cấu trúc:

```json
{
  "id": 1,
  "productName": "Áo thun nam",
  "description": "...",
  "minPrice": 100000,
  "maxPrice": 200000,
  "sizes": ["M", "L", "XL"],
  "colors": ["Đỏ", "Xanh"],
  "materials": ["Cotton"],
  "relevanceScore": 8.5,
  ...
}
```

---

## API Endpoints

### 1. Search Products (POST)

**Endpoint**: `POST /api/products/search`

**Authentication**: Không cần (Public endpoint)

**Request Body**:

```json
{
  "keyword": "áo thun",
  "categoryId": 5,
  "storeId": 1,
  "productType": "PHYSICAL",
  "minPrice": 100000,
  "maxPrice": 500000,
  "sizes": ["M", "L"],
  "colors": ["Đỏ", "Xanh"],
  "materials": ["Cotton"],
  "ocopCertified": true,
  "isVerified": true,
  "hasOriginTracking": false,
  "hasVariants": true,
  "isCombo": false,
  "sortBy": "price_asc",
  "sortDir": "ASC",
  "page": 0,
  "size": 20,
  "highlight": true
}
```

**Response**:

```json
{
  "message": "Tìm kiếm thành công",
  "status": 200,
  "data": {
    "products": [
      {
        "id": 1,
        "productName": "Áo thun nam",
        "description": "...",
        "minPrice": 150000,
        "maxPrice": 200000,
        ...
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "currentPage": 0,
    "pageSize": 20,
    "keyword": "áo thun",
    "searchTime": 45,
    "highlights": {
      "1": {
        "productName": "<em>Áo thun</em> nam",
        "description": "..."
      }
    },
    "facets": {
      "categories": {},
      "stores": {},
      "sizes": {},
      "colors": {},
      "materials": {},
      "priceRange": {
        "min": 100000,
        "max": 500000
      }
    }
  }
}
```

### 2. Search Products (GET)

**Endpoint**: `GET /api/products/search`

**Authentication**: Không cần (Public endpoint)

**Query Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| keyword | String | No | Keyword để tìm kiếm |
| storeId | Long | No | Filter theo store |
| categoryId | Long | No | Filter theo category |
| storeCategoryId | Long | No | Filter theo store category |
| productType | String | No | PHYSICAL, DIGITAL, SERVICE, VOUCHER |
| status | String | No | ACTIVE, INACTIVE, OUT_OF_STOCK, DELETED |
| minPrice | BigDecimal | No | Giá tối thiểu |
| maxPrice | BigDecimal | No | Giá tối đa |
| sizes | List<String> | No | Filter theo sizes (M, L, XL...) |
| colors | List<String> | No | Filter theo colors |
| materials | List<String> | No | Filter theo materials |
| ocopCertified | Boolean | No | Filter theo OCOP certified |
| isVerified | Boolean | No | Filter theo verified |
| hasOriginTracking | Boolean | No | Filter theo origin tracking |
| hasVariants | Boolean | No | Filter theo has variants |
| isCombo | Boolean | No | Filter theo combo |
| sortBy | String | No | relevance, price_asc, price_desc, rating, sold_count, created_at |
| sortDir | String | No | ASC, DESC |
| page | int | No | Page number (default: 0) |
| size | int | No | Page size (default: 20) |
| highlight | Boolean | No | Enable highlighting (default: true) |

**Example**:

```bash
GET /api/products/search?keyword=áo thun&categoryId=5&minPrice=100000&maxPrice=500000&sortBy=price_asc&page=0&size=20
```

### 3. Re-index All Products

**Endpoint**: `POST /api/products/search/reindex`

**Authentication**: Required (SUPER_ADMIN only)

**Request**: No body

**Response**:

```json
{
  "message": "Re-index tất cả sản phẩm thành công",
  "status": 200
}
```

**Lưu ý**: 
- Chỉ SUPER_ADMIN mới có thể thực hiện
- Quá trình này có thể mất thời gian nếu có nhiều sản phẩm
- Nên chạy vào giờ thấp điểm

### 4. Index Single Product

**Endpoint**: `POST /api/products/search/index/{productId}`

**Authentication**: Required (COOPERATIVE_MANAGER, SELLER, SUPER_ADMIN)

**Path Parameters**:
- `productId`: ID của sản phẩm cần index

**Response**:

```json
{
  "message": "Index sản phẩm thành công",
  "status": 200
}
```

---

## Ví dụ sử dụng

### 1. Tìm kiếm đơn giản

```bash
curl -X POST http://localhost:8086/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "áo thun",
    "page": 0,
    "size": 20
  }'
```

### 2. Tìm kiếm với filters

```bash
curl -X POST http://localhost:8086/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "áo",
    "categoryId": 5,
    "minPrice": 100000,
    "maxPrice": 500000,
    "sizes": ["M", "L"],
    "colors": ["Đỏ", "Xanh"],
    "ocopCertified": true,
    "sortBy": "price_asc",
    "page": 0,
    "size": 20
  }'
```

### 3. Tìm kiếm theo store

```bash
curl -X GET "http://localhost:8086/api/products/search?storeId=1&sortBy=sold_count&sortDir=DESC&page=0&size=10"
```

### 4. Tìm kiếm sản phẩm OCOP

```bash
curl -X POST http://localhost:8086/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "ocopCertified": true,
    "isVerified": true,
    "sortBy": "rating",
    "sortDir": "DESC",
    "page": 0,
    "size": 20
  }'
```

### 5. Re-index tất cả sản phẩm

```bash
curl -X POST http://localhost:8086/api/products/search/reindex \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 6. Index một sản phẩm cụ thể

```bash
curl -X POST http://localhost:8086/api/products/search/index/123 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Re-index dữ liệu

### Khi nào cần re-index?

1. **Lần đầu setup**: Sau khi cài đặt Elasticsearch, cần re-index tất cả sản phẩm
2. **Sau khi thay đổi mapping**: Nếu thay đổi cấu trúc ProductDocument
3. **Sau khi restore database**: Nếu restore database từ backup
4. **Sau khi có lỗi index**: Nếu có sản phẩm không được index đúng

### Cách re-index

#### Option 1: Sử dụng API (Khuyến nghị)

```bash
POST /api/products/search/reindex
```

#### Option 2: Re-index từng sản phẩm

```bash
# Lấy danh sách tất cả product IDs từ database
# Sau đó gọi API index cho từng sản phẩm
POST /api/products/search/index/{productId}
```

### Monitoring re-index process

Kiểm tra logs để theo dõi quá trình re-index:

```bash
# Xem logs
tail -f logs/gocgac-dev.log | grep "Indexed\|Re-index"
```

---

## Troubleshooting

### 1. Elasticsearch không kết nối được

**Lỗi**: `Connection refused` hoặc `NoNodeAvailableException`

**Giải pháp**:
- Kiểm tra Elasticsearch đang chạy: `curl http://localhost:9200`
- Kiểm tra cấu hình `spring.elasticsearch.uris` trong `application.yml`
- Kiểm tra firewall/network

### 2. Index không tồn tại

**Lỗi**: `IndexNotFoundException`

**Giải pháp**:
- Chạy re-index: `POST /api/products/search/reindex`
- Index sẽ được tạo tự động khi lưu document đầu tiên

### 3. Search không trả về kết quả

**Nguyên nhân có thể**:
- Sản phẩm chưa được index
- Sản phẩm không đáp ứng điều kiện (chưa APPROVED hoặc không ACTIVE)
- Keyword không match

**Giải pháp**:
- Kiểm tra sản phẩm đã được index: `GET http://localhost:9200/products/_doc/{productId}`
- Re-index sản phẩm: `POST /api/products/search/index/{productId}`
- Kiểm tra approval status và status của sản phẩm

### 4. Performance chậm

**Nguyên nhân**:
- Quá nhiều documents trong index
- Query quá phức tạp
- Elasticsearch thiếu resources

**Giải pháp**:
- Tối ưu query (giảm số filters)
- Tăng resources cho Elasticsearch
- Sử dụng pagination hợp lý
- Cân nhắc sử dụng Elasticsearch cluster

### 5. Auto-index không hoạt động

**Nguyên nhân**:
- ProductSearchService không được inject vào ProductService
- Exception trong quá trình index (nhưng không throw để không ảnh hưởng flow chính)

**Giải pháp**:
- Kiểm tra logs để xem lỗi cụ thể
- Index thủ công: `POST /api/products/search/index/{productId}`
- Kiểm tra Elasticsearch connection

---

## Best Practices

### 1. Index Management

- ✅ **Re-index định kỳ**: Nên re-index sau khi có thay đổi lớn về dữ liệu
- ✅ **Monitor index size**: Theo dõi kích thước index để tránh quá tải
- ✅ **Backup index**: Backup Elasticsearch index định kỳ

### 2. Search Optimization

- ✅ **Sử dụng pagination**: Luôn sử dụng pagination, không lấy tất cả kết quả
- ✅ **Limit filters**: Không sử dụng quá nhiều filters cùng lúc
- ✅ **Cache results**: Cân nhắc cache kết quả search cho các query phổ biến
- ✅ **Use appropriate sort**: Sử dụng sort phù hợp với use case

### 3. Data Consistency

- ✅ **Auto-index**: Hệ thống tự động index khi CRUD, nhưng nên monitor
- ✅ **Manual re-index**: Re-index thủ công sau khi có thay đổi lớn
- ✅ **Verify data**: Định kỳ verify dữ liệu trong Elasticsearch khớp với database

### 4. Performance

- ✅ **Elasticsearch cluster**: Sử dụng cluster cho production
- ✅ **Index settings**: Tối ưu index settings (shards, replicas)
- ✅ **Query optimization**: Tối ưu query để giảm thời gian search
- ✅ **Monitoring**: Monitor performance metrics (search time, index size, etc.)

### 5. Security

- ✅ **Access control**: Chỉ cho phép SUPER_ADMIN re-index
- ✅ **Input validation**: Validate input trong SearchRequest
- ✅ **Rate limiting**: Cân nhắc rate limiting cho search endpoints
- ✅ **Elasticsearch security**: Enable Elasticsearch security trong production

---

## Advanced Configuration

### 1. Vietnamese Analyzer (Optional)

Để cải thiện search cho tiếng Việt, có thể cấu hình Vietnamese analyzer:

```json
PUT /products
{
  "settings": {
    "analysis": {
      "analyzer": {
        "vi_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": [
            "lowercase",
            "asciifolding"
          ]
        }
      }
    }
  }
}
```

### 2. Custom Mapping

Có thể tùy chỉnh mapping trong `ProductDocument`:

```java
@Field(type = FieldType.Text, analyzer = "vi_analyzer", searchAnalyzer = "vi_analyzer")
private String productName;
```

### 3. Index Settings

Cấu hình index settings cho production:

```json
PUT /products/_settings
{
  "index": {
    "number_of_shards": 3,
    "number_of_replicas": 1,
    "refresh_interval": "5s"
  }
}
```

---

## Monitoring và Logging

### 1. Check Index Status

```bash
# Kiểm tra index tồn tại
curl http://localhost:9200/_cat/indices/products

# Kiểm tra số documents
curl http://localhost:9200/products/_count

# Kiểm tra mapping
curl http://localhost:9200/products/_mapping
```

### 2. Search Performance

Monitor search time trong response:

```json
{
  "searchTime": 45  // milliseconds
}
```

### 3. Application Logs

Xem logs trong file: `logs/gocgac-dev.log`

Tìm các log liên quan:
- `Indexed product` - Khi index thành công
- `Failed to index` - Khi index thất bại
- `Re-index completed` - Khi re-index hoàn tất

---

## FAQ

### Q: Có cần re-index sau mỗi lần update sản phẩm không?

**A**: Không, hệ thống tự động re-index khi update. Chỉ cần re-index thủ công nếu có vấn đề.

### Q: Search có tìm được sản phẩm chưa được duyệt không?

**A**: Không, chỉ tìm được sản phẩm đã APPROVED và ACTIVE.

### Q: Có thể search theo SKU code không?

**A**: Có, sử dụng keyword search, nó sẽ tìm trong cả SKU codes.

### Q: Làm sao để tìm sản phẩm theo nhiều categories?

**A**: Hiện tại chỉ hỗ trợ 1 categoryId. Có thể mở rộng để hỗ trợ multiple categories.

### Q: Elasticsearch có ảnh hưởng đến performance của database không?

**A**: Không, Elasticsearch hoạt động độc lập. Chỉ ảnh hưởng khi index (async, không block).

---

## Support

Nếu gặp vấn đề, vui lòng:
1. Kiểm tra logs: `logs/gocgac-dev.log`
2. Kiểm tra Elasticsearch health: `curl http://localhost:9200/_cluster/health`
3. Kiểm tra index: `curl http://localhost:9200/products/_stats`
4. Liên hệ team để được hỗ trợ

---

**Last Updated**: 2024-01-XX  
**Version**: 1.0.0

