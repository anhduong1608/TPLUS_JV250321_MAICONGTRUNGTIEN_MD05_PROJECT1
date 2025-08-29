package edu.service;

import edu.model.DTO.CourseDTO;
import edu.model.DTO.CourseDTO5TOP;
import edu.model.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseService {

    Page<Course> getAllCoursesForAdmin(Pageable pageable);

    Page<Course> getAllCoursesForUser(String keyword, Pageable pageable);

    long countStudentsByCourseId(Long courseId);

    Course save(Course course);
    Course findById(Long id);
    void deleteById(Long id);
    List<CourseDTO5TOP> getTopCourses(Pageable pageable);
    public boolean deleteCourse(Long courseId) ;
    Course findCourseByName(String name);




}
