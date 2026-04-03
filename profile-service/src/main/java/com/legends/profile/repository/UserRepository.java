package com.legends.profile.repository;

import com.legends.profile.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface UserRepository extends JpaRepository<UserProfile, Long> {

    boolean existsByUsername(String username);

    Optional<UserProfile> findByUsername(String username);
}
