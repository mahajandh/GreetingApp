package com.example.GreetingApp.Service;

import com.example.GreetingApp.Model.AuthUser;
import com.example.GreetingApp.Repository.AuthUserRepository;
import com.example.GreetingApp.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class    AuthenticationService {

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Register User
//    public String registerUser(AuthUser authUser) {
//        if (authUserRepository.existsByEmail(authUser.getEmail())) {
//            return "Email is already in use!";
//        }
//
//        // Save user with encrypted password
//        authUser.setPassword(passwordEncoder.encode(authUser.getPassword()));
//        authUserRepository.save(authUser);
//
//        return "User registered successfully!";
//    }
    public String registerUser(AuthUser authUser) {
        if (authUserRepository.existsByEmail(authUser.getEmail())) {
            return "Email is already in use!";
        }

        // Ensure password is hashed before saving
        authUser.setPassword(passwordEncoder.encode(authUser.getPassword()));
        authUserRepository.save(authUser);
        String subject = "Successful Login Notification";
        String content = "<h2>Hello " + authUser.getFirstName() + ",</h2>"
                + "<p>You have successfully registered on Greeting App.</p>"
                + "<p>If this wasn't you, please reset your password immediately.</p>"
                + "<br><p>Regards,</p><p><strong>GreetingsApp Team</strong></p>";

        emailService.sendEmail(authUser.getEmail(), subject, content);

        return "User registered successfully!";

    }


    // Authenticate User and Generate Token
    public String authenticateUser(String email, String password) {
        Optional<AuthUser> userOpt = authUserRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "User not found!";
        }

        AuthUser user = userOpt.get();

        System.out.println("Stored Hashed Password: " + user.getPassword()); // Debugging
        System.out.println("Raw Password: " + password); // Debugging
        System.out.println("Match Result: " + passwordEncoder.matches(password, user.getPassword())); // Debugging

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return "Invalid email or password!";
        }

        // Generate JWT Token
        String token = jwtUtil.generateToken(email);

        return token;
    }
    // Forgot Password Implementation
    public String forgotPassword(String email, String newPassword) {
        Optional<AuthUser> userOpt = authUserRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return "Sorry! We cannot find the user email: " + email;
        }

        AuthUser user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        authUserRepository.save(user);

        String subject = "Password Change Notification";
        String content = "<h2>Hello " + user.getFirstName() + ",</h2>"
                + "<p>Your password has been changed successfully.</p>"
                + "<br><p>Regards,</p><p><strong>GreetingsApp Team</strong></p>";

        emailService.sendEmail(user.getEmail(), subject, content);

        return "Password has been changed successfully!";
    }

    // Reset Password Implementation
    public String resetPassword(String email, String currentPassword, String newPassword) {
        Optional<AuthUser> userOpt = authUserRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return "User not found with email: " + email;
        }

        AuthUser user = userOpt.get();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return "Current password is incorrect!";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        authUserRepository.save(user);
        String subject = "Password Reset Notification";
        String content = "<h2>Hello " + user.getFirstName() + ",</h2>"
                + "<p>Your password has been reset successfully.</p>"
                + "<br><p>Regards,</p><p><strong>GreetingsApp Team</strong></p>";

        emailService.sendEmail(user.getEmail(), subject, content);

        return "Password reset successfully!";
    }
}
