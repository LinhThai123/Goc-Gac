package com.ecommerce.gocgac.service.store;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import java.util.List;
import com.ecommerce.gocgac.entity.Store;
import com.ecommerce.gocgac.repository.StoreRepository;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

    public List<Store> getStores() {
        return storeRepository.findAll();
    }

    public Store getStoreById(Long id) {
        return storeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy gian hàng"));
    }

    public Store getStoreByCooperativeId(Long cooperativeId) {
        return storeRepository.findByCooperativeId(cooperativeId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy gian hàng theo hợp tác xã"));
    }

    public Store getStoreBySellerId(Long sellerId) {
        return storeRepository.findBySellerId(sellerId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy gian hàng theo người bán"));
    }
}
