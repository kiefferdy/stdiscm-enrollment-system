package com.stdiscm.course.repository;

import com.stdiscm.common.model.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Use EntityGraph to fetch course eagerly (student is no longer an attribute)
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
    @Query("SELECT e FROM Enrollment e WHERE e.studentId = :studentId AND e.grade IS NOT NULL") // Use studentId field
    @EntityGraph(attributePaths = {"course"}) // No longer need to fetch student here
    List<Enrollment> findByStudentIdAndGradeIsNotNull(@Param("studentId") Long studentId);

    // These methods likely don't need the full graph fetched
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId); // Should still work by convention
}
