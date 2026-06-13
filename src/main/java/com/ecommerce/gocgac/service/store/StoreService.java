package com.ecommerce.gocgac.service.store;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.store.CreateStoreRequest;
import com.ecommerce.gocgac.dto.store.UpdateStoreRequest;
import com.ecommerce.gocgac.entity.Store;
import com.ecommerce.gocgac.entity.enums.StoreStatus;
import com.ecommerce.gocgac.exception.BadRequestException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.StoreRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

    public List<Store> getStores() {
        return storeRepository.findAll();
    }

    public Store getStoreById(Long id) {
        return storeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gian hàng"));
    }

    public Store getStoreByCooperativeId(Long cooperativeId) {
        return storeRepository.findByCooperativeId(cooperativeId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gian hàng theo hợp tác xã"));
    }

    public Store getStoreBySellerId(Long sellerId) {
        return storeRepository.findBySellerId(sellerId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gian hàng theo người bán"));
    }

    /**
     * Tạo gian hàng cho người bán cá thể (SELLER). Mỗi seller chỉ có 1 gian hàng.
     */
    @Transactional
    public Store createStore(Long sellerId, CreateStoreRequest request) {
        if (storeRepository.findBySellerId(sellerId).isPresent()) {
            throw new BadRequestException("Bạn đã có gian hàng. Mỗi người bán chỉ sở hữu một gian hàng.");
        }

        String storeCode = request.getStoreCode();
        if (!StringUtils.hasText(storeCode)) {
            storeCode = generateStoreCode();
        } else if (storeRepository.existsByStoreCode(storeCode)) {
            throw new BadRequestException("Mã gian hàng đã tồn tại: " + storeCode);
        }

        Store store = new Store();
        store.setSellerId(sellerId);
        store.setStoreName(XssSanitizer.sanitize(request.getStoreName()));
        store.setStoreCode(storeCode);
        store.setDescription(XssSanitizer.sanitizeHtml(request.getDescription()));
        store.setLogoUrl(request.getLogoUrl());
        store.setBannerUrl(request.getBannerUrl());
        store.setVideoUrl(request.getVideoUrl());
        store.setContactPhone(request.getContactPhone());
        store.setContactEmail(request.getContactEmail());
        store.setAddress(XssSanitizer.sanitize(request.getAddress()));
        store.setStatus(StoreStatus.ACTIVE);

        store = storeRepository.save(store);
        log.info("Store {} ({}) created by seller {}", store.getId(), storeCode, sellerId);
        return store;
    }

    /**
     * Cập nhật gian hàng — chỉ chủ sở hữu (seller) mới được phép.
     */
    @Transactional
    public Store updateStore(Long sellerId, Long storeId, UpdateStoreRequest request) {
        Store store = getStoreById(storeId);

        if (store.getSellerId() == null || !store.getSellerId().equals(sellerId)) {
            throw new BadRequestException("Bạn không có quyền cập nhật gian hàng này");
        }

        if (StringUtils.hasText(request.getStoreName())) {
            store.setStoreName(XssSanitizer.sanitize(request.getStoreName()));
        }
        if (request.getDescription() != null) {
            store.setDescription(XssSanitizer.sanitizeHtml(request.getDescription()));
        }
        if (request.getLogoUrl() != null) {
            store.setLogoUrl(request.getLogoUrl());
        }
        if (request.getBannerUrl() != null) {
            store.setBannerUrl(request.getBannerUrl());
        }
        if (request.getVideoUrl() != null) {
            store.setVideoUrl(request.getVideoUrl());
        }
        if (request.getContactPhone() != null) {
            store.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            store.setContactEmail(request.getContactEmail());
        }
        if (request.getAddress() != null) {
            store.setAddress(XssSanitizer.sanitize(request.getAddress()));
        }

        store = storeRepository.save(store);
        log.info("Store {} updated by seller {}", storeId, sellerId);
        return store;
    }

    private String generateStoreCode() {
        String code;
        do {
            code = "ST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (storeRepository.existsByStoreCode(code));
        return code;
    }
}
