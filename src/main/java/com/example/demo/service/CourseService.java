package com.example.demo.service;

import com.example.demo.dto.CourseCreateRequest;
import com.example.demo.dto.CourseInstructorResponse;
import com.example.demo.dto.CourseResponse;
import com.example.demo.dto.CourseUpdateRequest;
import com.example.demo.model.Course;
import com.example.demo.model.Instructor;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.InstructorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository, InstructorRepository instructorRepository) {
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
    }

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Course getCourseEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));
    }

    public CourseResponse getCourseById(Long id) {
        Course course = getCourseEntityById(id);
        return mapToResponse(course);
    }

    public Course createCourse(CourseCreateRequest req) {
        Instructor instructor = instructorRepository.findById(req.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Instructor not found with ID: " + req.getInstructorId()));

        Course course = new Course();
        course.setTitle(req.getTitle());
        course.setStatus(req.getStatus());
        course.setInstructor(instructor);

        return courseRepository.save(course);
    }

    public Course updateCourse(Long id, CourseUpdateRequest req) {
        Course existing = getCourseEntityById(id);
        Instructor instructor = instructorRepository.findById(req.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Instructor not found with ID: " + req.getInstructorId()));

        existing.setTitle(req.getTitle());
        existing.setStatus(req.getStatus());
        existing.setInstructor(instructor);

        return courseRepository.save(existing);
    }

    public Course deleteCourseById(Long id) {
        Course existing = getCourseEntityById(id);
        courseRepository.delete(existing);
        return existing;
    }

    public List<Course> getCoursesByInstructorId(Long instructorId) {
        return courseRepository.findByInstructor_Id(instructorId);
    }

    public CourseResponse mapToResponse(Course course) {
        CourseInstructorResponse instructorResp = null;
        if (course.getInstructor() != null) {
            instructorResp = new CourseInstructorResponse(
                    course.getInstructor().getId(),
                    course.getInstructor().getName()
            );
        }
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getStatus(),
                instructorResp
        );
    }
}
