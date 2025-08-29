package edu.service.IMP;

import edu.model.DTO.CourseDTO;
import edu.model.DTO.CourseDTO5TOP;
import edu.model.entity.Course;
import edu.model.entity.RegistrationStatus;
import edu.repo.CourseRepository;
import edu.repo.RegistrationRepository;
import edu.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Override
    public Page<Course> getAllCoursesForAdmin(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Override
    public Page<Course> getAllCoursesForUser(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isEmpty()) {
            return courseRepository.findAll(pageable);
        }
        return courseRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    @Override
    public long countStudentsByCourseId(Long courseId) {
        return registrationRepository.countByCourse(
                courseRepository.findById(courseId).orElseThrow(
                        () -> new RuntimeException("Không tìm thấy khóa học")
                )
        );
    }

    @Override
    public Course save(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public Course findById(Long id) {
        Optional<Course> course = courseRepository.findById(id);

        return course.orElseThrow(() -> new RuntimeException("không tìm thấy khóa học"));
    }

    @Override
    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    public List<CourseDTO5TOP> getTopCourses(Pageable pageable) {
        return courseRepository.findTopCourses(pageable);
    }

    public boolean deleteCourse(Long courseId) {
        boolean hasActiveRegistrations = registrationRepository.existsByCourseIdAndStatusIn(
                courseId,
                List.of(RegistrationStatus.WAITING, RegistrationStatus.CONFIRM)
        );
        if (hasActiveRegistrations) {
            return false;
        }
        courseRepository.deleteById(courseId);
        return true;

    }

    @Override
    public Course findCourseByName(String name) {
        return courseRepository.findCourseByNameContaining(name);
    }

}
