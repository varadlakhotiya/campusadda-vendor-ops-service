package com.campusadda.vendorops.user.repository;

import java.util.List;

import com.campusadda.vendorops.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUser_Id(Long userId);

    boolean existsByUser_IdAndRole_Id(Long userId, Long roleId);

    void deleteByUser_IdAndRole_Id(Long userId, Long roleId);
}