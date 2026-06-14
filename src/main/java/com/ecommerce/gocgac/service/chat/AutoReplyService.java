package com.ecommerce.gocgac.service.chat;

import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.chat.AutoReplyResponse;
import com.ecommerce.gocgac.dto.chat.UpsertAutoReplyRequest;
import com.ecommerce.gocgac.entity.AutoReply;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.AutoReplyRepository;
import com.ecommerce.gocgac.service.store.StoreResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Quản lý trả lời tự động của gian hàng (Sprint 8 - M16).
 */
@Service
@RequiredArgsConstructor
public class AutoReplyService {

    private final AutoReplyRepository autoReplyRepository;
    private final StoreResolver storeResolver;

    @Transactional
    public AutoReplyResponse create(Long sellerUserId, UpsertAutoReplyRequest req) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        AutoReply a = new AutoReply();
        a.setStoreId(storeId);
        a.setTriggerKeyword(req.getTriggerKeyword());
        a.setReplyMessage(XssSanitizer.sanitize(req.getReplyMessage()));
        a.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        return AutoReplyResponse.from(autoReplyRepository.save(a));
    }

    @Transactional
    public AutoReplyResponse update(Long sellerUserId, Long id, UpsertAutoReplyRequest req) {
        AutoReply a = getOwned(sellerUserId, id);
        if (req.getTriggerKeyword() != null) a.setTriggerKeyword(req.getTriggerKeyword());
        if (req.getReplyMessage() != null) a.setReplyMessage(XssSanitizer.sanitize(req.getReplyMessage()));
        if (req.getIsActive() != null) a.setIsActive(req.getIsActive());
        return AutoReplyResponse.from(autoReplyRepository.save(a));
    }

    @Transactional
    public void delete(Long sellerUserId, Long id) {
        AutoReply a = getOwned(sellerUserId, id);
        autoReplyRepository.delete(a);
    }

    public List<AutoReplyResponse> list(Long sellerUserId) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        return autoReplyRepository.findByStoreId(storeId).stream().map(AutoReplyResponse::from).toList();
    }

    private AutoReply getOwned(Long sellerUserId, Long id) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        AutoReply a = autoReplyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trả lời tự động không tồn tại"));
        if (!a.getStoreId().equals(storeId)) {
            throw new BusinessException("Bạn không có quyền quản lý mục này");
        }
        return a;
    }
}
