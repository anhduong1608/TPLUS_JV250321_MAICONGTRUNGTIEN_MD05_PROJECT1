package edu.service;

import edu.model.DTO.LoginDTO;
import edu.model.DTO.RegisterUserDTO;
import edu.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {
    User register(RegisterUserDTO dto);
    User login(LoginDTO dto);
    User findUserByPhone(String phone);
    Page<User> getAllUsers(String keyword, Pageable pageable);
    User findUserById(Long id);
    Optional<User> getUserById(Long id);

    void saveUser(User user);

    void changeStatus(Long id);

    void deleteUser(Long id);
    boolean existsEmail(String email);
    boolean existsPhone(String phone);
    boolean existsEmailOther(String email, Long id);
    boolean existsPhoneOther(String phone, Long id);
    User findUserByEmail(String email);
}
