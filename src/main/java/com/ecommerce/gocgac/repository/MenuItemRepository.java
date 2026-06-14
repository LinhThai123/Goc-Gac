package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.MenuItem;
import com.ecommerce.gocgac.entity.enums.MenuType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByMenuTypeAndIsActiveTrueOrderByDisplayOrderAsc(MenuType menuType);

    List<MenuItem> findByMenuTypeOrderByDisplayOrderAsc(MenuType menuType);
}
