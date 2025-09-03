package edu.controller;

import edu.model.DTO.CourseDTO;
import edu.model.DTO.CourseDTO5TOP;
import edu.model.entity.Course;
import edu.model.entity.User;
import edu.service.CloudinaryService;
import edu.service.CourseService;
import edu.service.StatisticService;
import edu.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private CourseService courseService;
    @Autowired
    CloudinaryService cloudinaryService;
    @Autowired
    StatisticService statisticService;


    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole().name());
    }


    @GetMapping("/home")
    public String listCoursesForAdmin(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "7") int size,
                                      @RequestParam(defaultValue = "5") int limit,
                                      @RequestParam(required = false) String keyword,
                                      Model model,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Course> coursePage = (keyword != null && !keyword.isEmpty())
                ? courseService.getAllCoursesForUser(keyword, pageable)
                : courseService.getAllCoursesForAdmin(pageable);

        Map<Long, Long> studentCount = new HashMap<>();
        coursePage.forEach(c -> studentCount.put(c.getId(), courseService.countStudentsByCourseId(c.getId())));

        List<CourseDTO5TOP> topCourses = courseService.getTopCourses(PageRequest.of(0, limit));

        long totalCourses = statisticService.getTotalCourses();
        long totalStudents = statisticService.getTotalStudents();
        long totalRegistrations = statisticService.getTotalRegistrations();

        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalRegistrations", totalRegistrations);

        model.addAttribute("courses", topCourses);
        model.addAttribute("coursePage", coursePage);
        model.addAttribute("studentCount", studentCount);
        model.addAttribute("keyword", keyword);
        return "admin/course_list";
    }

    @GetMapping("/course/add")
    public String showAddForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/login";
        }
        model.addAttribute("courseDTO", new CourseDTO());
        return "admin/add_course";
    }

    @PostMapping("/course/add")
    public String addCourse(@Valid @ModelAttribute("courseDTO") CourseDTO dto,
                            BindingResult result,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) throws IOException {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/";
        }
        if (result.hasErrors()) {
            return "admin/add_course";
        }
        String imageUrl = cloudinaryService.uploadFile(dto.getImage());

        Course course = Course.builder()
                .name(dto.getName())
                .duration(dto.getDuration())
                .instructor(dto.getInstructor())
                .image(imageUrl)
                .createAt(LocalDate.now())
                .build();

        courseService.save(course);
        return "redirect:/admin/home";
    }

    @GetMapping("/course/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/login";
        }
        Course course = courseService.findById(id);
        if (course == null) {
            return "redirect:/admin/course_management";
        }
        CourseDTO courseDTO = CourseDTO.builder()
                .name(course.getName())
                .duration(course.getDuration())
                .instructor(course.getInstructor())
                .build();

        model.addAttribute("courseDTO", courseDTO);
        model.addAttribute("courseId", id);
        model.addAttribute("courseImage", course.getImage());
        return "admin/edit_course";
    }

    @PostMapping("/course/{id}/edit")
    public String updateCourse(@PathVariable Long id,
                               @Valid @ModelAttribute("courseDTO") CourseDTO courseDTO,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) throws IOException {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("courseId", id);
            model.addAttribute("courseImage", courseService.findById(id).getImage());
            return "admin/edit_course";
        }

        Course course = courseService.findById(id);
        if (course == null) {
            bindingResult.reject("notFound", "Không tìm thấy khóa học cần chỉnh sửa.");
            return "admin/edit_course";
        }

        Course existingCourse = courseService.findCourseByName(courseDTO.getName());
        if (existingCourse != null && !existingCourse.getId().equals(id)) {
            bindingResult.rejectValue("name", "duplicate", "Tên khóa học đã tồn tại. Vui lòng chọn tên khác.");
            model.addAttribute("courseId", id);
            model.addAttribute("courseImage", course.getImage());
            return "admin/edit_course";
        }

        course.setName(courseDTO.getName());
        course.setDuration(courseDTO.getDuration());
        course.setInstructor(courseDTO.getInstructor());

        if (courseDTO.getImage() != null && !courseDTO.getImage().isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(courseDTO.getImage());
            course.setImage(imageUrl);
        }

        courseService.save(course);
        redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
        return "redirect:/admin/course_management";
    }

    @GetMapping("/course/{id}/delete")
    public String deleteCourse(@PathVariable Long id,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/";
        }
        boolean deleted = courseService.deleteCourse(id);
        if (!deleted) {
            redirectAttributes.addFlashAttribute("error", "Xóa không thành công! Khóa học đang có sinh viên đăng ký.");
        } else {
            redirectAttributes.addFlashAttribute("success", "Xóa khóa học thành công!");
        }
        return "redirect:/admin/course_management";
    }

    @GetMapping("/course_management")
    public String courseManagement(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "5") int size,
                                   @RequestParam(required = false) String keyword,
                                   Model model,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Course> coursePage = (keyword != null && !keyword.isEmpty())
                ? courseService.getAllCoursesForUser(keyword, pageable)
                : courseService.getAllCoursesForAdmin(pageable);

        Map<Long, Long> studentCount = new HashMap<>();
        coursePage.forEach(c -> studentCount.put(c.getId(), courseService.countStudentsByCourseId(c.getId())));

        model.addAttribute("coursePage", coursePage);
        model.addAttribute("studentCount", studentCount);
        model.addAttribute("keyword", keyword);
        return "admin/course_management";
    }


    @GetMapping("/users")
    public String listUsers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "5") int size,
                            @RequestParam(defaultValue = "id") String sortBy,
                            @RequestParam(defaultValue = "asc") String sortDir,
                            @RequestParam(required = false) String keyword,
                            Model model,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/";
        }

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userService.getAllUsers(keyword, pageable);

        model.addAttribute("userPage", userPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);

        return "admin/user_list";
    }

    @PostMapping("/users/{id}/change-status")
    public String toggleUserStatus(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/";
        }
        userService.changeStatus(id);
        redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/add")
    public String showAddUserForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/login";
        }
        model.addAttribute("user", new User());
        return "admin/user_form";
    }

    @PostMapping("/users/add")
    public String addUser(@Valid @ModelAttribute("user") User user,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/";
        }
        if (userService.existsEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email đã tồn tại");
        }
        if (userService.existsPhone(user.getPhone())) {
            result.rejectValue("phone", "error.user", "Số điện thoại đã tồn tại");
        }
        if (result.hasErrors()) {
            return "admin/user_form";
        }

        userService.saveUser(user);
        redirectAttributes.addFlashAttribute("success", "Thêm học viên thành công!");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/";
        }

        return userService.getUserById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    return "admin/user_form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Không tìm thấy học viên!");
                    return "redirect:/admin/users";
                });
    }

    @PostMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id,
                           @Valid @ModelAttribute("user") User user,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           HttpSession session) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/";
        }

        User existingUser = userService.getUserById(id).orElse(null);
        if (existingUser == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy học viên!");
            return "redirect:/admin/users";
        }

        if (!user.getEmail().equals(existingUser.getEmail()) &&
                userService.existsEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email đã tồn tại");
        }

        if (!user.getPhone().equals(existingUser.getPhone()) &&
                userService.existsPhone(user.getPhone())) {
            result.rejectValue("phone", "error.user", "Số điện thoại đã tồn tại");
        }

        if (result.hasErrors()) {
            return "admin/user_form";
        }

        user.setId(id);
        userService.saveUser(user);
        redirectAttributes.addFlashAttribute("success", "Cập nhật học viên thành công!");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập!");
            return "redirect:/";
        }
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Xóa học viên thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "redirect:/";
    }

}
