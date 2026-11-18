package com.demo.student.repository;

import com.demo.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Every query is scoped by owner id, so a user can only ever read or change
 * their own students. This is the backend ownership check.
 */
public interface StudentRepository extends JpaRepository<Student, UUID> {

    List<Student> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    Optional<Student> findByIdAndOwnerId(UUID id, UUID ownerId);

    boolean existsByOwnerIdAndEmail(UUID ownerId, String email);

    boolean existsByOwnerIdAndEmailAndIdNot(UUID ownerId, String email, UUID id);

    @Query("""
            SELECT s FROM Student s
            WHERE s.owner.id = :ownerId
              AND (LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY s.createdAt DESC
            """)
    List<Student> search(@Param("ownerId") UUID ownerId, @Param("keyword") String keyword);

    long countByOwnerId(UUID ownerId);

    long countByOwnerIdAndCreatedAtAfter(UUID ownerId, LocalDateTime after);

    List<Student> findTop5ByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    @Query("""
            SELECT s.department AS department, COUNT(s) AS count
            FROM Student s
            WHERE s.owner.id = :ownerId
            GROUP BY s.department
            ORDER BY COUNT(s) DESC, s.department ASC
            """)
    List<DepartmentCount> countByDepartment(@Param("ownerId") UUID ownerId);

    /** Spring Data projection for the department statistics query. */
    interface DepartmentCount {
        String getDepartment();

        long getCount();
    }
}
