package com.stdiscm.course.repository;

import com.stdiscm.common.model.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @Override
    @EntityGraph(attributePaths = {"course"})
    Optional<Enrollment> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"course"})
    List<Enrollment> findAll();

    // Use explicit query to avoid ambiguity with studentId field vs old student relationship
    @Query("SELECT e FROM Enrollment e WHERE e.studentId = :studentId")
    @EntityGraph(attributePaths = {"course"})
    List<Enrollment> findByStudentId(@Param("studentId") Long studentId);

    @EntityGraph(attributePaths = {"course"})
    List<Enrollment> findByCourseId(Long courseId);

    // Method to find graded enrollments for a student
    @Query("SELECT e FROM Enrollment e WHERE e.studentId = :studentId AND e.grade IS NOT NULL")
    @EntityGraph(attributePaths = {"course"})
    List<Enrollment> findByStudentIdAndGradeIsNotNull(@Param("studentId") Long studentId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    // Method to get IDs of courses a student is actively enrolled in
    @Query("SELECT e.course.id FROM Enrollment e WHERE e.studentId = :studentId AND e.isActive = true")
    Set<Long> findActiveCourseIdsByStudentId(@Param("studentId") Long studentId);
}
