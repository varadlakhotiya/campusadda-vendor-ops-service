package com.campusadda.vendorops.etl.repository;

import com.campusadda.vendorops.etl.entity.EtlJobRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtlJobRunRepository extends JpaRepository<EtlJobRun, Long> {

    List<EtlJobRun> findByJobNameOrderByStartedAtDesc(String jobName);

    Optional<EtlJobRun> findTopByJobNameAndStatusOrderByWindowEndDesc(
            String jobName,
            String status
    );
}