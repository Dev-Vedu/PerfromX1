package com.pms.repository;

import com.pms.entity.Project;
import com.pms.entity.ProjectStatus;
import com.pms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByLead(User lead);

    List<Project> findByStatus(ProjectStatus status);

    @Query("select p from Project p join p.members m where m = :user")
    List<Project> findByMember(@Param("user") User user);

    @Query("select p from Project p left join p.members m where p.lead = :user or m = :user")
    List<Project> findVisibleTo(@Param("user") User user);
}
