package com.ecommerce.gocgac.service.store;

import com.ecommerce.gocgac.entity.Cooperative;
import com.ecommerce.gocgac.entity.Store;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.entity.enums.UserType;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.CooperativeRepository;
import com.ecommerce.gocgac.repository.StoreRepository;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Phân giải gian hàng (store) từ người dùng đang đăng nhập.
 * Dùng chung cho các module phía người bán (voucher, promotion, đơn hàng...).
 */
@Service
@RequiredArgsConstructor
public class StoreResolver {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final CooperativeRepository cooperativeRepository;

    /**
     * Trả về storeId của người dùng.
     * - COOPERATIVE_MANAGER: store của HTX.
     * - SELLER: store cá nhân.
     */
    public Long resolveStoreId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        if (user.getUserType() == UserType.COOPERATIVE_MANAGER) {
            Cooperative cooperative = cooperativeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Bạn chưa có HTX"));
            return storeRepository.findByCooperativeId(cooperative.getId())
                .orElseThrow(() -> new BusinessException("HTX của bạn chưa có gian hàng"))
                .getId();
        } else if (user.getUserType() == UserType.SELLER) {
            return storeRepository.findBySellerId(userId)
                .orElseThrow(() -> new BusinessException("Bạn chưa có gian hàng"))
                .getId();
        }
        throw new BusinessException("Chỉ SELLER hoặc COOPERATIVE_MANAGER mới sở hữu gian hàng");
    }

    /**
     * Phân giải userId của chủ sở hữu gian hàng (để gán làm sellerId trong hội thoại).
     * - Gian hàng cá thể: trả về sellerId.
     * - Gian hàng HTX: trả về userId của người quản lý HTX.
     */
    public Long resolveStoreOwnerUserId(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("Gian hàng không tồn tại"));
        if (store.getSellerId() != null) {
            return store.getSellerId();
        }
        if (store.getCooperativeId() != null) {
            return cooperativeRepository.findById(store.getCooperativeId())
                .map(Cooperative::getUserId)
                .orElseThrow(() -> new BusinessException("HTX của gian hàng không tồn tại"));
        }
        throw new BusinessException("Gian hàng chưa có chủ sở hữu");
    }
}
