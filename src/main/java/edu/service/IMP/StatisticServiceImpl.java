package edu.service.IMP;

import edu.repo.CourseRepository;
import edu.repo.RegistrationRepository;
import edu.repo.UserRepository;
import edu.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    @Autowired
    CourseRepository courseRepository;
    @Autowired
    RegistrationRepository registrationRepository;
    @Autowired
    UserRepository userRepository;

    @Override
    public long getTotalCourses() {
        return courseRepository.count();
    }

    @Override
    public long getTotalStudents() {
        return userRepository.countStudents();
    }

    @Override
    public long getTotalRegistrations() {
        return registrationRepository.countRegistrations();
    }
}
