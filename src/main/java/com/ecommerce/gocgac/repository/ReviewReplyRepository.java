package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {

    List<ReviewReply> findByReviewIdOrderByCreatedAtAsc(Long reviewId);
}
