package com.stdiscm.course.controller;

import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.dto.CourseDto;
// import com.stdiscm.common.model.User; // No longer needed directly here
import com.stdiscm.course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    @GetMapping
    public ResponseEntity<?> getAllCourses() {
        List<CourseDto> courses = courseService.getAllCourses();
        return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
    }
    
    @GetMapping("/open")
    public ResponseEntity<?> getOpenCourses() {
        List<CourseDto> courses = courseService.getOpenCourses();
        return ResponseEntity.ok(ApiResponse.success("Open courses retrieved successfully", courses));
    }
    
    @GetMapping("/public")
    public ResponseEntity<?> getPublicCourseList() {
        List<CourseDto> courses = courseService.getOpenCourses();
        return ResponseEntity.ok(ApiResponse.success("Public course list retrieved successfully", courses));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        CourseDto course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success("Course retrieved successfully", course));
    }
    
    @GetMapping("/code/{courseCode}")
    public ResponseEntity<?> getCourseByCode(@PathVariable String courseCode) {
        CourseDto course = courseService.getCourseByCode(courseCode);
        return ResponseEntity.ok(ApiResponse.success("Course retrieved successfully", course));
    }
    
    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<?> getCoursesByFacultyId(@PathVariable Long facultyId) {
        List<CourseDto> courses = courseService.getCoursesByFacultyId(facultyId);
        return ResponseEntity.ok(ApiResponse.success("Faculty courses retrieved successfully", courses));
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@Valid @RequestBody CourseDto courseDto, @RequestHeader("X-User-Id") Long userId) {
        System.out.println("--- CourseController.createCourse ENTERED ---"); // Log entry into controller method
        System.out.println("--- Received DTO: " + courseDto);
        System.out.println("--- Received User ID: " + userId);
        CourseDto createdCourse = courseService.createCourse(courseDto, userId); 
        System.out.println("--- CourseService.createCourse finished ---");
        return ResponseEntity.ok(ApiResponse.success("Course created successfully", createdCourse));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
        CourseDto updatedCourse = courseService.updateCourse(id, courseDto);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", updatedCourse));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully"));
    }
    
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleCourseStatus(@PathVariable Long id) {
        courseService.toggleCourseStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Course status toggled successfully"));
    }
}
