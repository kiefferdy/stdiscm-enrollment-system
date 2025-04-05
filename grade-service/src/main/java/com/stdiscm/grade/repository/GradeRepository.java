package com.stdiscm.grade.repository;

import com.stdiscm.common.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Enrollment, Long> {
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.grade IS NOT NULL")
    List<Enrollment> findCompletedCoursesByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId AND e.grade IS NOT NULL")
    List<Enrollment> findCompletedEnrollmentsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.course.faculty.id = :facultyId AND e.grade IS NOT NULL")
    List<Enrollment> findCompletedEnrollmentsByFacultyId(@Param("facultyId") Long facultyId);
}
