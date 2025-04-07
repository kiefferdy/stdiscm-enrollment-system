package com.stdiscm.course.repository;

import com.stdiscm.common.model.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph; // Import EntityGraph
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Use EntityGraph to fetch student and course eagerly
    @Override
    @EntityGraph(attributePaths = {"student", "course"}) // Removed course.faculty
    Optional<Enrollment> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"student", "course"}) // Removed course.faculty
    List<Enrollment> findAll();

    @EntityGraph(attributePaths = {"student", "course"}) // Removed course.faculty
    List<Enrollment> findByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"student", "course"}) // Removed course.faculty
    List<Enrollment> findByCourseId(Long courseId);
    
    // These methods likely don't need the full graph fetched
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}
