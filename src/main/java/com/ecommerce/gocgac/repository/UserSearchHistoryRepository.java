package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.UserSearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSearchHistoryRepository extends JpaRepository<UserSearchHistory, Long> {

    List<UserSearchHistory> findTop20ByUserIdOrderBySearchedAtDesc(Long userId);
}
