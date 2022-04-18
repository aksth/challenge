package com.mindex.challenge.dao;

import com.mindex.challenge.data.ReportingStructure;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MongoTemplateRepositoryTest {

    @Autowired
    MongoTemplateRepository mongoTemplateRepository;

    @Test
    public void should_getReportByEmployeeId_when_validEmployeeIdGiven() {
        String employeeId = "16a596ae-edd3-4847-99fe-c4518e82c86f";
        ReportingStructure reportingStructure = mongoTemplateRepository
                .getReportByEmployeeId(employeeId);
        assertEquals(employeeId, reportingStructure.getEmployee().getEmployeeId());
        assertEquals(4, reportingStructure.getNumberOfReports());
    }

    @Test
    public void should_throwException_when_invalidIdGiven() {
        String employeeId = "123";
        assertThrows(RuntimeException.class, () -> mongoTemplateRepository
                .getReportByEmployeeId(employeeId));
    }
}