package com.demo.student.service;

import com.demo.student.dto.StudentRequest;
import com.demo.student.dto.StudentResponse;
import com.demo.student.entity.Student;
import com.demo.student.exception.DuplicateResourceException;
import com.demo.student.exception.ResourceNotFoundException;
import com.demo.student.repository.StudentRepository;
import com.demo.student.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Student CRUD. Every method takes the current user's id and only touches
 * students owned by that user. A student owned by someone else is reported
 * as "not found" (404) so other users' record ids are never confirmed.
 */
@Service
public class StudentService {

    private static final String DUPLICATE_EMAIL = "You already have a student with this email address";

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public StudentService(StudentRepository studentRepository, UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StudentResponse create(UUID ownerId, StudentRequest request) {
        String email = normalizeEmail(request.email());
        if (studentRepository.existsByOwnerIdAndEmail(ownerId, email)) {
            throw new DuplicateResourceException("email", DUPLICATE_EMAIL);
        }
        Student student = new Student();
        student.setOwner(userRepository.getReferenceById(ownerId));
        apply(student, request, email);
        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll(UUID ownerId) {
        return studentRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(StudentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(UUID ownerId, UUID studentId) {
        return StudentResponse.from(getOwnedStudent(ownerId, studentId));
    }

    @Transactional
    public StudentResponse update(UUID ownerId, UUID studentId, StudentRequest request) {
        Student student = getOwnedStudent(ownerId, studentId);
        String email = normalizeEmail(request.email());
        if (studentRepository.existsByOwnerIdAndEmailAndIdNot(ownerId, email, studentId)) {
            throw new DuplicateResourceException("email", DUPLICATE_EMAIL);
        }
        apply(student, request, email);
        return StudentResponse.from(studentRepository.saveAndFlush(student));
    }

    @Transactional
    public void delete(UUID ownerId, UUID studentId) {
        studentRepository.delete(getOwnedStudent(ownerId, studentId));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> search(UUID ownerId, String keyword) {
        String trimmed = keyword == null ? "" : keyword.trim();
        if (trimmed.isEmpty()) {
            return findAll(ownerId);
        }
        return studentRepository.search(ownerId, trimmed).stream()
                .map(StudentResponse::from)
                .toList();
    }

    private Student getOwnedStudent(UUID ownerId, UUID studentId) {
        return studentRepository.findByIdAndOwnerId(studentId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private static void apply(Student student, StudentRequest request, String normalizedEmail) {
        student.setFullName(request.fullName().trim());
        student.setEmail(normalizedEmail);
        student.setPhoneNumber(request.phoneNumber().trim());
        student.setDepartment(request.department().trim());
        student.setGender(request.gender());
        student.setDateOfBirth(request.dateOfBirth());
        String address = request.address() == null ? null : request.address().trim();
        student.setAddress(address == null || address.isEmpty() ? null : address);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
