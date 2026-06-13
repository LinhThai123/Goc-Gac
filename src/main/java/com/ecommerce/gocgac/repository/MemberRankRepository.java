package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.MemberRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRankRepository extends JpaRepository<MemberRank, Long> {

    /** Hạng đang hoạt động, sắp xếp theo ngưỡng chi tiêu giảm dần (cao nhất trước). */
    List<MemberRank> findByStatusOrderByMinSpendingDesc(String status);

    /** Hạng đang hoạt động, sắp xếp theo thứ tự hiển thị tăng dần. */
    List<MemberRank> findByStatusOrderByRankOrderAsc(String status);
}