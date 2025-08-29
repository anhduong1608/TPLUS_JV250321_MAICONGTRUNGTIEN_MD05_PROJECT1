package edu.repo;


import edu.model.entity.Course;
import edu.model.entity.Registration;
import edu.model.entity.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    long countByCourse(Course course);

    boolean existsByCourseIdAndStatusIn(Long courseId, List<RegistrationStatus> statuses);

    @Query("SELECT r FROM Registration r " +
            "WHERE (:status IS NULL OR r.status = :status) " +
            "AND (:keyword IS NULL OR LOWER(r.course.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Registration> searchRegistrations(@Param("status") RegistrationStatus status,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);

    @Query("SELECT COUNT(r) FROM Registration r")
    long countRegistrations();

    @Query("SELECT r FROM Registration r" +
            " JOIN r.course c" +
            " JOIN r.student" +
            " WHERE r.student.id = :studentId" +
            " AND (:keyword IS NULL OR LOWER(c.name) like LOWER(CONCAT('%',:keyword,'%'))) " +
            " ORDER BY r.status asc , r.registeredAt desc ")
    Page<Registration> findHistoryRegistrations(@Param("studentId") Long studentId, @Param("keyword") String keyword, Pageable pageable);
}
