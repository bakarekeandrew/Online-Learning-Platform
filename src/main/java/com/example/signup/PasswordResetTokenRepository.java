//package com.example.signup;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.security.core.userdetails.User;
//
//public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
//    PasswordResetToken findByToken(String token);
//    void deleteByUser(User user);
//}