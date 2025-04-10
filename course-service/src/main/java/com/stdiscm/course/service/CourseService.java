package com.stdiscm.course.service;

import com.stdiscm.common.dto.CourseDto;
import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.exception.BadRequestException;
import com.stdiscm.common.exception.ResourceNotFoundException;
import com.stdiscm.common.model.Course;
import com.stdiscm.common.model.User;
import com.stdiscm.course.client.AuthServiceClient;
import com.stdiscm.course.repository.CourseRepository;
import com.stdiscm.course.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseService {
    
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private AuthServiceClient authServiceClient;
    
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Updated to accept studentId to check enrollment status
    public List<CourseDto> getOpenCourses(Long studentId) {
        // 1. Fetch all open courses
        List<Course> openCourses = courseRepository.findByIsOpenTrue();

        // 2. Fetch the IDs of courses the student is actively enrolled in, only if studentId is provided
        final Set<Long> finalEnrolledCourseIds;
        if (studentId != null) {
            finalEnrolledCourseIds = enrollmentRepository.findActiveCourseIdsByStudentId(studentId);
        } else {
            finalEnrolledCourseIds = Collections.emptySet();
        }

        // 3. Convert to DTO, passing the final set of enrolled IDs
        return openCourses.stream()
                .map(course -> convertToDto(course, finalEnrolledCourseIds))
                .collect(Collectors.toList());
    }
    
    public List<CourseDto> getCoursesByFacultyId(Long facultyId) {
        return courseRepository.findByFacultyId(facultyId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return convertToDto(course);
    }
    
    public CourseDto getCourseByCode(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "code", courseCode));
        return convertToDto(course);
    }
    
    @Transactional
    public CourseDto createCourse(CourseDto courseDto, Long facultyId) {
        // Check if course code already exists
        if (courseRepository.existsByCourseCode(courseDto.getCourseCode())) {
            throw new BadRequestException("Course code already exists: " + courseDto.getCourseCode());
        }

        // Create and save the course
        Course course = new Course();
        course.setCourseCode(courseDto.getCourseCode());
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setCredits(courseDto.getCredits());
        course.setCapacity(courseDto.getCapacity());
        course.setEnrolled(0); 
        course.setIsOpen(true);
        course.setFacultyId(facultyId);

        Course savedCourse = courseRepository.save(course);
        return convertToDto(savedCourse);
    }
    
    @Transactional
    public CourseDto updateCourse(Long id, CourseDto courseDto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        
        if (!course.getCourseCode().equals(courseDto.getCourseCode()) 
                && courseRepository.existsByCourseCode(courseDto.getCourseCode())) {
            throw new BadRequestException("Course code already exists");
        }
        
        course.setCourseCode(courseDto.getCourseCode());
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setCredits(courseDto.getCredits());
        course.setCapacity(courseDto.getCapacity());
        course.setIsOpen(courseDto.getIsOpen());
        
        Course updatedCourse = courseRepository.save(course);
        return convertToDto(updatedCourse);
    }
    
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        
        if (course.getEnrolled() > 0) {
            throw new BadRequestException("Cannot delete course with enrolled students");
        }
        
        courseRepository.delete(course);
    }
    
    @Transactional
    public void toggleCourseStatus(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        
        course.setIsOpen(!course.getIsOpen());
        courseRepository.save(course);
    }
    
    @Transactional
    public void incrementEnrolled(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        
        if (!course.getIsOpen()) {
            throw new BadRequestException("Course is not open for enrollment");
        }
        
        if (course.getEnrolled() >= course.getCapacity()) {
            throw new BadRequestException("Course has reached its capacity");
        }
        
        course.setEnrolled(course.getEnrolled() + 1);
        courseRepository.save(course);
    }
    
    @Transactional
    public void decrementEnrolled(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        
        if (course.getEnrolled() > 0) {
            course.setEnrolled(course.getEnrolled() - 1);
            courseRepository.save(course);
        }
    }

    // Overloaded method to check enrollment status
    private CourseDto convertToDto(Course course, Set<Long> enrolledCourseIds) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setCourseCode(course.getCourseCode());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setCredits(course.getCredits());
        dto.setCapacity(course.getCapacity());
        dto.setEnrolled(course.getEnrolled());
        dto.setIsOpen(course.getIsOpen());
        dto.setFacultyId(course.getFacultyId());

        // Fetch and set facultyName using AuthServiceClient
        if (course.getFacultyId() != null) {
            try {
                ResponseEntity<ApiResponse<User>> response = authServiceClient.getUserById(course.getFacultyId());
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getData() != null) {
                    dto.setFacultyName(response.getBody().getData().getFullName());
                } else {
                    dto.setFacultyName("N/A"); 
                }
            } catch (Exception e) {
                dto.setFacultyName("Error");
            }
        } else {
            dto.setFacultyName("N/A");
        }
        
        dto.setHasAvailableSlots(course.hasAvailableSlots());

        // Set the new flag based on the provided set of enrolled course IDs
        dto.setCurrentUserEnrolled(enrolledCourseIds != null && enrolledCourseIds.contains(course.getId()));

        return dto;
    }

    // Original method now calls the overloaded one with an empty set
    private CourseDto convertToDto(Course course) {
        return convertToDto(course, Collections.emptySet());
    }
}
