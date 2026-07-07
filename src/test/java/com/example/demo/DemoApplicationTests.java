package com.example.demo;

import com.example.demo.controller.*;
import com.example.demo.model.Course;
import com.example.demo.model.CourseStatus;
import com.example.demo.model.Instructor;
import com.example.demo.model.Student;
import com.example.demo.model.StudentEnrollment;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.InstructorRepository;
import com.example.demo.repository.StudentEnrollmentRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private InstructorService instructorService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentEnrollmentService studentEnrollmentService;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentEnrollmentRepository studentEnrollmentRepository;

    private MockMvc mockInstructorMvc;
    private MockMvc mockCourseMvc;
    private MockMvc mockEnrollmentMvc;
    private MockMvc mockStudentMvc;
    private MockMvc mockStudentEnrollmentMvc;

    // Fields to store saved entity IDs to guarantee deterministic test runs
    private Long inst1Id;
    private Long inst2Id;
    private Long student1Id;
    private Long student2Id;
    private Long course1Id;
    private Long course2Id;

    @BeforeEach
    void setUp() {
        // Reset and seed database state before each test to ensure complete isolation
        studentEnrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        instructorRepository.deleteAll();

        Instructor inst1 = instructorRepository.save(new Instructor(null, "Dr. John Doe", "john.doe@example.com", new ArrayList<>()));
        Instructor inst2 = instructorRepository.save(new Instructor(null, "Prof. Jane Smith", "jane.smith@example.com", new ArrayList<>()));
        inst1Id = inst1.getId();
        inst2Id = inst2.getId();

        Student s1 = studentRepository.save(new Student(null, "Alice Johnson", "alice.johnson@example.com", new ArrayList<>()));
        Student s2 = studentRepository.save(new Student(null, "Bob Miller", "bob.miller@example.com", new ArrayList<>()));
        student1Id = s1.getId();
        student2Id = s2.getId();

        Course c1 = courseRepository.save(new Course(null, "Spring Boot Framework", CourseStatus.ACTIVE, inst1, new ArrayList<>()));
        Course c2 = courseRepository.save(new Course(null, "Relational Database Design", CourseStatus.ACTIVE, inst2, new ArrayList<>()));
        course1Id = c1.getId();
        course2Id = c2.getId();

        studentEnrollmentRepository.save(new StudentEnrollment(null, s1, c1));
        studentEnrollmentRepository.save(new StudentEnrollment(null, s2, c2));

        mockInstructorMvc = MockMvcBuilders.standaloneSetup(new InstructorController(instructorService)).build();
        mockCourseMvc = MockMvcBuilders.standaloneSetup(new CourseController(courseService)).build();
        mockEnrollmentMvc = MockMvcBuilders.standaloneSetup(new EnrollmentController(enrollmentService)).build();
        mockStudentMvc = MockMvcBuilders.standaloneSetup(new StudentController(studentService)).build();
        mockStudentEnrollmentMvc = MockMvcBuilders.standaloneSetup(new StudentEnrollmentController(studentEnrollmentService)).build();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testInstructorCrud() throws Exception {
        // 1. Get all instructors
        mockInstructorMvc.perform(get("/instructors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Dr. John Doe"));

        // 2. Get instructor by ID (success and not found)
        mockInstructorMvc.perform(get("/instructors/" + inst1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Dr. John Doe"));

        mockInstructorMvc.perform(get("/instructors/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty());

        // 3. Create instructor (success and bad request) -> returns data: null
        String newInstructor = """
                {
                    "name": "Prof. David Clark",
                    "email": "david.clark@example.com"
                }
                """;
        mockInstructorMvc.perform(post("/instructors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newInstructor))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        String badInstructor = """
                {
                    "name": "",
                    "email": "bad@example.com"
                }
                """;
        mockInstructorMvc.perform(post("/instructors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badInstructor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty());

        // 4. Update instructor (success and not found)
        String updatedInstructor = """
                {
                    "name": "Dr. Johnathan Doe",
                    "email": "johnathan.doe@example.com"
                }
                """;
        mockInstructorMvc.perform(put("/instructors/" + inst1Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedInstructor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Dr. Johnathan Doe"));

        mockInstructorMvc.perform(put("/instructors/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedInstructor))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        // 5. Delete instructor (success and not found)
        // Deleting instructor with courses should fail
        mockInstructorMvc.perform(delete("/instructors/" + inst2Id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot delete instructor: Instructor has active courses"));

        // Delete the course first
        mockCourseMvc.perform(delete("/courses/" + course2Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Now deleting the instructor should succeed
        mockInstructorMvc.perform(delete("/instructors/" + inst2Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Prof. Jane Smith"));

        mockInstructorMvc.perform(delete("/instructors/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testCourseCrud() throws Exception {
        // 1. Get all courses
        mockCourseMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));

        // 2. Get course by ID (success and not found)
        mockCourseMvc.perform(get("/courses/" + course1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Spring Boot Framework"));

        mockCourseMvc.perform(get("/courses/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        // 3. Create course (success and bad request) -> returns data: null
        String newCourse = String.format("""
                {
                    "title": "Advanced Web Dev",
                    "status": "ACTIVE",
                    "instructorId": %d
                }
                """, inst1Id);
        mockCourseMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCourse))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        String badCourse = """
                {
                    "title": "",
                    "status": "ACTIVE"
                }
                """;
        mockCourseMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badCourse))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testEnrollmentCrud() throws Exception {
        // 1. Get all enrollments
        mockEnrollmentMvc.perform(get("/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));

        // 2. Get enrollment by ID
        Long firstEnrollmentId = studentEnrollmentRepository.findAll().get(0).getId();
        mockEnrollmentMvc.perform(get("/enrollments/" + firstEnrollmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentName").value("Alice Johnson"));

        // 3. Create enrollment (success and bad request)
        String newEnrollment = String.format("""
                {
                    "studentName": "Charlie Green",
                    "courseId": %d
                }
                """, course2Id);
        mockEnrollmentMvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newEnrollment))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentName").value("Charlie Green"));

        String badEnrollment = """
                {
                    "studentName": "",
                    "courseId": null
                }
                """;
        mockEnrollmentMvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badEnrollment))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testEnrollCourseBusinessLogic() throws Exception {
        // 1. Successful enrollment
        String successRequest = String.format("""
                {
                    "id": 100,
                    "studentName": "Student test",
                    "courseId": %d
                }
                """, course1Id);
        mockEnrollmentMvc.perform(post("/enrollments/enroll-course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(successRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Enrollment successful"))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.studentName").value("Student test"))
                .andExpect(jsonPath("$.data.course.id").value(course1Id))
                .andExpect(jsonPath("$.data.course.title").value("Spring Boot Framework"));

        // 2. Failure: Inactive course
        // First make Course 2 INACTIVE
        String inactiveCourseUpdate = String.format("""
                {
                    "title": "Relational Database Design",
                    "status": "INACTIVE",
                    "instructorId": %d
                }
                """, inst2Id);
        mockCourseMvc.perform(put("/courses/" + course2Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inactiveCourseUpdate))
                .andExpect(status().isOk());

        // Then try to enroll in Course 2
        String enrollInactiveRequest = String.format("""
                {
                    "id": 101,
                    "studentName": "Student test",
                    "courseId": %d
                }
                """, course2Id);
        mockEnrollmentMvc.perform(post("/enrollments/enroll-course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(enrollInactiveRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot enroll in an inactive course"))
                .andExpect(jsonPath("$.data").isEmpty());

        // 3. Failure: Course not found
        String enrollNonExistentRequest = """
                {
                    "id": 102,
                    "studentName": "Student test",
                    "courseId": 9999
                }
                """;
        mockEnrollmentMvc.perform(post("/enrollments/enroll-course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(enrollNonExistentRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Course not found"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testInstructorDetailsBusinessQuery() throws Exception {
        // Initially:
        // Course 1 is ACTIVE and has Enrollment 1 (Alice Johnson) -> Instructor 1 (Dr. John Doe) should have Course 1
        // Course 2 is ACTIVE and has Enrollment 2 (Bob Miller) -> Instructor 2 (Prof. Jane Smith) should have Course 2
        mockInstructorMvc.perform(get("/instructors/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].courses.length()").value(1))
                .andExpect(jsonPath("$.data[0].courses[0].title").value("Spring Boot Framework"))
                .andExpect(jsonPath("$.data[1].courses.length()").value(1))
                .andExpect(jsonPath("$.data[1].courses[0].title").value("Relational Database Design"));

        // 1. If Course 2 is made INACTIVE -> it should be filtered out
        String inactiveCourseUpdate = String.format("""
                {
                    "title": "Relational Database Design",
                    "status": "INACTIVE",
                    "instructorId": %d
                }
                """, inst2Id);
        mockCourseMvc.perform(put("/courses/" + course2Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inactiveCourseUpdate))
                .andExpect(status().isOk());

        mockInstructorMvc.perform(get("/instructors/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[1].courses.length()").value(0));

        // 2. If we add a new course "Course 3" under Instructor 1 with status ACTIVE but 0 enrollments -> it should NOT appear
        String newCourseWithoutEnrollment = String.format("""
                {
                    "title": "Active Course without Enrollments",
                    "status": "ACTIVE",
                    "instructorId": %d
                }
                """, inst1Id);
        mockCourseMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCourseWithoutEnrollment))
                .andExpect(status().isCreated());

        mockInstructorMvc.perform(get("/instructors/details"))
                .andExpect(status().isOk())
                // Course 3 is ignored, so Instructor 1 still has only 1 course (Course 1)
                .andExpect(jsonPath("$.data[0].courses.length()").value(1))
                .andExpect(jsonPath("$.data[0].courses[0].title").value("Spring Boot Framework"));
    }

    @Test
    void testStudentEnrollmentBusinessWorkflow() throws Exception {
        // 1. POST /instructors -> Create a new instructor
        String instructorJson = """
                {
                    "name": "Instructor Test",
                    "email": "inst.test@example.com"
                }
                """;
        mockInstructorMvc.perform(post("/instructors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instructorJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        Instructor instructor = instructorRepository.findAll().stream()
                .filter(i -> i.getEmail().equals("inst.test@example.com"))
                .findFirst().orElseThrow();
        Long newInstId = instructor.getId();

        // 2. POST /courses -> Create a new course under that instructor
        String courseJson = String.format("""
                {
                    "title": "Business Logic Course",
                    "status": "ACTIVE",
                    "instructorId": %d
                }
                """, newInstId);
        mockCourseMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        Course course = courseRepository.findAll().stream()
                .filter(c -> c.getTitle().equals("Business Logic Course"))
                .findFirst().orElseThrow();
        Long newCourseId = course.getId();

        // 3. PUT /courses/{id} -> Update status of course
        String updateJson = String.format("""
                {
                    "title": "Business Logic Course Updated",
                    "status": "ACTIVE",
                    "instructorId": %d
                }
                """, newInstId);
        mockCourseMvc.perform(put("/courses/" + newCourseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        // 4. POST /students -> Create a student
        String studentJson = """
                {
                    "name": "Student Test Workflow",
                    "email": "student.test.workflow@example.com"
                }
                """;
        mockStudentMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        Student student = studentRepository.findByName("Student Test Workflow").orElseThrow();
        Long newStudentId = student.getId();

        // 5. POST /students-enrollments -> Enroll the student in the course
        String enrollJson = String.format("""
                {
                    "studentId": %d,
                    "courseId": %d
                }
                """, newStudentId, newCourseId);
        mockStudentEnrollmentMvc.perform(post("/students-enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(enrollJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        // Verify state in DB
        boolean enrolled = studentEnrollmentRepository.findAll().stream()
                .anyMatch(se -> se.getStudent().getId().equals(newStudentId) && se.getCourse().getId().equals(newCourseId));
        org.junit.jupiter.api.Assertions.assertTrue(enrolled);
    }
}
