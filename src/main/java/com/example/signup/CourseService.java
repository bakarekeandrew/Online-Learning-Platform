package com.example.signup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CourseService {

    private static final String UPLOAD_DIR = "uploads/";
    private static final String IMAGE_URL_PREFIX = "/uploads/";

    private final CourseRepository courseRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List getAllCourses() {
        return courseRepository.findAll();
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    public String saveImage(MultipartFile imageFile) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = imageFile.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return IMAGE_URL_PREFIX + uniqueFilename;
    }

    public Course createCourse(String title, String description, String imageUrl) {
        Course course = new Course(title, description, imageUrl);
        return courseRepository.save(course);
    }

    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    public Course updateCourse(Long id, Course updatedCourse) {
        return courseRepository.findById(id)
                .map(course -> {
                    course.setTitle(updatedCourse.getTitle());
                    course.setDescription(updatedCourse.getDescription());
                    course.setImageUrl(updatedCourse.getImageUrl());
                    return courseRepository.save(course);
                }).orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    public Page<Course> getAllCoursesPaginated(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    public Page<Course> searchCourses(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return courseRepository.searchCourses(keyword, pageable);
        }
        return courseRepository.findAll(pageable);
    }
}
