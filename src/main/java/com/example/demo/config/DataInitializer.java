package com.example.demo.config;

import com.example.demo.model.Course;
import com.example.demo.model.CourseStatus;
import com.example.demo.model.Instructor;
import com.example.demo.model.Student;
import com.example.demo.model.StudentEnrollment;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.InstructorRepository;
import com.example.demo.repository.StudentEnrollmentRepository;
import com.example.demo.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class DataInitializer implements CommandLineRunner {
    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;

    public DataInitializer(InstructorRepository instructorRepository,
                           CourseRepository courseRepository,
                           StudentRepository studentRepository,
                           StudentEnrollmentRepository studentEnrollmentRepository) {
        this.instructorRepository = instructorRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.studentEnrollmentRepository = studentEnrollmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (instructorRepository.count() == 0) {
            Instructor inst1 = new Instructor(null, "Dr. John Doe", "john.doe@example.com", new ArrayList<>());
            Instructor inst2 = new Instructor(null, "Prof. Jane Smith", "jane.smith@example.com", new ArrayList<>());
            inst1 = instructorRepository.save(inst1);
            inst2 = instructorRepository.save(inst2);

            Student s1 = new Student(null, "Alice Johnson", "alice.johnson@example.com", new ArrayList<>());
            Student s2 = new Student(null, "Bob Miller", "bob.miller@example.com", new ArrayList<>());
            s1 = studentRepository.save(s1);
            s2 = studentRepository.save(s2);

            Course c1 = new Course(null, "Spring Boot Framework", CourseStatus.ACTIVE, inst1, new ArrayList<>());
            Course c2 = new Course(null, "Relational Database Design", CourseStatus.ACTIVE, inst2, new ArrayList<>());
            c1 = courseRepository.save(c1);
            c2 = courseRepository.save(c2);

            StudentEnrollment se1 = new StudentEnrollment(null, s1, c1);
            StudentEnrollment se2 = new StudentEnrollment(null, s2, c2);
            studentEnrollmentRepository.save(se1);
            studentEnrollmentRepository.save(se2);
        }
    }
}
