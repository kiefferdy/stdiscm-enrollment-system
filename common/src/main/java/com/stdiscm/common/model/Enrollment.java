package com.stdiscm.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
// Remove @Table annotation as a workaround for potential constraint issues
// @Table(name = "enrollments", uniqueConstraints = {
//    @UniqueConstraint(columnNames = {"student_id", "course_id"})
// })
public class Enrollment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Store studentId directly, remove User relationship
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne // Keep relationship to Course within this service's context
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(nullable = false)
    private LocalDateTime enrollmentDate;
    
    @Column
    private String grade;
    
    @Column
    private Boolean isActive = true;
}
