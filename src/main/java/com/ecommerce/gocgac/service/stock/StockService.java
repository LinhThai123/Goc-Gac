package com.ecommerce.gocgac.service.stock;

import com.ecommerce.gocgac.entity.ProductVariant;
import com.ecommerce.gocgac.entity.StockTransaction;
import com.ecommerce.gocgac.entity.enums.StockTransactionType;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.ProductVariantRepository;
import com.ecommerce.gocgac.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dịch vụ quản lý tồn kho ở cấp SKU (M15).
 *
 * <p>Mô hình giữ tồn:
 * <ul>
 *   <li>{@link #reserve} — khi đặt hàng: tăng {@code reservedQuantity} (available giảm).</li>
 *   <li>{@link #release} — khi hủy đơn (chưa giao): giảm {@code reservedQuantity} (available tăng lại).</li>
 *   <li>{@link #commit} — khi giao thành công: giảm cả {@code stockQuantity} và {@code reservedQuantity}.</li>
 * </ul>
 * {@code availableQuantity} được entity tự tính lại trong {@code @PreUpdate}.
 *
 * <p>Dùng khóa ghi bi quan ({@code findByIdForUpdate}) để chống oversell khi đặt đồng thời.
 * Mọi phương thức phải chạy trong transaction của lời gọi (checkout/cancel...).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductVariantRepository variantRepository;
    private final StockTransactionRepository stockTransactionRepository;

    private static final String REF_ORDER = "ORDER";

    /** Giữ tồn cho đơn hàng. Ném {@link BusinessException} nếu không đủ tồn khả dụng. */
    @Transactional
    public void reserve(Long variantId, int quantity, Long orderId, Long userId) {
        ProductVariant v = lockVariant(variantId);
        int available = nz(v.getAvailableQuantity());
        if (quantity > available) {
            throw new BusinessException("Không đủ tồn kho cho SKU " + v.getSku() + ". Còn lại: " + available);
        }
        v.setReservedQuantity(nz(v.getReservedQuantity()) + quantity);
        variantRepository.save(v);
        record(v, quantity, StockTransactionType.OUT, orderId, userId, "Giữ tồn khi đặt hàng");
        log.debug("Reserved {} of variant {} for order {}", quantity, variantId, orderId);
    }

    /** Hoàn tồn khi hủy đơn (chưa giao). */
    @Transactional
    public void release(Long variantId, int quantity, Long orderId, Long userId) {
        ProductVariant v = lockVariant(variantId);
        v.setReservedQuantity(Math.max(0, nz(v.getReservedQuantity()) - quantity));
        variantRepository.save(v);
        record(v, quantity, StockTransactionType.IN, orderId, userId, "Hoàn tồn do hủy đơn");
        log.debug("Released {} of variant {} for order {}", quantity, variantId, orderId);
    }

    /** Chốt xuất kho khi giao thành công: trừ tồn thật và giảm phần đã giữ. */
    @Transactional
    public void commit(Long variantId, int quantity, Long orderId, Long userId) {
        ProductVariant v = lockVariant(variantId);
        v.setStockQuantity(Math.max(0, nz(v.getStockQuantity()) - quantity));
        v.setReservedQuantity(Math.max(0, nz(v.getReservedQuantity()) - quantity));
        variantRepository.save(v);
        log.debug("Committed {} of variant {} for order {}", quantity, variantId, orderId);
    }

    /** Hoàn kho khi khách trả hàng (đơn đã giao): cộng lại tồn thật. */
    @Transactional
    public void restock(Long variantId, int quantity, Long orderId, Long userId) {
        ProductVariant v = lockVariant(variantId);
        v.setStockQuantity(nz(v.getStockQuantity()) + quantity);
        variantRepository.save(v);
        record(v, quantity, StockTransactionType.IN, orderId, userId, "Hoàn kho do trả hàng");
        log.debug("Restocked {} of variant {} for order {}", quantity, variantId, orderId);
    }

    private ProductVariant lockVariant(Long variantId) {
        return variantRepository.findByIdForUpdate(variantId)
            .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm (SKU) không tồn tại"));
    }

    private void record(ProductVariant v, int qty, StockTransactionType type,
                        Long orderId, Long userId, String note) {
        StockTransaction txn = new StockTransaction();
        txn.setProductId(v.getProductId());
        txn.setVariantId(v.getId());
        txn.setTransactionType(type);
        txn.setQuantity(qty);
        txn.setReferenceType(REF_ORDER);
        txn.setReferenceId(orderId);
        txn.setNote(note);
        txn.setCreatedBy(userId);
        stockTransactionRepository.save(txn);
    }

    private int nz(Integer value) {
        return value != null ? value : 0;
    }
}
