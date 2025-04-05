package com.stdiscm.grade.client;

import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.dto.EnrollmentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service")
public interface CourseServiceClient {
    
    @GetMapping("/api/courses/enrollments/{id}")
    ApiResponse<EnrollmentDto> getEnrollmentById(@PathVariable("id") Long id);
    
    @GetMapping("/api/courses/enrollments/course/{courseId}")
    ApiResponse<java.util.List<EnrollmentDto>> getEnrollmentsByCourseId(@PathVariable("courseId") Long courseId);
    
    @GetMapping("/api/courses/enrollments/student/{studentId}")
    ApiResponse<java.util.List<EnrollmentDto>> getEnrollmentsByStudentId(@PathVariable("studentId") Long studentId);
}
