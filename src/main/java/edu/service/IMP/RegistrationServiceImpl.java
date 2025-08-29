package edu.service.IMP;

import edu.model.entity.Course;
import edu.model.entity.Registration;
import edu.model.entity.RegistrationStatus;
import edu.model.entity.User;
import edu.repo.CourseRepository;
import edu.repo.RegistrationRepository;
import edu.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    RegistrationRepository registrationRepository;
    @Autowired
    CourseRepository courseRepository;

    @Override
    public Page<Registration> getRegistrations(RegistrationStatus status, String keyword, Pageable pageable) {
        return registrationRepository.searchRegistrations(status, keyword, pageable);
    }

    @Override
    public void approveRegistration(Long id) {
        Registration registration = registrationRepository.findById(id).orElseThrow(null);
        registration.setStatus(RegistrationStatus.CONFIRM);
        registrationRepository.save(registration);
    }

    @Override
    public void denyRegistration(Long id) {
        Registration registration = registrationRepository.findById(id).orElseThrow(null);
        registration.setStatus(RegistrationStatus.DENIED);
        registrationRepository.save(registration);
    }

    @Override
    public void cancelRegistration(Long id) {
        Registration registration = registrationRepository.findById(id).orElseThrow(null);
        registration.setStatus(RegistrationStatus.CANCEL);
        registrationRepository.save(registration);
    }

    @Override
    public Registration registerCourse(User student, Course course) {
        Registration registration = Registration.builder()
                .student(student)
                .course(course)
                .registeredAt(LocalDateTime.now())
                .status(RegistrationStatus.WAITING)
                .build();
        return registrationRepository.save(registration);
    }

    @Override
    public boolean canRegisterCourse(Long studentId, Long courseId) {
        List<Course> courseList = courseRepository.findWaitingCourses(studentId);
        return courseList.stream().noneMatch(course -> course.getId().equals(courseId));
    }

    @Override
    public Page<Registration> findHistoryRegistrations(Long studentId, String keyword, Pageable pageable) {
        return registrationRepository.findHistoryRegistrations(studentId, keyword, pageable);
    }

    @Override
    public void cancelRegistration(Long registrationId, Long studentId) {
        Registration r = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đăng ký"));


        if (r.getStudent().getId().equals(studentId)
                && (r.getStatus() == RegistrationStatus.WAITING
                || r.getStatus() == RegistrationStatus.CONFIRM)) {
            r.setStatus(RegistrationStatus.CANCEL);
            registrationRepository.save(r);
        } else {
            throw new RuntimeException("Không thể hủy đăng ký này!");
        }

    }


}
