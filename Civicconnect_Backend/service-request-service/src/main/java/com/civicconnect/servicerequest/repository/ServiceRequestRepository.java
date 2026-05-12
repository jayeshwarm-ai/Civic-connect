package com.civicconnect.servicerequest.repository;

import com.civicconnect.servicerequest.entity.ServiceRequest;
import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import com.civicconnect.servicerequest.enums.ServiceRequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);

    List<ServiceRequest> findByAssignedOfficerUserIdOrderByCreatedAtDesc(Long officerUserId);

    List<ServiceRequest> findByStatusOrderByCreatedAtAsc(ServiceRequestStatus status);

    List<ServiceRequest> findByStatusAndAssignedOfficerUserIdOrderByCreatedAtAsc(
            ServiceRequestStatus status, Long officerUserId);

    List<ServiceRequest> findByTypeOrderByCreatedAtDesc(ServiceRequestType type);

    long countByStatus(ServiceRequestStatus status);

    long countByType(ServiceRequestType type);
}
