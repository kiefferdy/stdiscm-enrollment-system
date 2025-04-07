package com.stdiscm.grade.client;

import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.dto.EnrollmentDto;
import com.stdiscm.common.dto.GradeSubmissionDto; // Import GradeSubmissionDto
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*; // Import RequestBody, RequestHeader

@FeignClient(name = "course-service")
public interface CourseServiceClient {
    
    @GetMapping("/api/courses/enrollments/{id}")
    ApiResponse<EnrollmentDto> getEnrollmentById(@PathVariable("id") Long id);
    
    @GetMapping("/api/courses/enrollments/course/{courseId}")
    ApiResponse<java.util.List<EnrollmentDto>> getEnrollmentsByCourseId(@PathVariable("courseId") Long courseId);
    
    @GetMapping("/api/courses/enrollments/student/{studentId}")
    ApiResponse<java.util.List<EnrollmentDto>> getEnrollmentsByStudentId(@PathVariable("studentId") Long studentId);

    // Method to call the new grade update endpoint
    @PutMapping("/api/courses/enrollments/grade")
    ApiResponse<EnrollmentDto> updateGrade(@RequestBody GradeSubmissionDto gradeSubmissionDto,
                                           @RequestHeader("X-User-Id") Long facultyId); // Pass facultyId in header

    // Methods to fetch GRADED enrollments
    @GetMapping("/api/courses/enrollments/student/{studentId}/graded")
    ApiResponse<java.util.List<EnrollmentDto>> getGradedEnrollmentsByStudentId(@PathVariable("studentId") Long studentId);

    @GetMapping("/api/courses/enrollments/course/{courseId}/graded")
    ApiResponse<java.util.List<EnrollmentDto>> getGradedEnrollmentsByCourseId(@PathVariable("courseId") Long courseId);

    @GetMapping("/api/courses/enrollments/faculty/{facultyId}/graded")
    ApiResponse<java.util.List<EnrollmentDto>> getGradedEnrollmentsByFacultyId(@PathVariable("facultyId") Long facultyId);
}
