package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String compensationUrl;
    private String compensationEmployeeUrl;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompensationService compensationService;

    private Compensation savedCompensation;

    @Before
    public void setup() {
        compensationUrl = "http://localhost:" + port + "/compensation";
        compensationEmployeeUrl = "http://localhost:" + port + "/compensation/employee/{employeeId}";
        savedCompensation = restTemplate.postForEntity(compensationUrl, compensation(), Compensation.class).getBody();
    }

    @Test
    public void should_createCompensation_when_compensationGiven() {
        assertNotNull(savedCompensation);
        assertEquals(compensation().getEmployee().getEmployeeId(), savedCompensation.getEmployee().getEmployeeId());
    }

    @Test
    public void should_getByEmployeeId_when_existingEmployeeIdGiven() {
        Compensation compensation = restTemplate.getForEntity(compensationEmployeeUrl, Compensation.class,
                compensation().getEmployee().getEmployeeId()).getBody();
        assertNotNull(compensation);
        assertEquals(compensation().getEmployee().getEmployeeId(), compensation.getEmployee().getEmployeeId());
    }

    @Test
    public void should_throwException_when_nonExistingEmployeeIdGiven() {
        assertThrows(RuntimeException.class, () -> compensationService.getByEmployeeId("123"));
    }

    private Compensation compensation() {
        Employee employee = new Employee();
        employee.setEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f");
        Compensation request = new Compensation();
        request.setEmployee(employee);
        request.setSalary(new BigDecimal(50000));
        request.setEffectiveDate(LocalDateTime.now());
        return request;
    }
}