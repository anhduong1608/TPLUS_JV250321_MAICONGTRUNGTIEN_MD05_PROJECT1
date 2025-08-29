//package edu.controller;
//
//import edu.model.entity.User;
//import jakarta.servlet.http.HttpSession;
//
//public abstract class BaseController {
//
//    public boolean isAdmin(HttpSession session) {
//        User user = (User) session.getAttribute("user");
//        return user != null && "ADMIN".equalsIgnoreCase(user.getRole().name());
//    }
//
//    public boolean isStudent(HttpSession session) {
//        User user = (User) session.getAttribute("user");
//        return user != null && "STUDENT".equalsIgnoreCase(user.getRole().name());
//    }
//
//    public User getCurrentUser(HttpSession session) {
//        return (User) session.getAttribute("user");
//    }
//}
