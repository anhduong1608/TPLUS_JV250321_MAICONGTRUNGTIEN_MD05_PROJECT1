package edu.controller;

import edu.model.DTO.ChangePasswordDTO;
import edu.model.DTO.UserDTO;
import edu.model.entity.Course;
import edu.model.entity.Registration;
import edu.model.entity.User;
import edu.service.CourseService;
import edu.service.RegistrationService;
import edu.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/home")
    public String home(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "6") int size,
                       @RequestParam(required = false) String keyword,
                       Model model) {

        Page<Course> coursePage = courseService.getAllCoursesForUser(keyword, PageRequest.of(page, size));

        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "user/user_home";
    }

    @PostMapping("/register/{id}")
    public String registerCourse(@PathVariable("id") Long courseId,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }

        Course course = courseService.findById(courseId);
        if (!registrationService.canRegisterCourse(user.getId(), courseId)) {
            redirectAttributes.addFlashAttribute("error", "Khóa học đã được đăng trước rồi đăng ký không thành công!!");
            return "redirect:/user/home";

        }
        registrationService.registerCourse(user, course);
        redirectAttributes.addFlashAttribute("success", "Đăng kí khóa học thành công!!");
        return "redirect:/user/home";
    }

    @GetMapping("/history")
    public String viewHistory(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "6") int size,
                              @RequestParam(required = false) String keyword,
                              HttpSession session,
                              Model model) {

        User student = (User) session.getAttribute("user");
        Pageable pageable = PageRequest.of(page, size);

        Page<Registration> history = registrationService.findHistoryRegistrations(student.getId(), keyword, pageable);

        model.addAttribute("history", history);
        model.addAttribute("keyword", keyword);

        return "user/history";
    }

    @PostMapping("/cancel/{id}")
    public String cancelRegistration(@PathVariable Long id,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User student = (User) session.getAttribute("user");
        try {
            registrationService.cancelRegistration(id, student.getId());
            redirectAttributes.addFlashAttribute("success", "Hủy đăng ký thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/history";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            return "redirect:/";
        }

        UserDTO dto = UserDTO.builder()
                .name(currentUser.getName())
                .dob(currentUser.getDob())
                .email(currentUser.getEmail())
                .sex(currentUser.getSex())
                .phone(currentUser.getPhone())
                .build();

        model.addAttribute("user", dto);
        return "user/profile";
    }


    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("user") UserDTO dto,
                                BindingResult result,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/";
        }

        if (result.hasErrors()) {
            return "user/profile";
        }

        User existingEmail = userService.findUserByEmail(dto.getEmail());
        if (existingEmail != null && !existingEmail.getId().equals(currentUser.getId())) {
            result.rejectValue("email", "error.user", "Email đã tồn tại");
            return "user/profile";
        }

        User existingPhone = userService.findUserByPhone(dto.getPhone());
        if (existingPhone != null && !existingPhone.getId().equals(currentUser.getId())) {
            result.rejectValue("phone", "error.user", "Số điện thoại đã tồn tại");
            return "user/profile";
        }

        currentUser.setName(dto.getName());
        currentUser.setDob(dto.getDob());
        currentUser.setEmail(dto.getEmail());
        currentUser.setSex(dto.getSex());
        currentUser.setPhone(dto.getPhone());

        userService.saveUser(currentUser);

        session.setAttribute("user", currentUser);

        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        return "redirect:/user/profile";
    }
    @GetMapping("/change-password")
    public String showChangePasswordForm(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            return "redirect:/";
        }
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
        return "user/change_password";
    }


    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("changePasswordDTO") ChangePasswordDTO dto,
                                 BindingResult result,
                                 HttpSession session,
                                 Model model) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            return "redirect:/";
        }


        if (result.hasErrors()) {
            return "user/change_password";
        }


        if (!BCrypt.checkpw(dto.getOldPassword(), loggedInUser.getPassword())) {
            model.addAttribute("error", "Mật khẩu cũ không chính xác");
            return "user/change_password";
        }


        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("error", "Xác nhận mật khẩu mới không khớp");
            return "user/change_password";
        }

        if (!BCrypt.checkpw(dto.getNewPassword(), loggedInUser.getPassword())) {
            model.addAttribute("error", "Mật khẩu mới không được trùng với mật khẩu cũ");
            return "user/change_password";
        }


        loggedInUser.setPassword(BCrypt.hashpw(dto.getNewPassword(),BCrypt.gensalt(10)));
        userService.saveUser(loggedInUser);

        model.addAttribute("success", "Đổi mật khẩu thành công");
        return "user/change_password";
    }
}


