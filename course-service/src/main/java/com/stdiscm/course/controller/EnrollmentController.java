package com.stdiscm.course.controller;

import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.dto.EnrollmentDto;
import com.stdiscm.common.model.User;
import com.stdiscm.course.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/enrollments")
public class EnrollmentController {
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getEnrollmentsByStudentId(@PathVariable Long studentId) {
        List<EnrollmentDto> enrollments = enrollmentService.getEnrollmentsByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success("Student enrollments retrieved successfully", enrollments));
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getEnrollmentsByCourseId(@PathVariable Long courseId) {
        List<EnrollmentDto> enrollments = enrollmentService.getEnrollmentsByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.success("Course enrollments retrieved successfully", enrollments));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getEnrollmentById(@PathVariable Long id) {
        EnrollmentDto enrollment = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Enrollment retrieved successfully", enrollment));
    }
    
    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<?> enrollStudent(@PathVariable Long courseId, @RequestAttribute("user") User student) {
        EnrollmentDto enrollment = enrollmentService.enrollStudent(courseId, student);
        return ResponseEntity.ok(ApiResponse.success("Student enrolled successfully", enrollment));
    }
    
    @PostMapping("/drop/{enrollmentId}")
    public ResponseEntity<?> dropEnrollment(@PathVariable Long enrollmentId, @RequestAttribute("user") User student) {
        enrollmentService.dropEnrollment(enrollmentId, student.getId());
        return ResponseEntity.ok(ApiResponse.success("Enrollment dropped successfully"));
    }
}
