package edu.service.IMP;

import edu.model.DTO.LoginDTO;
import edu.model.DTO.RegisterUserDTO;
import edu.model.entity.Role;
import edu.model.entity.User;
import edu.repo.UserRepository;
import edu.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User register(RegisterUserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!!");
        }
        if (userRepository.existsByPhone(dto.getPhone())) {
            throw new RuntimeException("Phone Đã tồn tại!!");
        }

        User user = User.builder()
                .name(dto.getName())
                .dob(dto.getDob())
                .email(dto.getEmail())
                .sex(dto.getSex())
                .phone(dto.getPhone())
                .password(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt(10)))
                .createAt(LocalDate.now())
                .role(Role.STUDENT)
                .status(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    public User login(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng");
        }

        return user;
    }

    @Override
    public User findUserByPhone(String phone) {
        return userRepository.findUserByPhone(phone);
    }

    @Override
    public Page<User> getAllUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        Long idSearch = null;
        try {
            idSearch = Long.parseLong(keyword);
        } catch (NumberFormatException ignored) {
        }

        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrId(
                keyword, keyword, idSearch, pageable
        );
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findUserById(id);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void changeStatus(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus(!user.getStatus());
            userRepository.save(user);
        });
    }


    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không thể xóa tài khoản ADMIN!");
        }

        if (Boolean.TRUE.equals(user.getStatus())) {
            throw new RuntimeException("Bạn cần khóa tài khoản trước khi xóa!");
        }

        userRepository.delete(user);
    }


    @Override
    public boolean existsEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public boolean existsEmailOther(String email, Long id) {
        return userRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public boolean existsPhoneOther(String phone, Long id) {
        return userRepository.existsByPhoneAndIdNot(phone, id);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }
}
