package com.civicconnect.servicerequest.repository;

import com.civicconnect.servicerequest.entity.RequestUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestUpdateRepository extends JpaRepository<RequestUpdate, Long> {

    List<RequestUpdate> findByServiceRequest_RequestIdOrderByCreatedAtAsc(Long requestId);
}
