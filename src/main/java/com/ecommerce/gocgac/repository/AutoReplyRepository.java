package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.AutoReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoReplyRepository extends JpaRepository<AutoReply, Long> {

    List<AutoReply> findByStoreId(Long storeId);

    List<AutoReply> findByStoreIdAndIsActiveTrue(Long storeId);
}
