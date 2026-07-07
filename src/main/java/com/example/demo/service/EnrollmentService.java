package com.example.demo.service;

import com.example.demo.dto.EnrollCourseRequest;
import com.example.demo.dto.EnrollmentDetail;
import com.example.demo.dto.CourseResponse;
import com.example.demo.dto.CourseInstructorResponse;
import com.example.demo.model.Course;
import com.example.demo.model.CourseStatus;
import com.example.demo.model.Student;
import com.example.demo.model.StudentEnrollment;
import com.example.demo.repository.StudentEnrollmentRepository;
import com.example.demo.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final CourseService courseService;
    private final InstructorService instructorService;
    private final StudentRepository studentRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EnrollmentService(StudentEnrollmentRepository studentEnrollmentRepository,
                             CourseService courseService,
                             InstructorService instructorService,
                             StudentRepository studentRepository,
                             JdbcTemplate jdbcTemplate) {
        this.studentEnrollmentRepository = studentEnrollmentRepository;
        this.courseService = courseService;
        this.instructorService = instructorService;
        this.studentRepository = studentRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<StudentEnrollment> getAllEnrollments() {
        return studentEnrollmentRepository.findAll();
    }

    public List<EnrollmentDetail> getAllEnrollmentsMapped() {
        return studentEnrollmentRepository.findAll().stream()
                .map(this::mapToDetail)
                .toList();
    }

    public StudentEnrollment getEnrollmentById(Long id) {
        return studentEnrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + id));
    }

    public EnrollmentDetail getEnrollmentDetailById(Long id) {
        return mapToDetail(getEnrollmentById(id));
    }

    public StudentEnrollment createEnrollment(StudentEnrollment enrollment) {
        // Resolve student
        Student student = studentRepository.findByName(enrollment.getStudentName()).orElse(null);
        if (student == null) {
            student = new Student();
            student.setName(enrollment.getStudentName());
            student.setEmail(enrollment.getStudentName().toLowerCase().replaceAll("\\s+", "") + "@example.com");
            student = studentRepository.save(student);
        }
        enrollment.setStudent(student);

        // Resolve course
        Course course = courseService.getCourseEntityById(enrollment.getCourseId());
        enrollment.setCourse(course);

        return studentEnrollmentRepository.save(enrollment);
    }

    public StudentEnrollment updateEnrollment(Long id, StudentEnrollment enrollment) {
        StudentEnrollment existing = getEnrollmentById(id);

        // Resolve student
        Student student = studentRepository.findByName(enrollment.getStudentName()).orElse(null);
        if (student == null) {
            student = new Student();
            student.setName(enrollment.getStudentName());
            student.setEmail(enrollment.getStudentName().toLowerCase().replaceAll("\\s+", "") + "@example.com");
            student = studentRepository.save(student);
        }
        existing.setStudent(student);

        // Resolve course
        Course course = courseService.getCourseEntityById(enrollment.getCourseId());
        existing.setCourse(course);

        return studentEnrollmentRepository.save(existing);
    }

    public StudentEnrollment deleteEnrollmentById(Long id) {
        StudentEnrollment existing = getEnrollmentById(id);
        studentEnrollmentRepository.delete(existing);
        return existing;
    }

    public EnrollmentDetail enrollCourse(EnrollCourseRequest request) {
        // 1. Course must exist.
        Course course;
        try {
            course = courseService.getCourseEntityById(request.getCourseId());
        } catch (RuntimeException e) {
            throw new RuntimeException("Course not found");
        }
        if (course == null) {
            throw new RuntimeException("Course not found");
        }

        // 2. Course status must be ACTIVE.
        if (course.getStatus() != CourseStatus.ACTIVE) {
            throw new RuntimeException("Cannot enroll in an inactive course");
        }

        // 3. Instructor of the course must exist.
        try {
            instructorService.getInstructorById(course.getInstructorId());
        } catch (RuntimeException e) {
            throw new RuntimeException("Instructor not found");
        }

        // 4. Resolve or create Student
        Student student = studentRepository.findByName(request.getStudentName()).orElse(null);
        if (student == null) {
            student = new Student();
            student.setName(request.getStudentName());
            student.setEmail(request.getStudentName().toLowerCase().replaceAll("\\s+", "") + "@example.com");
            student = studentRepository.save(student);
        }

        // 5. Save enrollment (use JdbcTemplate if manual ID is provided to bypass JPA merge/detached checks)
        StudentEnrollment enrollment = new StudentEnrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);

        if (request.getId() != null) {
            enrollment.setId(request.getId());
            jdbcTemplate.update("INSERT INTO student_enrollments (id, student_id, course_id) VALUES (?, ?, ?)",
                    request.getId(), student.getId(), course.getId());
        } else {
            enrollment = studentEnrollmentRepository.save(enrollment);
        }

        // 6. Construct and return detail with mapped CourseResponse DTO
        return mapToDetail(enrollment);
    }

    public List<StudentEnrollment> getEnrollmentsByCourseId(Long courseId) {
        return studentEnrollmentRepository.findByCourse_Id(courseId);
    }

    public EnrollmentDetail mapToDetail(StudentEnrollment enrollment) {
        if (enrollment == null) return null;

        CourseResponse courseResp = null;
        if (enrollment.getCourse() != null) {
            Course course = enrollment.getCourse();
            CourseInstructorResponse instructorResp = null;
            if (course.getInstructor() != null) {
                instructorResp = new CourseInstructorResponse(
                        course.getInstructor().getId(),
                        course.getInstructor().getName()
                );
            }
            courseResp = new CourseResponse(
                    course.getId(),
                    course.getTitle(),
                    course.getStatus(),
                    instructorResp
            );
        }
        return new EnrollmentDetail(
                enrollment.getId(),
                enrollment.getStudentName(),
                courseResp
        );
    }
}
