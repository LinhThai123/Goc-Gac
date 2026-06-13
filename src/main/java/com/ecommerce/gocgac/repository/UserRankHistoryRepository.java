package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.UserRankHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRankHistoryRepository extends JpaRepository<UserRankHistory, Long> {

    List<UserRankHistory> findByUserIdOrderByAchievedAtDesc(Long userId);
}