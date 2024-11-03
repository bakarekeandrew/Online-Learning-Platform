package com.example.signup.controller;

import com.example.signup.Course;
import com.example.signup.CourseService;
import com.example.signup.UsersModel;
import com.example.signup.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    private final CourseService courseService;
    private final UsersService usersService;

    @Autowired
    public DashboardController(CourseService courseService, UsersService usersService) {
        this.courseService = courseService;
        this.usersService = usersService;
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        // Get all courses and users
        List<Course> courses = courseService.getAllCourses();
        List<UsersModel> users = usersService.getAllUsers();

        // Calculate basic statistics
        int totalCourses = courses.size();
        int totalUsers = users.size();

        // Get current month statistics
        YearMonth currentMonth = YearMonth.now();
        int newCourses = getNewCoursesCount(courses, currentMonth);
        int newUsers = getNewUsersCount(users, currentMonth);

        // Calculate active students (users with ROLE_USER)
        long activeStudents = users.stream()
                .filter(user -> "ROLE_USER".equals(user.getRole()))
                .count();

        // Add basic statistics to model
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeStudents", activeStudents);
        model.addAttribute("completions", calculateCompletions(courses));
        model.addAttribute("newCourses", newCourses);
        model.addAttribute("newUsers", newUsers);
        model.addAttribute("newActiveStudents", calculateNewActiveStudents());
        model.addAttribute("newCompletions", calculateNewCompletions());

        // Add chart data
        model.addAttribute("enrollmentData", getEnrollmentData());
        model.addAttribute("registrationData", getRegistrationData());

        return "admin_page";

    }@GetMapping("/users")
    public String getUsers(Model model) {
        // Fetch users from the service
        List<UsersModel> users = usersService.getAllUsers();
        model.addAttribute("users", users);
        return "users";
    }
    @GetMapping("/courses")
    public String getCourses(Model model) {
        // Fetch courses from the service
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        return "courses";
    }




    private List<ChartData> getEnrollmentData() {
        // Generate last 6 months of enrollment data
        List<ChartData> data = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.from(now.minusMonths(i));
            // In a real application, you would query this from your database
            // This is sample data for demonstration
            data.add(new ChartData(
                    month.getMonth().toString(),
                    (int) (Math.random() * 100 + 50) // Random number between 50 and 150
            ));
        }

        return data;
    }

    private List<ChartData> getRegistrationData() {
        // Generate last 6 months of user registration data
        List<ChartData> data = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.from(now.minusMonths(i));
            data.add(new ChartData(
                    month.getMonth().toString(),
                    (int) (Math.random() * 50 + 20) // Random number between 20 and 70
            ));
        }

        return data;
    }

    private int getNewCoursesCount(List<Course> courses, YearMonth month) {
        // In a real application, you would filter courses by creation date
        // This is a placeholder implementation
        return (int) (courses.size() * 0.2); // Assume 20% are new
    }

    private int getNewUsersCount(List<UsersModel> users, YearMonth month) {
        // In a real application, you would filter users by registration date
        // This is a placeholder implementation
        return (int) (users.size() * 0.15); // Assume 15% are new
    }

    private int calculateCompletions(List<Course> courses) {
        // In a real application, you would count actual course completions
        // This is a placeholder implementation
        return (int) (courses.size() * 0.3); // Assume 30% completion rate
    }

    private int calculateNewActiveStudents() {
        // Placeholder implementation
        return 15; // Fixed number for demonstration
    }

    private int calculateNewCompletions() {
        // Placeholder implementation
        return 25; // Fixed number for demonstration
    }
}

// Data class for chart data
class ChartData {
    private String month;
    private int count;

    public ChartData(String month, int count) {
        this.month = month;
        this.count = count;
    }

    // Getters and setters
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}