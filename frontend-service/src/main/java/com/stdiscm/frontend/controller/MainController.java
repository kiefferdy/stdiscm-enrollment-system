package com.stdiscm.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
    
    @GetMapping("/courses")
    public String courses() {
        return "courses";
    }
    
    @GetMapping("/enrollments")
    public String enrollments() {
        return "enrollments";
    }
    
    @GetMapping("/grades")
    public String grades() {
        return "grades";
    }
    
    @GetMapping("/faculty/courses")
    public String facultyCourses() {
        return "faculty/courses";
    }
    
    @GetMapping("/faculty/grades")
    public String facultyGrades() {
        return "faculty/grades";
    }
}
