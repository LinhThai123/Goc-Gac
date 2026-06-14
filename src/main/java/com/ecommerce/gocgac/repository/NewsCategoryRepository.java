package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.NewsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsCategoryRepository extends JpaRepository<NewsCategory, Long> {

    List<NewsCategory> findByIsActiveTrueOrderByDisplayOrderAsc();

    boolean existsByCategorySlug(String categorySlug);
}
