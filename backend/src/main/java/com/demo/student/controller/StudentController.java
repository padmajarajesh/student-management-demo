package com.demo.student.controller;

import com.demo.student.dto.ApiResponse;
import com.demo.student.dto.StudentRequest;
import com.demo.student.dto.StudentResponse;
import com.demo.student.security.AuthenticatedUser;
import com.demo.student.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> create(@AuthenticationPrincipal AuthenticatedUser user,
                                                               @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Student created successfully", studentService.create(user.id(), request)));
    }

    @GetMapping
    public ApiResponse<List<StudentResponse>> findAll(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok("Students retrieved", studentService.findAll(user.id()));
    }

    // Declared before /{id} for readability; Spring matches the literal path first anyway.
    @GetMapping("/search")
    public ApiResponse<List<StudentResponse>> search(@AuthenticationPrincipal AuthenticatedUser user,
                                                     @RequestParam(defaultValue = "") String keyword) {
        return ApiResponse.ok("Search results", studentService.search(user.id(), keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<StudentResponse> findById(@AuthenticationPrincipal AuthenticatedUser user,
                                                 @PathVariable UUID id) {
        return ApiResponse.ok("Student retrieved", studentService.findById(user.id(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<StudentResponse> update(@AuthenticationPrincipal AuthenticatedUser user,
                                               @PathVariable UUID id,
                                               @Valid @RequestBody StudentRequest request) {
        return ApiResponse.ok("Student updated successfully", studentService.update(user.id(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        studentService.delete(user.id(), id);
        return ApiResponse.ok("Student deleted successfully");
    }
}
