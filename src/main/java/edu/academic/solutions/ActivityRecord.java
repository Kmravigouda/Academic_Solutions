package edu.academic.solutions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "activity_records")
public class ActivityRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String moduleKey;

    @Column(nullable = false, length = 80)
    private String studentId;

    @Column(nullable = false, length = 120)
    private String studentName;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 120)
    private String period;

    @Column(length = 2000)
    private String details;

    @Column(precision = 10, scale = 2)
    private BigDecimal metric;

    @Column(precision = 10, scale = 2)
    private BigDecimal maximumMetric;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weight = BigDecimal.ONE;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public String getModuleKey() { return moduleKey; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getTitle() { return title; }
    public String getPeriod() { return period; }
    public String getDetails() { return details; }
    public BigDecimal getMetric() { return metric; }
    public BigDecimal getMaximumMetric() { return maximumMetric; }
    public BigDecimal getWeight() { return weight; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setModuleKey(String moduleKey) { this.moduleKey = moduleKey; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setTitle(String title) { this.title = title; }
    public void setPeriod(String period) { this.period = period; }
    public void setDetails(String details) { this.details = details; }
    public void setMetric(BigDecimal metric) { this.metric = metric; }
    public void setMaximumMetric(BigDecimal maximumMetric) { this.maximumMetric = maximumMetric; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public void setStatus(String status) { this.status = status; }
}
