package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.SocialLogin;
import com.ecommerce.gocgac.entity.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {

    Optional<SocialLogin> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);
}

