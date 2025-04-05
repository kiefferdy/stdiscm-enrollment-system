package com.stdiscm.course.service;

import com.stdiscm.common.dto.EnrollmentDto;
import com.stdiscm.common.exception.BadRequestException;
import com.stdiscm.common.exception.ResourceNotFoundException;
import com.stdiscm.common.model.Course;
import com.stdiscm.common.model.Enrollment;
import com.stdiscm.common.model.User;
import com.stdiscm.course.repository.CourseRepository;
import com.stdiscm.course.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public EnrollmentDto enrollStudent(Long courseId, User student) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        
        if (!course.getIsOpen()) {
            throw new BadRequestException("Course is not open for enrollment");
        }
        
        if (course.getEnrolled() >= course.getCapacity()) {
            throw new BadRequestException("Course has reached its capacity");
        }
        
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new BadRequestException("Student is already enrolled in this course");
        }
        
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
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
