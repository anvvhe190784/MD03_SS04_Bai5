package com.example.demo.controller;

import com.example.demo.dto.StudentEnrollmentRequest;
import com.example.demo.model.ApiResponse;
import com.example.demo.model.StudentEnrollment;
import com.example.demo.service.StudentEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students-enrollments")
public class StudentEnrollmentController {
    private final StudentEnrollmentService studentEnrollmentService;

    @Autowired
    public StudentEnrollmentController(StudentEnrollmentService studentEnrollmentService) {
        this.studentEnrollmentService = studentEnrollmentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentEnrollment>> enrollStudent(@RequestBody StudentEnrollmentRequest request) {
        if (request == null || request.getStudentId() == null || request.getCourseId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid inputs: studentId and courseId are required", null));
        }
        try {
            studentEnrollmentService.enrollStudent(request.getStudentId(), request.getCourseId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Student enrolled in course successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
