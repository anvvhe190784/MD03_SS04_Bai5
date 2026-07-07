package com.example.demo.service;

import com.example.demo.dto.InstructorCreateRequest;
import com.example.demo.dto.InstructorDetail;
import com.example.demo.dto.InstructorResponse;
import com.example.demo.dto.CourseResponse;
import com.example.demo.dto.CourseInstructorResponse;
import com.example.demo.model.Course;
import com.example.demo.model.CourseStatus;
import com.example.demo.model.Instructor;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.StudentEnrollmentRepository;
import com.example.demo.repository.InstructorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InstructorService {
    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;

    @Autowired
    public InstructorService(InstructorRepository instructorRepository,
                             CourseRepository courseRepository,
                             StudentEnrollmentRepository studentEnrollmentRepository) {
        this.instructorRepository = instructorRepository;
        this.courseRepository = courseRepository;
        this.studentEnrollmentRepository = studentEnrollmentRepository;
    }

    public List<InstructorResponse> getAllInstructors() {
        return instructorRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Instructor getInstructorById(Long id) {
        return instructorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instructor not found with ID: " + id));
    }

    public InstructorResponse getInstructorResponseById(Long id) {
        return mapToResponse(getInstructorById(id));
    }

    public Instructor createInstructor(InstructorCreateRequest req) {
        Instructor instructor = new Instructor();
        instructor.setName(req.getName());
        instructor.setEmail(req.getEmail());
        return instructorRepository.save(instructor);
    }

    public Instructor updateInstructor(Long id, Instructor instructor) {
        Instructor existing = getInstructorById(id);
        existing.setName(instructor.getName());
        existing.setEmail(instructor.getEmail());
        return instructorRepository.save(existing);
    }

    public Instructor deleteInstructorById(Long id) {
        Instructor existing = getInstructorById(id);
        List<Course> courses = courseRepository.findByInstructor_Id(id);
        boolean hasActiveCourses = courses.stream()
                .anyMatch(course -> course.getStatus() == CourseStatus.ACTIVE);
        if (hasActiveCourses) {
            throw new RuntimeException("Cannot delete instructor: Instructor has active courses");
        }
        instructorRepository.delete(existing);
        return existing;
    }

    public List<InstructorDetail> getDetailedInstructors() {
        List<Instructor> instructors = instructorRepository.findAll();
        List<InstructorDetail> details = new ArrayList<>();

        for (Instructor instructor : instructors) {
            List<Course> courses = courseRepository.findByInstructor_Id(instructor.getId());
            List<CourseResponse> courseResponses = courses.stream()
                    .filter(course -> course.getStatus() == CourseStatus.ACTIVE)
                    .filter(course -> !studentEnrollmentRepository.findByCourse_Id(course.getId()).isEmpty())
                    .map(this::mapCourseToResponse)
                    .toList();

            details.add(new InstructorDetail(
                    instructor.getId(),
                    instructor.getName(),
                    instructor.getEmail(),
                    courseResponses
            ));
        }

        return details;
    }

    public InstructorResponse mapToResponse(Instructor instructor) {
        if (instructor == null) return null;
        return new InstructorResponse(
                instructor.getId(),
                instructor.getName(),
                instructor.getEmail()
        );
    }

    private CourseResponse mapCourseToResponse(Course course) {
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
