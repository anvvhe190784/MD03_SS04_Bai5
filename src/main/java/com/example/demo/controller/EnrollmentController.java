package com.example.demo.controller;

import com.example.demo.dto.EnrollCourseRequest;
import com.example.demo.dto.EnrollmentDetail;
import com.example.demo.model.ApiResponse;
import com.example.demo.model.StudentEnrollment;
import com.example.demo.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EnrollmentDetail>>> getAllEnrollments() {
        List<EnrollmentDetail> list = enrollmentService.getAllEnrollmentsMapped();
        return ResponseEntity.ok(new ApiResponse<>(true, "Get all enrollments successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentDetail>> getEnrollmentById(@PathVariable Long id) {
        try {
            EnrollmentDetail enrollment = enrollmentService.getEnrollmentDetailById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Get enrollment by ID successfully", enrollment));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentDetail>> createEnrollment(@RequestBody StudentEnrollment enrollment) {
        if (enrollment == null || enrollment.getStudentName() == null || enrollment.getStudentName().trim().isEmpty() ||
                enrollment.getCourseId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid enrollment inputs: Student name and courseId are required", null));
        }
        StudentEnrollment created = enrollmentService.createEnrollment(enrollment);
        EnrollmentDetail detail = enrollmentService.mapToDetail(created);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Enrollment created successfully", detail));
    }

    @PostMapping("/enroll-course")
    public ResponseEntity<ApiResponse<EnrollmentDetail>> enrollCourse(@RequestBody EnrollCourseRequest request) {
        if (request == null || request.getStudentName() == null || request.getStudentName().trim().isEmpty() ||
                request.getCourseId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid enrollment inputs: Student name and courseId are required", null));
        }
        try {
            EnrollmentDetail detail = enrollmentService.enrollCourse(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Enrollment successful", detail));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentDetail>> updateEnrollment(@PathVariable Long id, @RequestBody StudentEnrollment enrollment) {
        if (enrollment == null || enrollment.getStudentName() == null || enrollment.getStudentName().trim().isEmpty() ||
                enrollment.getCourseId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid enrollment inputs: Student name and courseId are required", null));
        }
        try {
            StudentEnrollment updated = enrollmentService.updateEnrollment(id, enrollment);
            EnrollmentDetail detail = enrollmentService.mapToDetail(updated);
            return ResponseEntity.ok(new ApiResponse<>(true, "Enrollment updated successfully", detail));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentDetail>> deleteEnrollmentById(@PathVariable Long id) {
        try {
            StudentEnrollment deleted = enrollmentService.deleteEnrollmentById(id);
            EnrollmentDetail detail = enrollmentService.mapToDetail(deleted);
            return ResponseEntity.ok(new ApiResponse<>(true, "Enrollment deleted successfully", detail));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
