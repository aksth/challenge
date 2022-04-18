package com.mindex.challenge.dao;

import com.mindex.challenge.data.ReportingStructure;

public interface MongoTemplateRepository {
    ReportingStructure getReportByEmployeeId(String employeeId);
}
