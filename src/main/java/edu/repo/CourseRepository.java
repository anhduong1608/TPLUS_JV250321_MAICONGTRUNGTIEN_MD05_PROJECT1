package edu.repo;


import edu.model.DTO.CourseDTO5TOP;
import edu.model.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT new edu.model.DTO.CourseDTO5TOP(c.id, c.name, c.image, COUNT(r.id)) " +
            "FROM Course c JOIN Registration r ON r.course.id = c.id " +
            "WHERE r.status = 'CONFIRM' " +
            "GROUP BY c.id, c.name, c.image " +
            "ORDER BY COUNT(r.id) DESC")
    List<CourseDTO5TOP> findTopCourses(Pageable pageable);


    Course findCourseByNameContaining(String name);

    @Query("SELECT r.course FROM Registration r " +
            "JOIN r.course c" +
            " JOIN r.student s" +
            " WHERE s.id = :studentId AND r.status = 'WAITING'")
    List<Course> findWaitingCourses(@Param("studentId") Long studentId);

}
