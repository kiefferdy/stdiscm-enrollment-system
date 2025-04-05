package com.stdiscm.course.repository;

import com.stdiscm.common.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    boolean existsByCourseCode(String courseCode);
    List<Course> findByIsOpenTrue();
    List<Course> findByFacultyId(Long facultyId);
}
