package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.dao.MongoTemplateRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MongoTemplateRepository mongoTemplateRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure getReportingByEmployeeId(String id) {
        LOG.debug("Getting direct reports for employee with id [{}]", id);

        List<Employee> employees = employeeRepository.findAll();
        Optional<Employee> employee = employees.stream().filter(e -> e.getEmployeeId().equals(id)).findFirst();
        if(!employee.isPresent()) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        Set<String> directReports = new HashSet<>();
        getDirectReports(employee.get(), directReports, employees);

        ReportingStructure reportingStructure = new ReportingStructure();
        reportingStructure.setEmployee(employee.get());
        reportingStructure.setNumberOfReports(directReports.size());

        return reportingStructure;
    }

    private void getDirectReports(Employee employee, Set<String> cumulativeReports, List<Employee> employees) {
        if(employee.getDirectReports() == null) {
            employee.setDirectReports(new ArrayList<>());
            return;
        } else if (employee.getDirectReports().size() == 0)
            return;
        for (int i = 0; i < employee.getDirectReports().size(); i++) {
            Employee directReport = employee.getDirectReports().get(i);
            if(cumulativeReports.contains(directReport.getEmployeeId())) {
                return;
            } else {
                cumulativeReports.add(directReport.getEmployeeId());

                Optional<Employee> directReportChild =
                        employees.stream().filter(e -> e.getEmployeeId().equals(directReport.getEmployeeId())).findFirst();
                employee.getDirectReports().set(i, directReportChild.orElse(null));
                if(!directReportChild.isPresent())
                    continue;
                else if(directReportChild.get().getDirectReports() == null) {
                    directReportChild.get().setDirectReports(new ArrayList<>());
                    continue;
                } else if(directReportChild.get().getDirectReports().size() == 0)
                    continue;
                getDirectReports(employee.getDirectReports().get(i), cumulativeReports, employees);
            }
        }
    }

}
