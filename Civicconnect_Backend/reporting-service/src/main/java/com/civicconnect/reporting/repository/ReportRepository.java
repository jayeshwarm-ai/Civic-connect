package com.civicconnect.reporting.repository;

import com.civicconnect.reporting.entity.Report;
import com.civicconnect.reporting.enums.ReportScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByScopeOrderByGeneratedDateDesc(ReportScope scope);

    List<Report> findAllByOrderByGeneratedDateDesc();

    List<Report> findByGeneratedByUserIdOrderByGeneratedDateDesc(Long userId);
}
