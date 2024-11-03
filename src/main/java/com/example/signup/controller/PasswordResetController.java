//package com.example.signup.controller;
//
//import com.example.signup.ForgotPasswordRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@Controller
//@RequestMapping("/password")
//public class PasswordResetController {
//    private final UserService userService;
//    private final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);
//
//    public PasswordResetController(UserService userService) {
//        this.userService = userService;
//    }
//
//    @GetMapping("/forgot")
//    public String showForgotPasswordForm() {
//        return "forgot_password";
//    }
//
//    @PostMapping("/forgot")
//    public String processForgotPassword(@RequestParam("email") String email, Model model) {
//        User user = userService.findByEmail(email);
//        if (user == null) {
//            model.addAttribute("error", "Email not found");
//            return "forgot_password";
//        }
//
//        try {
//            userService.createPasswordResetTokenForUser(user);
//            model.addAttribute("message", "Password reset link has been sent to your email");
//        } catch (Exception e) {
//            logger.error("Error during password reset", e);
//            model.addAttribute("error", "Error sending reset email");
//        }
//
//        return "forgot_password";
//    }
//
//    @GetMapping("/reset")
//    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
//        String result = userService.validatePasswordResetToken(token);
//
//        if (!"valid".equals(result)) {
//            model.addAttribute("error", "Invalid or expired token");
//            return "error";
//        }
//
//        model.addAttribute("token", token);
//        return "reset_password";
//    }
//
//    @PostMapping("/reset")
//    public String processResetPassword(@RequestParam("token") String token,
//                                       @RequestParam("password") String password,
//                                       Model model) {
//        String result = userService.validatePasswordResetToken(token);
//        if (!"valid".equals(result)) {
//            model.addAttribute("error", "Invalid or expired token");
//            return "error";
//        }
//
//        Optional<User> user = userService.getUserByPasswordResetToken(token);
//        if (user.isPresent()) {
//            userService.changeUserPassword(user.get(), password);
//            model.addAttribute("message", "Password has been reset successfully");
//            return "login";
//        }
//
//        model.addAttribute("error", "Error resetting password");
//        return "error";
//    }
//}
