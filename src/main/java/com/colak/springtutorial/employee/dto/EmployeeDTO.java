package com.colak.springtutorial.employee.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class EmployeeDTO  {

    private Long id;

    private String firstName;

    private String lastName;
}
