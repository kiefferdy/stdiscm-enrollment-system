package com.stdiscm.grade.service;

import com.stdiscm.common.dto.EnrollmentDto;
import com.stdiscm.common.dto.GradeSubmissionDto;
import com.stdiscm.common.exception.BadRequestException;
import com.stdiscm.common.exception.ResourceNotFoundException;
import com.stdiscm.common.model.Enrollment;
import com.stdiscm.grade.client.CourseServiceClient;
import com.stdiscm.grade.repository.GradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GradeService {
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private CourseServiceClient courseServiceClient;
    
    public List<EnrollmentDto> getGradesByStudentId(Long studentId) {
        List<Enrollment> enrollments = gradeRepository.findCompletedCoursesByStudentId(studentId);
        return enrollments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<EnrollmentDto> getGradesByCourseId(Long courseId) {
        List<Enrollment> enrollments = gradeRepository.findCompletedEnrollmentsByCourseId(courseId);
        return enrollments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<EnrollmentDto> getGradesByFacultyId(Long facultyId) {
        List<Enrollment> enrollments = gradeRepository.findCompletedEnrollmentsByFacultyId(facultyId);
        return enrollments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public EnrollmentDto submitGrade(GradeSubmissionDto gradeSubmissionDto, Long facultyId) {
        // Fetch enrollment from course service
        EnrollmentDto enrollmentDto = courseServiceClient.getEnrollmentById(gradeSubmissionDto.getEnrollmentId()).getData();
        
        if (enrollmentDto == null) {
            throw new ResourceNotFoundException("Enrollment", "id", gradeSubmissionDto.getEnrollmentId());
        }
        
        // Verify if the course belongs to the faculty
        validateFacultyAuthorization(enrollmentDto, facultyId);
        
        // Update grade
        Enrollment enrollment = gradeRepository.findById(gradeSubmissionDto.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", gradeSubmissionDto.getEnrollmentId()));
        
        enrollment.setGrade(gradeSubmissionDto.getGrade());
        Enrollment updatedEnrollment = gradeRepository.save(enrollment);
        
        return convertToDto(updatedEnrollment);
    }
    
    private void validateFacultyAuthorization(EnrollmentDto enrollmentDto, Long facultyId) {
        // This would require an additional API call to validate course faculty
        // Simplified for now - assume the authorization is done at the API Gateway level
        if (!isAuthorizedFaculty(enrollmentDto.getCourseId(), facultyId)) {
            throw new BadRequestException("You are not authorized to submit grades for this course");
        }
    }
    
    private boolean isAuthorizedFaculty(Long courseId, Long facultyId) {
        // In a real implementation, we would check if the faculty is assigned to the course
        // For simplicity, we'll return true
        return true;
    }
    
    private EnrollmentDto convertToDto(Enrollment enrollment) {
        EnrollmentDto dto = new EnrollmentDto();
        dto.setId(enrollment.getId());
        
        if (enrollment.getStudent() != null) {
            dto.setStudentId(enrollment.getStudent().getId());
            dto.setStudentName(enrollment.getStudent().getFullName());
        }
        
        if (enrollment.getCourse() != null) {
            dto.setCourseId(enrollment.getCourse().getId());
            dto.setCourseCode(enrollment.getCourse().getCourseCode());
            dto.setCourseTitle(enrollment.getCourse().getTitle());
        }
        
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setGrade(enrollment.getGrade());
        dto.setIsActive(enrollment.getIsActive());
        
        return dto;
    }
}
