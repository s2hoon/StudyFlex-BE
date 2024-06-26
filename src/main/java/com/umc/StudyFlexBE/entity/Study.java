package com.umc.StudyFlexBE.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;
import org.hibernate.annotations.BatchSize;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id")
    private Long id;

    @Column(name = "study_name", length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "study_status", columnDefinition = "ENUM('RECRUITING', 'COMPLETED')")
    private StudyStatus status;


    @Column(name = "thumbnail_url", length = 2083)
    private String thumbnailUrl;

    @OneToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private Category category;

    @Column(name = "study_created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "study_updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "study_completed_at")
    private LocalDateTime completedAt;

    @Column(name = "leader_id")
    private Long leaderId;

    @Column(name = "max_members")
    private Integer maxMembers;

    @Column(name = "current_members")
    private Integer currentMembers;

    @Column(name = "study_hits")
    private BigInteger hits;

    @Transient
    private Double rankScore;

    @Column(name = "completed_week")
     private Integer completedWeek = 0;

    @Column(name = "targer_week")
    private Integer targetWeek;

    @BatchSize(size = 100)
    @Builder.Default
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<StudyParticipation> studyParticipationList = new ArrayList<>();

    @BatchSize(size = 100)
    @Builder.Default
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Progress> progressList = new ArrayList<>();

    public void setRankScore(Double rankScore) {
        this.rankScore = rankScore;
    }

    public Double getRankScore() {
        return rankScore;
    }

    public int participationStudy() {
        ++currentMembers;
        if (currentMembers.equals(maxMembers)) {
            status = StudyStatus.COMPLETED;
        }

        completedWeek = 0;
        return currentMembers;
    }

    public Double getTotalProgressRate(){
        return (completedWeek*1.0)/targetWeek;
    }
}

