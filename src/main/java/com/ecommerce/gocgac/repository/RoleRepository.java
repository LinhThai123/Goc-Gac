package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(String code);
    Optional<Role> findByName(String name);
    boolean existsByCode(String code);
    boolean existsByName(String name);
}

