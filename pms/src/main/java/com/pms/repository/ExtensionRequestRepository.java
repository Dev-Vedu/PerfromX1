package com.pms.repository;

import com.pms.entity.ExtensionRequest;
import com.pms.entity.RequestStatus;
import com.pms.entity.Task;
import com.pms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExtensionRequestRepository extends JpaRepository<ExtensionRequest, Long> {

    List<ExtensionRequest> findByRequestedByOrderByCreatedAtDesc(User user);

    List<ExtensionRequest> findByTaskOrderByCreatedAtDesc(Task task);

    long countByRequestedByAndStatus(User user, RequestStatus status);

    long countByRequestedBy(User user);

    @Query("select r from ExtensionRequest r where r.task.project.lead = :lead and r.status = :status "
            + "order by r.createdAt asc")
    List<ExtensionRequest> findForLeadByStatus(@Param("lead") User lead, @Param("status") RequestStatus status);
}
