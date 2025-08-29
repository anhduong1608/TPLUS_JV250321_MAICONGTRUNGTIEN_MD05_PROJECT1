package edu.controller;

import edu.model.DTO.LoginDTO;
import edu.model.DTO.RegisterUserDTO;
import edu.model.entity.Role;
import edu.model.entity.User;
import edu.repo.UserRepository;
import edu.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping({"/","/auth"})
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;


    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("dto", new RegisterUserDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("dto") RegisterUserDTO dto,
                           BindingResult result,
                           Model model) {

        if (result.hasErrors()) {
            return "auth/register";
        }


        if (userRepository.existsByEmail(dto.getEmail())) {
            result.rejectValue("email", "error.dto", "Email đã tồn tại");
        }


        if (userRepository.existsByPhone(dto.getPhone())) {
            result.rejectValue("phone", "error.dto", "Số điện thoại đã tồn tại");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        userService.register(dto);
        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("dto", new LoginDTO());
        return "auth/login";
    }

    @PostMapping
    public String login(@Valid @ModelAttribute("dto") LoginDTO dto,
                        BindingResult result,
                        HttpSession session,
                        Model model) {
        if (result.hasErrors()) {
            return "auth/login";
        }

        try {
            User user = userService.login(dto);


            if (Boolean.FALSE.equals(user.getStatus())) {
                model.addAttribute("loginError", "Tài khoản đã bị khóa");
                return "auth/login";
            }

            session.setAttribute("user", user);

            if (user.getRole() == Role.ADMIN) {
                return "redirect:/admin/home";
            } else if (user.getRole() == Role.STUDENT) {
                return "redirect:/user/home";
            } else {
                model.addAttribute("loginError", "Vai trò không hợp lệ");
                return "auth/login";
            }

        } catch (RuntimeException e) {
            model.addAttribute("loginError", e.getMessage());
            return "auth/login";
        }
    }


}


