package com.ecommerce.gocgac.service.affiliate;

import com.ecommerce.gocgac.dto.affiliate.ProductCommissionResponse;
import com.ecommerce.gocgac.dto.affiliate.SetProductCommissionRequest;
import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.ProductCommission;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.ProductCommissionRepository;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.service.store.StoreResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Người bán đặt mức hoa hồng tiếp thị liên kết cho từng sản phẩm (M17).
 */
@Service
@RequiredArgsConstructor
public class ProductCommissionService {

    private final ProductCommissionRepository productCommissionRepository;
    private final ProductRepository productRepository;
    private final StoreResolver storeResolver;

    @Transactional
    public ProductCommissionResponse setCommission(Long sellerUserId, SetProductCommissionRequest req) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        Product product = productRepository.findById(req.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
        if (!product.getStoreId().equals(storeId)) {
            throw new BusinessException("Sản phẩm không thuộc gian hàng của bạn");
        }
        ProductCommission pc = productCommissionRepository.findByProductId(req.getProductId())
            .orElseGet(ProductCommission::new);
        pc.setProductId(req.getProductId());
        pc.setStoreId(storeId);
        pc.setCommissionRate(req.getCommissionRate());
        pc.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        return ProductCommissionResponse.from(productCommissionRepository.save(pc));
    }

    public List<ProductCommissionResponse> list(Long sellerUserId) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        return productCommissionRepository.findByStoreId(storeId)
            .stream().map(ProductCommissionResponse::from).toList();
    }
}
