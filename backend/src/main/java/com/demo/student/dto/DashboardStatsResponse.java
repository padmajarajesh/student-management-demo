package com.demo.student.dto;

import java.util.List;

public record DashboardStatsResponse(
        long totalStudents,
        long studentsLast7Days,
        long studentsLast30Days,
        long totalDepartments,
        List<DepartmentStat> departmentCounts,
        List<StudentResponse> recentStudents
) {

    public record DepartmentStat(String department, long count) {
    }
}
