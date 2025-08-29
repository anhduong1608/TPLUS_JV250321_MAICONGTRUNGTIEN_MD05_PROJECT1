package edu.service;

import edu.model.entity.Course;
import edu.model.entity.Registration;
import edu.model.entity.RegistrationStatus;
import edu.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegistrationService {
    Page<Registration> getRegistrations(RegistrationStatus status, String keyword, Pageable pageable);
    void approveRegistration(Long id);
    void denyRegistration(Long id);
    void cancelRegistration(Long id);
    Registration registerCourse(User student, Course course);
    boolean canRegisterCourse(Long studentId,Long courseId);
    Page<Registration> findHistoryRegistrations(Long studentId, String keyword, Pageable pageable);
    public void cancelRegistration(Long registrationId, Long studentId);
}
