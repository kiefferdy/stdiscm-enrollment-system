package com.stdiscm.course.repository;

import com.stdiscm.common.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    // No longer need EntityGraph for faculty
    // @EntityGraph(attributePaths = {"faculty"}) 
    @Override
    Optional<Course> findById(Long id);

    // @EntityGraph(attributePaths = {"faculty"})
    @Override
    List<Course> findAll();

    Optional<Course> findByCourseCode(String courseCode); 
    
    boolean existsByCourseCode(String courseCode);
    
    // Remove JOIN FETCH for faculty
    // @Query("SELECT c FROM Course c JOIN FETCH c.faculty WHERE c.isOpen = true")
    List<Course> findByIsOpenTrue(); // Rely on derived query
    
    // Remove JOIN FETCH for faculty
    // @Query("SELECT c FROM Course c JOIN FETCH c.faculty WHERE c.faculty.id = :facultyId")
    List<Course> findByFacultyId(@Param("facultyId") Long facultyId); // Keep @Param, rely on derived query based on field name
}
