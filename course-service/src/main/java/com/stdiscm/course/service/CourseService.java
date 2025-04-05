package com.stdiscm.course.service;

import com.stdiscm.common.dto.CourseDto;
import com.stdiscm.common.exception.BadRequestException;
import com.stdiscm.common.exception.ResourceNotFoundException;
import com.stdiscm.common.model.Course;
import com.stdiscm.common.model.User;
import com.stdiscm.course.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<CourseDto> getOpenCourses() {
        return courseRepository.findByIsOpenTrue().stream()
                .map(this::convertToDto)
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
    public CourseDto createCourse(CourseDto courseDto, User faculty) {
        if (courseRepository.existsByCourseCode(courseDto.getCourseCode())) {
            throw new BadRequestException("Course code already exists");
        }
        
        Course course = new Course();
        course.setCourseCode(courseDto.getCourseCode());
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setCredits(courseDto.getCredits());
        course.setCapacity(courseDto.getCapacity());
        course.setEnrolled(0);
        course.setIsOpen(true);
        course.setFaculty(faculty);
        
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
    
    private CourseDto convertToDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setCourseCode(course.getCourseCode());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setCredits(course.getCredits());
        dto.setCapacity(course.getCapacity());
        dto.setEnrolled(course.getEnrolled());
        dto.setIsOpen(course.getIsOpen());
        
        if (course.getFaculty() != null) {
            dto.setFacultyId(course.getFaculty().getId());
            dto.setFacultyName(course.getFaculty().getFullName());
        }
        
        dto.setHasAvailableSlots(course.hasAvailableSlots());
        
        return dto;
    }
}
