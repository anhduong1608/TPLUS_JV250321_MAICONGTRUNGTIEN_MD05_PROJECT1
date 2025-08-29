package edu.controller;

import edu.model.entity.Registration;
import edu.model.entity.RegistrationStatus;
import edu.model.entity.User;
import edu.service.RegistrationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole().name());
    }

    @GetMapping
    public String listRegistrations(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "5") int size,
                                    @RequestParam(required = false) RegistrationStatus status,
                                    @RequestParam(required = false) String keyword,
                                    HttpSession session,
                                    Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Registration> registrations = registrationService.getRegistrations(status, keyword, pageable);

        model.addAttribute("registrations", registrations);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        return "registration/registration_list";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        registrationService.approveRegistration(id);
        return "redirect:/admin/registrations";
    }

    @PostMapping("/{id}/deny")
    public String deny(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        registrationService.denyRegistration(id);
        return "redirect:/admin/registrations";
    }
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        registrationService.cancelRegistration(id);
        return "redirect:/admin/registrations";
    }
}
