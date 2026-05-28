package com.campusadda.vendorops.user.repository;

import java.util.Optional;

import com.campusadda.vendorops.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleCode(String roleCode);
}