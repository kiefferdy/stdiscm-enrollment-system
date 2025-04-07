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
import java.util.Optional;
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
        // Use explicit query from repository
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
    public EnrollmentDto enrollStudent(Long courseId, Long studentId) {
        // 1. Fetch Course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // 2. Perform validation checks
        if (!course.getIsOpen()) {
            throw new BadRequestException("Course is not open for enrollment");
        }

        if (course.getEnrolled() >= course.getCapacity()) {
            throw new BadRequestException("Course has reached its capacity");
        }

        // 3. Check for existing enrollment (active or inactive)
        Optional<Enrollment> existingEnrollmentOpt = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);

        if (existingEnrollmentOpt.isPresent()) {
            Enrollment existingEnrollment = existingEnrollmentOpt.get();
            if (existingEnrollment.getIsActive()) {
                // Already actively enrolled
                throw new BadRequestException("Student is already actively enrolled in this course");
            } else {
                // Re-enrollment: Update existing inactive record
                existingEnrollment.setIsActive(true);
                existingEnrollment.setEnrollmentDate(LocalDateTime.now()); // Update enrollment date
                existingEnrollment.setGrade(null); // Clear any previous grade if re-enrolling

                courseService.incrementEnrolled(courseId); // Increment count again

                Enrollment savedEnrollment = enrollmentRepository.save(existingEnrollment);
                return convertToDto(savedEnrollment);
            }
        } else {
            // 4. Create and save NEW enrollment if none exists
            Enrollment enrollment = new Enrollment();
            enrollment.setStudentId(studentId); // Set studentId directly
            enrollment.setCourse(course);
            enrollment.setEnrollmentDate(LocalDateTime.now());
            enrollment.setIsActive(true);

            courseService.incrementEnrolled(courseId);

            Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
            return convertToDto(savedEnrollment);
        }
    }

    @Transactional
    public void dropEnrollment(Long enrollmentId, Long studentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        // Check against stored studentId
        if (!enrollment.getStudentId().equals(studentId)) {
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
    // Use the dedicated repository method to fetch only graded enrollments
    return enrollmentRepository.findByStudentIdAndGradeIsNotNull(studentId).stream()
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
        dto.setStudentId(enrollment.getStudentId()); // Set studentId directly

        // Fetch student name from auth-service when converting to DTO
        try {
            ResponseEntity<ApiResponse<User>> response = authServiceClient.getUserById(enrollment.getStudentId());
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getData() != null) {
                dto.setStudentName(response.getBody().getData().getFullName());
            } else {
                dto.setStudentName("N/A"); // Handle case where user might not be found or auth-service error
            }
        } catch (Exception e) {
            // Log error if needed: logger.error("Failed to fetch student name for ID {}: {}", enrollment.getStudentId(), e.getMessage());
            dto.setStudentName("Error"); // Indicate an error occurred fetching the name
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
