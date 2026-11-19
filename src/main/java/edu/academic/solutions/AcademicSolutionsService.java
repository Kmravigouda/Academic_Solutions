package edu.academic.solutions;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AcademicSolutionsService {
    private static final List<ModuleDefinition> MODULES = List.of(
            new ModuleDefinition("leetcode", "LeetCode Tracker", "Follow problem-solving progress by student.", "Problems solved", "Target problems", "Topic / difficulty", "Record weight"),
            new ModuleDefinition("github", "GitHub Repo Evaluator", "Record rubric-based repository reviews.", "Rubric score", "Maximum score", "Repository / rubric", "Record weight"),
            new ModuleDefinition("academic-score", "Academic Score Tracker", "Track subject grades and calculate weighted CGPA.", "Grade point", "Maximum grade point", "Semester / subject", "Credits"),
            new ModuleDefinition("semester-progress", "Semester-wise Progress", "Compare performance across semesters.", "Score", "Maximum score", "Semester", "Record weight"),
            new ModuleDefinition("interest-finder", "Area-of-Interest Finder", "Record interests and suitability ratings.", "Suitability score", "Maximum score", "Interest area", "Record weight"),
            new ModuleDefinition("mock-tests", "Personalised Mock Tests", "Track tailored practice tests and results.", "Test score", "Maximum score", "Topic / area", "Record weight"),
            new ModuleDefinition("peer-space", "Peer Space", "Connect students around shared interests.", "Skill rating", "Maximum rating", "Interest group", "Record weight"),
            new ModuleDefinition("career-path", "Career Advancement Platform", "Track career goals and milestones.", "Readiness score", "Maximum score", "Career path / milestone", "Record weight"),
            new ModuleDefinition("company-prep", "Company-Specific Preparation", "Track preparation by company and role.", "Readiness score", "Maximum score", "Company / role", "Record weight"),
            new ModuleDefinition("campus-mock", "Campus Hiring Mock Readiness", "Record campus-company mock interview readiness.", "Readiness score", "Maximum score", "Company / role", "Record weight")
    );
    private static final Set<String> VALID_STATUSES = Set.of("Planned", "In progress", "Completed");
    private static final Set<String> MODULE_KEYS = MODULES.stream().map(ModuleDefinition::key).collect(java.util.stream.Collectors.toUnmodifiableSet());

    private final ActivityRecordRepository repository;

    public AcademicSolutionsService(ActivityRecordRepository repository) {
        this.repository = repository;
    }

    public List<ModuleDefinition> getModules() {
        return MODULES;
    }

    public List<ActivityRecord> getRecords(String moduleKey) {
        if (moduleKey == null || moduleKey.isBlank()) {
            return repository.findAll();
        }
        requireModule(moduleKey);
        return repository.findByModuleKeyOrderByCreatedAtDesc(moduleKey);
    }

    @Transactional
    public ActivityRecord create(ActivityRequest request) {
        requireModule(request.moduleKey());
        if (!VALID_STATUSES.contains(request.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a valid status.");
        }
        if ((request.metric() == null) != (request.maximumMetric() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter both a score and its maximum, or leave both blank.");
        }
        if (request.metric() != null && request.metric().compareTo(request.maximumMetric()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score cannot exceed its maximum.");
        }

        ActivityRecord record = new ActivityRecord();
        record.setModuleKey(request.moduleKey());
        record.setStudentId(request.studentId().trim());
        record.setStudentName(request.studentName().trim());
        record.setTitle(request.title().trim());
        record.setPeriod(trimToNull(request.period()));
        record.setDetails(trimToNull(request.details()));
        record.setMetric(request.metric());
        record.setMaximumMetric(request.maximumMetric());
        record.setWeight(request.weight());
        record.setStatus(request.status());
        return repository.save(record);
    }

    @Transactional
    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found.");
        }
        repository.deleteById(id);
    }

    public Map<String, Object> getDashboardStats() {
        List<ActivityRecord> records = repository.findAll();
        BigDecimal credits = BigDecimal.ZERO;
        BigDecimal weightedGradePoints = BigDecimal.ZERO;
        BigDecimal semesterScores = BigDecimal.ZERO;
        BigDecimal semesterMaximums = BigDecimal.ZERO;
        long semesterRecords = 0;
        long completed = 0;

        for (ActivityRecord record : records) {
            if ("Completed".equals(record.getStatus())) {
                completed++;
            }
            if (record.getMetric() == null || record.getMaximumMetric() == null) {
                continue;
            }
            if ("academic-score".equals(record.getModuleKey())) {
                credits = credits.add(record.getWeight());
                weightedGradePoints = weightedGradePoints.add(record.getMetric().multiply(record.getWeight()));
            } else if ("semester-progress".equals(record.getModuleKey())) {
                semesterScores = semesterScores.add(record.getMetric());
                semesterMaximums = semesterMaximums.add(record.getMaximumMetric());
                semesterRecords++;
            }
        }
        BigDecimal cgpa = credits.signum() == 0 ? null
                : weightedGradePoints.divide(credits, 2, RoundingMode.HALF_UP);
        BigDecimal semesterAverage = semesterMaximums.signum() == 0 ? null
                : semesterScores.multiply(BigDecimal.valueOf(100))
                .divide(semesterMaximums, 1, RoundingMode.HALF_UP);

        return Map.of(
                "recordCount", records.size(),
                "completedCount", completed,
                "cgpa", cgpa == null ? "" : cgpa,
                "semesterAverage", semesterAverage == null ? "" : semesterAverage,
                "semesterRecordCount", semesterRecords
        );
    }

    private void requireModule(String moduleKey) {
        if (!MODULE_KEYS.contains(moduleKey)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown module.");
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
