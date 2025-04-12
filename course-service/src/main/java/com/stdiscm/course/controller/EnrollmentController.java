package com.stdiscm.course.controller;

import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.dto.EnrollmentDto;
import com.stdiscm.course.service.EnrollmentService;
import com.stdiscm.common.dto.GradeSubmissionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @GetMapping("/my-enrollments")
    public ResponseEntity<?> getMyEnrollments(@RequestHeader("X-User-Id") Long studentId) {
        List<EnrollmentDto> enrollments = enrollmentService.getEnrollmentsByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success("My enrollments retrieved successfully", enrollments));
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
    public ResponseEntity<?> enrollStudent(@PathVariable Long courseId, @RequestHeader("X-User-Id") Long studentId) {
        EnrollmentDto enrollment = enrollmentService.enrollStudent(courseId, studentId); 
        return ResponseEntity.ok(ApiResponse.success("Student enrolled successfully", enrollment));
    }
    
    @PostMapping("/drop/{enrollmentId}")
    public ResponseEntity<?> dropEnrollment(@PathVariable Long enrollmentId, @RequestHeader("X-User-Id") Long studentId) {
        // Service method already accepts studentId, so this change is straightforward
        enrollmentService.dropEnrollment(enrollmentId, studentId); 
        return ResponseEntity.ok(ApiResponse.success("Enrollment dropped successfully"));
    }

    // New endpoint for updating grade
    @PutMapping("/grade")
    public ResponseEntity<?> updateGrade(@Valid @RequestBody GradeSubmissionDto gradeSubmissionDto,
                                         @RequestHeader("X-User-Id") Long facultyId) {
        EnrollmentDto updatedEnrollment = enrollmentService.updateGrade(gradeSubmissionDto, facultyId);
        return ResponseEntity.ok(ApiResponse.success("Grade updated successfully", updatedEnrollment));
    }

    // --- Endpoints for fetching GRADED enrollments ---

    @GetMapping("/student/{studentId}/graded")
    public ResponseEntity<?> getGradedEnrollmentsByStudentId(@PathVariable Long studentId) {
        List<EnrollmentDto> enrollments = enrollmentService.getGradedEnrollmentsByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success("Graded student enrollments retrieved successfully", enrollments));
    }

    @GetMapping("/course/{courseId}/graded")
    public ResponseEntity<?> getGradedEnrollmentsByCourseId(@PathVariable Long courseId) {
        List<EnrollmentDto> enrollments = enrollmentService.getGradedEnrollmentsByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.success("Graded course enrollments retrieved successfully", enrollments));
    }

    @GetMapping("/faculty/{facultyId}/graded")
    public ResponseEntity<?> getGradedEnrollmentsByFacultyId(@PathVariable Long facultyId) {
        List<EnrollmentDto> enrollments = enrollmentService.getGradedEnrollmentsByFacultyId(facultyId);
        return ResponseEntity.ok(ApiResponse.success("Graded faculty enrollments retrieved successfully", enrollments));
    }
}
