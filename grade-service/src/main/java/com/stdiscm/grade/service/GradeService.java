package com.stdiscm.grade.service;

import com.stdiscm.common.dto.EnrollmentDto;
import com.stdiscm.common.dto.GradeSubmissionDto;
import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.grade.client.CourseServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class GradeService {
    
    // @Autowired
    // private GradeRepository gradeRepository; // Remove GradeRepository dependency
    
    @Autowired
    private CourseServiceClient courseServiceClient;
    
    public List<EnrollmentDto> getGradesByStudentId(Long studentId) {
        try {
            ApiResponse<List<EnrollmentDto>> response = courseServiceClient.getGradedEnrollmentsByStudentId(studentId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            } else {
                // Log error or handle appropriately
                System.err.println("Error fetching graded enrollments by student from course-service: " + (response != null ? response.getMessage() : "Null response"));
                return Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("Exception fetching graded enrollments by student: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    public List<EnrollmentDto> getGradesByCourseId(Long courseId) {
         try {
            ApiResponse<List<EnrollmentDto>> response = courseServiceClient.getGradedEnrollmentsByCourseId(courseId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            } else {
                System.err.println("Error fetching graded enrollments by course from course-service: " + (response != null ? response.getMessage() : "Null response"));
                return Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("Exception fetching graded enrollments by course: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    public List<EnrollmentDto> getGradesByFacultyId(Long facultyId) {
         try {
            ApiResponse<List<EnrollmentDto>> response = courseServiceClient.getGradedEnrollmentsByFacultyId(facultyId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            } else {
                System.err.println("Error fetching graded enrollments by faculty from course-service: " + (response != null ? response.getMessage() : "Null response"));
                return Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("Exception fetching graded enrollments by faculty: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    public EnrollmentDto submitGrade(GradeSubmissionDto gradeSubmissionDto, Long facultyId) {
        // Call the course-service endpoint to update the grade
        // The course-service handles fetching enrollment, authorization, and saving.
        try {
            ApiResponse<EnrollmentDto> response = courseServiceClient.updateGrade(gradeSubmissionDto, facultyId);
            
            if (response == null || !response.isSuccess() || response.getData() == null) {
                 // Handle potential errors returned from course-service
                 String errorMsg = (response != null && response.getMessage() != null) ? response.getMessage() : "Failed to submit grade in course-service";
                 throw new RuntimeException(errorMsg);
            }
            
            return response.getData(); // Return the updated DTO from course-service
            
        } catch (Exception e) {
            // Catch Feign exceptions or other communication errors
            // Log the error for debugging
            // logger.error("Error calling course-service to submit grade: {}", e.getMessage(), e);
            throw new RuntimeException("Error submitting grade: " + e.getMessage(), e);
        }
    }
    
    // Note: The getGradesBy... methods now correctly delegate to course-service.
    // Further optimization could involve adding specific queries to EnrollmentRepository 
    // in course-service to filter by grade != null directly in the database.
}
