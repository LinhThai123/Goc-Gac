package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndVariantId(Long cartId, Long variantId);

    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);

    long countByCartId(Long cartId);

    void deleteAllByCartId(Long cartId);
}
