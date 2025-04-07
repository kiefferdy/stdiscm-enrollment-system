package com.stdiscm.grade.controller;

import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.dto.EnrollmentDto;
import com.stdiscm.common.dto.GradeSubmissionDto;
// import com.stdiscm.common.model.User; // No longer needed directly
import com.stdiscm.grade.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {
    
    @Autowired
    private GradeService gradeService;
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getGradesByStudentId(@PathVariable Long studentId) {
        List<EnrollmentDto> grades = gradeService.getGradesByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success("Student grades retrieved successfully", grades));
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getGradesByCourseId(@PathVariable Long courseId) {
        List<EnrollmentDto> grades = gradeService.getGradesByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.success("Course grades retrieved successfully", grades));
    }
    
    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<?> getGradesByFacultyId(@PathVariable Long facultyId) {
        List<EnrollmentDto> grades = gradeService.getGradesByFacultyId(facultyId);
        return ResponseEntity.ok(ApiResponse.success("Faculty grades retrieved successfully", grades));
    }
    
    @PostMapping("/submit")
    public ResponseEntity<?> submitGrade(@Valid @RequestBody GradeSubmissionDto gradeSubmissionDto, 
                                        @RequestHeader("X-User-Id") Long facultyId) { // Changed to RequestHeader
        // Service method already accepts facultyId
        EnrollmentDto updatedEnrollment = gradeService.submitGrade(gradeSubmissionDto, facultyId); 
        return ResponseEntity.ok(ApiResponse.success("Grade submitted successfully", updatedEnrollment));
    }
}
