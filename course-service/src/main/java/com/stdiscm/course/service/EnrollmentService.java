package com.stdiscm.course.service;

import com.stdiscm.common.dto.EnrollmentDto;
import com.stdiscm.common.exception.BadRequestException;
import com.stdiscm.common.exception.ResourceNotFoundException;
import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.model.Course;
import com.stdiscm.common.model.Enrollment;
import com.stdiscm.common.model.User;
import com.stdiscm.course.client.AuthServiceClient;
import com.stdiscm.course.repository.CourseRepository;
import com.stdiscm.course.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.stdiscm.common.dto.GradeSubmissionDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CourseService courseService;

    @Autowired
    private AuthServiceClient authServiceClient; // Inject Feign Client
    
    public List<EnrollmentDto> getEnrollmentsByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<EnrollmentDto> getEnrollmentsByCourseId(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public EnrollmentDto getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
        return convertToDto(enrollment);
    }
    
    @Transactional
    public EnrollmentDto enrollStudent(Long courseId, Long studentId) { // Changed signature
        // 1. Fetch Course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        
        // 2. Fetch Student User object from auth-service via Feign client
        User student;
        try {
            ResponseEntity<ApiResponse<User>> response = authServiceClient.getUserById(studentId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getData() != null) {
                student = response.getBody().getData();
            } else {
                String errorMsg = (response.getBody() != null && response.getBody().getMessage() != null) 
                                  ? response.getBody().getMessage() 
                                  : "User not found or auth-service error";
                throw new ResourceNotFoundException("Student", "id", studentId + " (" + errorMsg + ")");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch student details for ID " + studentId + ": " + e.getMessage(), e);
        }

        // 3. Perform validation checks
        if (!course.getIsOpen()) {
            throw new BadRequestException("Course is not open for enrollment");
        }
        
        if (course.getEnrolled() >= course.getCapacity()) {
            throw new BadRequestException("Course has reached its capacity");
        }
        
        // Use fetched student's ID for the check
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) { 
            throw new BadRequestException("Student is already enrolled in this course");
        }
        
        // 4. Create and save enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student); // Use fetched student object
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setIsActive(true);
        
        courseService.incrementEnrolled(courseId);
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return convertToDto(savedEnrollment);
    }
    
    @Transactional
    public void dropEnrollment(Long enrollmentId, Long studentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));
        
        if (!enrollment.getStudent().getId().equals(studentId)) {
            throw new BadRequestException("Enrollment does not belong to the student");
        }
        
        if (enrollment.getGrade() != null) {
            throw new BadRequestException("Cannot drop a course that already has a grade");
        }
        
        courseService.decrementEnrolled(enrollment.getCourse().getId());
        enrollment.setIsActive(false);
        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public EnrollmentDto updateGrade(GradeSubmissionDto gradeSubmissionDto, Long facultyId) {
        // 1. Fetch Enrollment
        Enrollment enrollment = enrollmentRepository.findById(gradeSubmissionDto.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", gradeSubmissionDto.getEnrollmentId()));

        // 2. Authorization Check: Ensure the faculty submitting the grade is assigned to the course
        // Use facultyId directly from the Course object
        if (enrollment.getCourse() == null || enrollment.getCourse().getFacultyId() == null || 
            !enrollment.getCourse().getFacultyId().equals(facultyId)) {
            throw new BadRequestException("Faculty not authorized to submit grade for this course enrollment.");
        }

        // 3. Check if enrollment is active
        if (!enrollment.getIsActive()) {
             throw new BadRequestException("Cannot submit grade for an inactive enrollment.");
        }

        // 4. Update grade and save
        enrollment.setGrade(gradeSubmissionDto.getGrade());
        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);

        // 5. Convert and return DTO
        return convertToDto(updatedEnrollment);
    }

    // --- Methods for fetching GRADED enrollments ---

    public List<EnrollmentDto> getGradedEnrollmentsByStudentId(Long studentId) {
        // Assuming EnrollmentRepository has or will have a method like findByStudentIdAndGradeIsNotNull
        // For now, filter in memory after fetching (less efficient but works with current repo)
        // Ideally, add a specific query to EnrollmentRepository
        return enrollmentRepository.findByStudentId(studentId).stream()
                .filter(e -> e.getGrade() != null)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<EnrollmentDto> getGradedEnrollmentsByCourseId(Long courseId) {
        // Ideally, add findByCourseIdAndGradeIsNotNull to EnrollmentRepository
        return enrollmentRepository.findByCourseId(courseId).stream()
                .filter(e -> e.getGrade() != null)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<EnrollmentDto> getGradedEnrollmentsByFacultyId(Long facultyId) {
        // This requires a more complex query in EnrollmentRepository, joining Course and User
        // Placeholder: Fetch all and filter (inefficient)
        // Ideally, add findByCourseFacultyIdAndGradeIsNotNull to EnrollmentRepository
        // Use facultyId directly from the Course object for filtering
        return enrollmentRepository.findAll().stream() 
                .filter(e -> e.getGrade() != null && 
                             e.getCourse() != null && 
                             e.getCourse().getFacultyId() != null &&
                             e.getCourse().getFacultyId().equals(facultyId))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // --- Conversion ---
    
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
            // We can set the facultyId in the DTO if needed, but not the name without a Feign call
            dto.setFacultyId(enrollment.getCourse().getFacultyId()); 
        }
        
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setGrade(enrollment.getGrade());
        dto.setIsActive(enrollment.getIsActive());
        
        return dto;
    }
}
