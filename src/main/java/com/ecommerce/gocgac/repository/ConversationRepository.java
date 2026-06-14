package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByBuyerIdAndStoreId(Long buyerId, Long storeId);

    Page<Conversation> findByBuyerIdOrderByLastMessageAtDesc(Long buyerId, Pageable pageable);

    Page<Conversation> findByStoreIdOrderByLastMessageAtDesc(Long storeId, Pageable pageable);
}
