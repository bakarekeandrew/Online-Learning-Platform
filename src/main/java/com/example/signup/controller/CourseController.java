package com.example.signup.controller;

import com.example.signup.Course;
import com.example.signup.CourseService;
import com.example.signup.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final PdfService pdfService;

    @Autowired
    public CourseController(CourseService courseService, PdfService pdfService) {
        this.courseService = courseService;
        this.pdfService = pdfService;
    }

    // Endpoint to create a new course
    @PostMapping("/create")
    public String createCourse(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model) throws IOException {
        try {
            String imageUrl = courseService.saveImage(imageFile);
            courseService.createCourse(title, description, imageUrl);

            List<Course> courses = courseService.getAllCourses();
            model.addAttribute("courses", courses);

            model.addAttribute("successMessage", "Course created successfully");
            return "admin_page";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while creating the course.");
            throw new RuntimeException(e);
        }
    }

    // Endpoint to retrieve all courses for the user
    @GetMapping("/personal")
    public String getPersonalCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Course> coursePage;

            if (keyword != null && !keyword.isEmpty()) {
                coursePage = courseService.searchCourses(keyword, pageable);
                model.addAttribute("keyword", keyword);
            } else {
                coursePage = courseService.getAllCoursesPaginated(pageable);
            }

            // Add pagination attributes
            model.addAttribute("courses", coursePage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", coursePage.getTotalPages());
            model.addAttribute("totalItems", coursePage.getTotalElements());

            return "personal_page";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error retrieving courses: " + e.getMessage());
            return "error";
        }
    }
    // Endpoint to search for a course by ID
    @GetMapping("/search")
    public String searchCourse(@RequestParam("id") Long id, Model model) {
        Optional<Course> courseOptional = courseService.getCourseById(id);

        if (courseOptional.isPresent()) {
            model.addAttribute("searchedCourse", courseOptional.get());
        } else {
            model.addAttribute("searchError", "Course not found");
        }

        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);

        return "admin_page";
    }

    // Update course by ID
    @PostMapping("/update/{id}")
    public String updateCourse(@PathVariable Long id,
                               @RequestParam String title,
                               @RequestParam String description,
                               @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        String imageUrl = courseService.saveImage(imageFile);

        Course updatedCourse = new Course();
        updatedCourse.setTitle(title);
        updatedCourse.setDescription(description);
        updatedCourse.setImageUrl(imageUrl);

        courseService.updateCourse(id, updatedCourse);
        return "redirect:/admin_page";
    }

    // Endpoint to display courses for admin
    @GetMapping("/admin")
    public String getAllCoursesForAdmin(Model model) {
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        return "admin_page";
    }

    // Endpoint to get course by ID
    @GetMapping("/{id}")
    public String getCourseById(@PathVariable Long id, Model model) {
        Optional<Course> course = courseService.getCourseById(id);
        course.ifPresent(c -> model.addAttribute("course", c));
        return course.isPresent() ? "course_detail" : "404";
    }

    // Endpoint to delete a course
    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return "redirect:/admin_page";
    }

    // Endpoint to download course details as PDF
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadCoursePdf(@PathVariable Long id) {
        try {
            Course course = courseService.getCourseById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            byte[] pdfContent = pdfService.generateCoursePdf(course);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", course.getTitle() + ".pdf");

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    // Endpoint to display courses in the admin panel
    @GetMapping
    public String getCoursesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Course> coursePage;

            if (keyword != null && !keyword.isEmpty()) {
                coursePage = courseService.searchCourses(keyword, pageable);
                model.addAttribute("keyword", keyword);
            } else {
                coursePage = courseService.getAllCoursesPaginated(pageable);
            }

            // Add pagination attributes
            model.addAttribute("courses", coursePage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", coursePage.getTotalPages());
            model.addAttribute("totalItems", coursePage.getTotalElements());

            // For debugging
            System.out.println("Number of courses: " + coursePage.getContent().size());
            System.out.println("Total elements: " + coursePage.getTotalElements());

            return "courses"; // Make sure this matches your template name
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error retrieving courses: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/course")
    public String getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {

        try {
            Page<Course> coursePage;
            if (keyword != null && !keyword.isEmpty()) {
                coursePage = courseService.searchCourses(keyword, PageRequest.of(page, size));
                model.addAttribute("keyword", keyword);
            } else {
                coursePage = courseService.getAllCoursesPaginated(PageRequest.of(page, size));
            }

            // Add debug logging
            System.out.println("Total courses found: " + coursePage.getTotalElements());
            System.out.println("Courses in current page: " + coursePage.getContent().size());

            if (coursePage.getContent().isEmpty()) {
                System.out.println("No courses found in the current page");
            } else {
                System.out.println("First course title: " + coursePage.getContent().get(0).getTitle());
            }

            model.addAttribute("courses", coursePage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", coursePage.getTotalPages());
            model.addAttribute("totalItems", coursePage.getTotalElements());

            return "personal_page";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error retrieving courses: " + e.getMessage());
            return "error";
        }
    }
//    @GetMapping("/course")
//    public String getAllCourses(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            Model model) {
//        Page coursePage = courseService.getAllCoursesPaginated(PageRequest.of(page, size));
//        model.addAttribute("courses", coursePage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", coursePage.getTotalPages());
//        return "personal_page";
//    }

//    @GetMapping("/course")
//    public String getAllCourses(
//            @RequestParam(required = false) String keyword,
//            Model model) {
//        List courses = keyword != null ?
//                courseService.searchCourses(keyword, Pageable.unpaged()).getContent() :
//                courseService.getAllCourses();
//        model.addAttribute("courses", courses);
//        return "personal_page";
//    }
//    @GetMapping("/course")
//    public String getAllCourses(
//            @RequestParam(defaultValue = "title") String sortBy,
//            Model model) {
//        List courses = courseService.getAllCoursesSorted(Sort.by(sortBy));
//        model.addAttribute("courses", courses);
//        return "personal_page";
//    }
}
