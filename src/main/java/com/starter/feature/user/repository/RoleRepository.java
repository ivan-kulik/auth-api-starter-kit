package com.starter.feature.user.repository;

import com.starter.feature.user.RoleName;
import com.starter.feature.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
