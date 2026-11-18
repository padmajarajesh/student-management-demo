package com.demo.student.service;

import com.demo.student.dto.DashboardStatsResponse;
import com.demo.student.dto.DashboardStatsResponse.DepartmentStat;
import com.demo.student.dto.StudentResponse;
import com.demo.student.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/** All numbers are computed live from PostgreSQL for the current user. */
@Service
public class DashboardService {

    private final StudentRepository studentRepository;

    public DashboardService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats(UUID ownerId) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        List<DepartmentStat> departments = studentRepository.countByDepartment(ownerId).stream()
                .map(d -> new DepartmentStat(d.getDepartment(), d.getCount()))
                .toList();

        List<StudentResponse> recent = studentRepository.findTop5ByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(StudentResponse::from)
                .toList();

        return new DashboardStatsResponse(
                studentRepository.countByOwnerId(ownerId),
                studentRepository.countByOwnerIdAndCreatedAtAfter(ownerId, now.minusDays(7)),
                studentRepository.countByOwnerIdAndCreatedAtAfter(ownerId, now.minusDays(30)),
                departments.size(),
                departments,
                recent);
    }
}
