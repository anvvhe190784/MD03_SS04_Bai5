package com.example.demo.controller;

import com.example.demo.dto.InstructorCreateRequest;
import com.example.demo.dto.InstructorDetail;
import com.example.demo.dto.InstructorResponse;
import com.example.demo.model.ApiResponse;
import com.example.demo.model.Instructor;
import com.example.demo.service.InstructorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instructors")
public class InstructorController {
    private final InstructorService instructorService;

    @Autowired
    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InstructorResponse>>> getAllInstructors() {
        List<InstructorResponse> list = instructorService.getAllInstructors();
        return ResponseEntity.ok(new ApiResponse<>(true, "Get all instructors successfully", list));
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<List<InstructorDetail>>> getDetailedInstructors() {
        List<InstructorDetail> list = instructorService.getDetailedInstructors();
        return ResponseEntity.ok(new ApiResponse<>(true, "Get detailed instructors successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InstructorResponse>> getInstructorById(@PathVariable Long id) {
        try {
            InstructorResponse instructor = instructorService.getInstructorResponseById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Get instructor by ID successfully", instructor));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InstructorResponse>> createInstructor(@RequestBody InstructorCreateRequest instructor) {
        if (instructor == null || instructor.getName() == null || instructor.getName().trim().isEmpty() ||
                instructor.getEmail() == null || instructor.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid instructor inputs: Name and email are required", null));
        }
        instructorService.createInstructor(instructor);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Instructor created successfully", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InstructorResponse>> updateInstructor(@PathVariable Long id, @RequestBody Instructor instructor) {
        if (instructor == null || instructor.getName() == null || instructor.getName().trim().isEmpty() ||
                instructor.getEmail() == null || instructor.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid instructor inputs: Name and email are required", null));
        }
        try {
            Instructor updated = instructorService.updateInstructor(id, instructor);
            InstructorResponse response = instructorService.mapToResponse(updated);
            return ResponseEntity.ok(new ApiResponse<>(true, "Instructor updated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<InstructorResponse>> deleteInstructorById(@PathVariable Long id) {
        try {
            Instructor deleted = instructorService.deleteInstructorById(id);
            InstructorResponse response = instructorService.mapToResponse(deleted);
            return ResponseEntity.ok(new ApiResponse<>(true, "Instructor deleted successfully", response));
        } catch (RuntimeException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            if (e.getMessage() != null && e.getMessage().contains("Cannot delete")) {
                status = HttpStatus.BAD_REQUEST;
            }
            return ResponseEntity.status(status)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
