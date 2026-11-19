package edu.academic.solutions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRecordRepository extends JpaRepository<ActivityRecord, Long> {
    List<ActivityRecord> findByModuleKeyOrderByCreatedAtDesc(String moduleKey);
}
