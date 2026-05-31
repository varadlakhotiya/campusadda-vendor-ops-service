package com.campusadda.vendorops.ml.repository;

import com.campusadda.vendorops.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MlUserRepository extends JpaRepository<User, Long> {
}