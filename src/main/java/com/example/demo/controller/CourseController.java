package com.example.demo.controller;

import com.example.demo.dto.CourseCreateRequest;
import com.example.demo.dto.CourseUpdateRequest;
import com.example.demo.dto.CourseResponse;
import com.example.demo.model.ApiResponse;
import com.example.demo.model.Course;
import com.example.demo.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses() {
        List<CourseResponse> list = courseService.getAllCourses();
        return ResponseEntity.ok(new ApiResponse<>(true, "Get all courses successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long id) {
        try {
            CourseResponse course = courseService.getCourseById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Get course by ID successfully", course));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Course>> createCourse(@RequestBody CourseCreateRequest course) {
        if (course == null || course.getTitle() == null || course.getTitle().trim().isEmpty() ||
                course.getStatus() == null || course.getInstructorId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid course inputs: Title, status, and instructorId are required", null));
        }
        try {
            courseService.createCourse(course);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Course created successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(@PathVariable Long id, @RequestBody CourseUpdateRequest course) {
        if (course == null || course.getTitle() == null || course.getTitle().trim().isEmpty() ||
                course.getStatus() == null || course.getInstructorId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid course inputs: Title, status, and instructorId are required", null));
        }
        try {
            courseService.updateCourse(id, course);
            return ResponseEntity.ok(new ApiResponse<>(true, "Course updated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> deleteCourseById(@PathVariable Long id) {
        try {
            Course deleted = courseService.deleteCourseById(id);
            CourseResponse response = courseService.mapToResponse(deleted);
            return ResponseEntity.ok(new ApiResponse<>(true, "Course deleted successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
