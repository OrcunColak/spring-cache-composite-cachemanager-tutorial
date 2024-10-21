package com.colak.springtutorial.employee.mapstruct;


import com.colak.springtutorial.employee.dto.EmployeeDTO;
import com.colak.springtutorial.employee.jpa.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EmployeeMapper {
    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    Employee dtoToEmployee(EmployeeDTO employeeDTO);
    EmployeeDTO employeeToDto(Employee employee);
}
