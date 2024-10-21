package com.colak.springtutorial.employee.repository;

import com.colak.springtutorial.employee.jpa.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
