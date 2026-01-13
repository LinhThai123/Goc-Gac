# Hướng dẫn Implementation Channel và Catalog cho HTX

## ✅ Đã hoàn thành

### 1. Database Migration (V7)
- ✅ Tạo bảng `channels` - Micro-site riêng cho shop
- ✅ Tạo bảng `channel_follows` - Social feature (follow channel)
- ✅ Tạo bảng `shop_catalogs` - Catalog riêng cho từng shop
- ✅ Tạo bảng `catalog_products` - Mapping products vào catalog
- ✅ Tạo triggers để tự động update `follower_count` và `product_count`

### 2. Entities
- ✅ `Channel.java` - Entity cho Channel
- ✅ `ChannelFollow.java` - Entity cho Channel Follow
- ✅ `ShopCatalog.java` - Entity cho ShopCatalog
- ✅ `CatalogProduct.java` - Entity cho CatalogProduct mapping

### 3. Repositories
- ✅ `ChannelRepository.java` - Repository cho Channel
- ✅ `ChannelFollowRepository.java` - Repository cho Channel Follow
- ✅ `ShopCatalogRepository.java` - Repository cho ShopCatalog
- ✅ `CatalogProductRepository.java` - Repository cho CatalogProduct

## 📋 Cần làm tiếp

### 4. Service Layer (Tiếp theo)

#### 4.1. ChannelService
Cần tạo service với các chức năng:
- `createChannel(Long storeId, CreateChannelRequest request)` - Tạo channel mới
- `getChannelBySlug(String slug)` - Lấy channel theo slug (public)
- `getChannelByStoreId(Long storeId)` - Lấy channel của store (multi-tenant)
- `updateChannel(Long channelId, Long storeId, UpdateChannelRequest request)` - Cập nhật channel (chỉ owner)
- `updateThemeSettings(Long channelId, Long storeId, ThemeSettings settings)` - Customize theme
- `followChannel(Long channelId, Long userId)` - Follow channel
- `unfollowChannel(Long channelId, Long userId)` - Unfollow channel
- `getChannelFollowers(Long channelId)` - Lấy danh sách followers
- `isUserFollowing(Long channelId, Long userId)` - Kiểm tra user đã follow chưa

#### 4.2. ShopCatalogService
Cần tạo service với các chức năng:
- `createCatalog(Long storeId, CreateCatalogRequest request)` - Tạo catalog mới
- `getCatalogsByStoreId(Long storeId)` - Lấy tất cả catalogs của store (multi-tenant)
- `getCatalogById(Long catalogId, Long storeId)` - Lấy catalog theo ID (multi-tenant)
- `updateCatalog(Long catalogId, Long storeId, UpdateCatalogRequest request)` - Cập nhật catalog
- `deleteCatalog(Long catalogId, Long storeId)` - Xóa catalog
- `addProductToCatalog(Long catalogId, Long storeId, Long productId)` - Thêm product vào catalog
- `removeProductFromCatalog(Long catalogId, Long storeId, Long productId)` - Xóa product khỏi catalog
- `validateDuplicateProduct(Long catalogId, Long productId)` - Validate duplicate
- `getProductsInCatalog(Long catalogId, Long storeId)` - Lấy products trong catalog

### 5. DTOs (Data Transfer Objects)

#### 5.1. Channel DTOs
- `CreateChannelRequest.java`
- `UpdateChannelRequest.java`
- `ChannelResponse.java`
- `ThemeSettings.java` (inner class hoặc separate)

#### 5.2. Catalog DTOs
- `CreateCatalogRequest.java`
- `UpdateCatalogRequest.java`
- `CatalogResponse.java`
- `AddProductToCatalogRequest.java`

### 6. Controllers

#### 6.1. ChannelController
Endpoints:
- `GET /api/channels/{slug}` - Public: Lấy channel theo slug
- `GET /api/channels/my-channel` - Protected: Lấy channel của mình
- `POST /api/channels` - Protected: Tạo channel mới
- `PUT /api/channels/{id}` - Protected: Cập nhật channel
- `PUT /api/channels/{id}/theme` - Protected: Customize theme
- `POST /api/channels/{id}/follow` - Protected: Follow channel
- `DELETE /api/channels/{id}/follow` - Protected: Unfollow channel
- `GET /api/channels/{id}/followers` - Protected: Lấy followers

#### 6.2. CatalogController
Endpoints:
- `GET /api/catalogs` - Protected: Lấy catalogs của mình
- `GET /api/catalogs/{id}` - Protected: Lấy catalog theo ID
- `POST /api/catalogs` - Protected: Tạo catalog mới
- `PUT /api/catalogs/{id}` - Protected: Cập nhật catalog
- `DELETE /api/catalogs/{id}` - Protected: Xóa catalog
- `POST /api/catalogs/{id}/products` - Protected: Thêm product vào catalog
- `DELETE /api/catalogs/{id}/products/{productId}` - Protected: Xóa product khỏi catalog
- `GET /api/catalogs/{id}/products` - Protected: Lấy products trong catalog

### 7. Multi-tenant Security

Cần đảm bảo:
- Shop chỉ thấy và chỉnh sửa channel/catalog của mình
- Validate `storeId` từ JWT token
- Sử dụng `@PreAuthorize` để kiểm tra quyền
- Validate ownership trước khi update/delete

### 8. Integration với Elasticsearch

- Index products theo channel
- Search trong channel riêng: `GET /api/channels/{slug}/search?q=keyword`
- Search toàn site với filter channel: `GET /api/products/search?q=keyword&channelId=xxx`

### 9. Product Mapping vào Global Categories

Khi shop upload product vào catalog:
- Product vẫn có `category_id` (danh mục chung của Gocgac)
- Product có `store_category_id` (danh mục riêng của store) - optional
- Product được thêm vào `catalog_products` với `catalog_id`

## 🏗️ Architecture

```
┌─────────────────┐
│   Channel       │ (1:1 với Store)
│   - slug        │
│   - banner      │
│   - theme       │
│   - story       │
└────────┬────────┘
         │
         │ 1:N
         ▼
┌─────────────────┐
│  ChannelFollow  │
│  - user_id      │
│  - channel_id   │
└─────────────────┘

┌─────────────────┐
│  ShopCatalog    │ (N:1 với Store - owner_id)
│  - catalog_code │
│  - description  │
│  - settings     │
└────────┬────────┘
         │
         │ 1:N (Many-to-Many với Product)
         ▼
┌─────────────────┐
│ CatalogProduct  │
│  - catalog_id   │
│  - product_id   │
│  - display_order│
└────────┬────────┘
         │
         │ N:1
         ▼
┌─────────────────┐
│    Product      │
│  - category_id  │ (Global category)
│  - store_id     │
└─────────────────┘
```

## 📝 Lưu ý

1. **Multi-tenant**: Luôn validate `storeId` từ JWT token
2. **Duplicate validation**: Kiểm tra product đã có trong catalog chưa trước khi thêm
3. **Theme settings**: Lưu dạng JSON string, parse khi sử dụng
4. **Slug uniqueness**: Đảm bảo channel slug là unique
5. **Catalog code**: Unique per store hoặc global (tùy requirement)
6. **Trigger updates**: `product_count` và `follower_count` được update tự động qua triggers

## 🚀 Next Steps

1. Tạo DTOs
2. Tạo Service layer
3. Tạo Controllers
4. Implement multi-tenant security
5. Test APIs
6. Integration với Elasticsearch

