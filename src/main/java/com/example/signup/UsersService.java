package com.example.signup;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UsersService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersService(UsersRepository userRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsersModel authenticate(String login, String password) {
        // Step 1: Fetch user by login, which returns an Optional
        Optional<UsersModel> optionalUser = usersRepository.findByLogin(login);

        // Step 2: Check if user exists, then validate the password
        if (optionalUser.isPresent()) {
            UsersModel user = optionalUser.get(); // Get the actual UsersModel object from the Optional
            // Step 3: If the password matches, return the authenticated user
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }

        // Return null if authentication fails (either user not found or password mismatch)
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user from the database using login
        Optional<UsersModel> optionalUser = usersRepository.findByLogin(username);

        // Handle case when the user is not found
        UsersModel user = optionalUser.orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Prepend "ROLE_" to the user's role to match Spring Security's expectations
        String roleWithPrefix = "ROLE_" + user.getRole();  // e.g., "ADMIN" becomes "ROLE_ADMIN"

        // Return a UserDetails object with the user's login, password, and authorities (roles)
        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(roleWithPrefix))
        );
    }



    public UsersModel registerUser(String login, String password, String email, String role) {
        if (usersRepository.findByLogin(login).isPresent()) {
            return null; // User already exists
        }

        UsersModel user = new UsersModel();
        user.setLogin(login);
        user.setPassword(getPasswordEncoder().encode(password));
        user.setEmail(email);
        user.setRole(role);

        return usersRepository.save(user);
    }

    @Autowired
    private ObjectProvider<PasswordEncoder> passwordEncoderProvider;

    private PasswordEncoder getPasswordEncoder() {
        return passwordEncoderProvider.getIfAvailable(() -> new BCryptPasswordEncoder());
    }


    public List<UsersModel> getAllUsers() {
        return usersRepository.findAll();
    }

    public String getLoggedInUserLogin() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

//    public List<UsersModel> getAllUsers() {
//        return usersRepository.findAll();
//    }

    // Create new user
    public void createUser(UsersModel user) {
        // Check if user already exists
        if (usersRepository.findByLogin(user.getLogin()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default role if not specified
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        } else {
            // Ensure role has ROLE_ prefix
            if (!user.getRole().startsWith("ROLE_")) {
                user.setRole("ROLE_" + user.getRole());
            }
        }

        usersRepository.save(user);
    }

    // Find user by login
    public UsersModel findByLogin(String login) {
        return usersRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Delete user
    public void deleteUser(String login) {
        UsersModel user = findByLogin(login);
        usersRepository.delete(user);
    }

    // Update user
    public void updateUser(String originalLogin, UsersModel updatedUser) {
        UsersModel existingUser = findByLogin(originalLogin);

        // Update fields
        existingUser.setLogin(updatedUser.getLogin());
        existingUser.setEmail(updatedUser.getEmail());

        // Only update password if a new one is provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        // Update role if provided
        if (updatedUser.getRole() != null && !updatedUser.getRole().isEmpty()) {
            String role = updatedUser.getRole();
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }
            existingUser.setRole(role);
        }

        usersRepository.save(existingUser);
    }

    // Check if user exists
    public boolean userExists(String login) {
        return usersRepository.findByLogin(login).isPresent();
    }

}


