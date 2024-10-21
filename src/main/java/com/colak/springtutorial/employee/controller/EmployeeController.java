package com.colak.springtutorial.employee.controller;


import com.colak.springtutorial.employee.dto.EmployeeDTO;
import com.colak.springtutorial.employee.jpa.Employee;
import com.colak.springtutorial.employee.mapstruct.EmployeeMapper;
import com.colak.springtutorial.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;


@RestController
@RequestMapping("/api/employee")
@CacheConfig(cacheNames = "employees")

@Slf4j
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // If we use @Cacheable a new post saving the same Employee will not work
    @PostMapping("/save")
    @Cacheable(key = "#employeeDTO.id")
    public EmployeeDTO save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("save is called with : {}", employeeDTO);

        // saving employee into db
        Employee employee = EmployeeMapper.INSTANCE.dtoToEmployee(employeeDTO);

        employeeService.save(employee);

        return EmployeeMapper.INSTANCE.employeeToDto(employee);
    }

    // To save an employee and update the cache
    @PostMapping("/saveOrUpdate")
    @Caching(evict = {@CacheEvict(key = "#employeeDTO.id")},
            put = {@CachePut(key = "#employeeDTO.id")})
    public EmployeeDTO saveOrUpdate(EmployeeDTO employeeDTO) {
        // saving employee into db
        Employee employee = EmployeeMapper.INSTANCE.dtoToEmployee(employeeDTO);

        employeeService.save(employee);

        return EmployeeMapper.INSTANCE.employeeToDto(employee);
    }

    @PostMapping("/update")
    @CachePut(key = "#employeeDTO.id", unless = "#employeeDTO != null")
    public EmployeeDTO update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("update is called with : {}", employeeDTO);

        // saving employee into db
        Employee employee = EmployeeMapper.INSTANCE.dtoToEmployee(employeeDTO);

        employeeService.save(employee);

        return EmployeeMapper.INSTANCE.employeeToDto(employee);
    }

    @GetMapping("/findAll")
    // @Cacheable for findAll is not a good idea because for each create/update/delete
    // either
    // - we would need to evict all entries for employees cache
    // or
    // - we can have a separate cache for findAll but, again we would need to evict all entries for employeesAll cache
    // for each create/update/delete
    public List<EmployeeDTO> findAll() {
        log.info("findAll is called");
        return employeeService.findAll()
                .stream()
                .map(EmployeeMapper.INSTANCE::employeeToDto)
                .toList();
    }

    // http://localhost:8080/api/employee/findById/1
    @GetMapping(path = "/findById/{id}")
    // Setting sync to true means that consecutive hits which happened before the cache was properly populated,
    // will wait for the cache to actually be populated, instead of performing another request to the database.
    @Cacheable(key = "#id", condition = "#id != null", sync = true)
    public EmployeeDTO findById(@PathVariable Long id) {
        log.info("findById is called with : {}", id);
        return employeeService.findById(id)
                .map(EmployeeMapper.INSTANCE::employeeToDto)
                // throws NoSuchElementException
                .orElseThrow();
    }

    @DeleteMapping(path = "/deleteById/{id}")
    // beforeInvocation attribute allows us to control the eviction process, enabling us to choose whether the eviction
    // should occur before or after the method execution.
    @CacheEvict(key = "#id", condition = "#id != null", beforeInvocation = true)
    public void deleteById(@PathVariable Long id) {
        log.info("deleteById is called with : {}", id);
        employeeService.deleteById(id);
    }

    @DeleteMapping(path = "/deleteAll")
    @CacheEvict(allEntries = true)
    public void deleteAll() {
        log.info("deleteAll is called");
        employeeService.deleteAll();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException exception) {
        // Return 404
        return ResponseEntity.notFound().build();
    }

}
