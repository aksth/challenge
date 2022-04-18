package com.mindex.challenge.dao;

import com.mindex.challenge.data.ReportingStructure;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ObjectOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.ROOT;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Component
public class MongoTemplateRepositoryImpl implements MongoTemplateRepository {

    MongoTemplate mongoTemplate;

    public MongoTemplateRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * This method doesn't populate all the child direct report employees.
     * Can be used if no tree populating is required for the direct reports.
     *
     * Since $graphLookup is only supported by mongoDB versions from 3.4,
     * this method works only for only mongoDB 3.4 or higher.
     *
     * mongo-java-server has support for 3.6, but this method would still not work
     * because mongo-java-server hasn't implemented $graphLookup having sub-document references.
     * So, the reference of "directReports.employeeId" in this method will not work and
     * this method would fail with mongo-java-server.
     *
     * But it would work with real mongoDB engine. (Tested in version 5+)
     *
     * @param employeeId
     * @return
     */
    @Override
    public ReportingStructure getReportByEmployeeId(String employeeId) {
        try {
            Aggregation agg = Aggregation.newAggregation(
                    match(Criteria.where("employeeId").is(employeeId)),
                    Aggregation.graphLookup("employee")
                            .startWith("$employeeId")
                            .connectFrom("directReports.employeeId")
                            .connectTo("employeeId")
                            .depthField("depth")
                            .as("linkedDirectReports"),
                    Aggregation.project()
                            //.and().minus(1).as("numberOfReports")
                            .and(ObjectOperators.valueOf(ROOT).merge()).as("employee")
                            .and(ArithmeticOperators.Subtract.valueOf(ArrayOperators.arrayOf("$linkedDirectReports")
                                    .length()).subtract(1)).as("numberOfReports")
            );
            AggregationResults<ReportingStructure> result = mongoTemplate.aggregate(agg, "employee", ReportingStructure.class);
            ReportingStructure reportingStructure = result.getUniqueMappedResult();
            if(reportingStructure == null)
                throw new RuntimeException("Employee not found for id: " + employeeId);
            return reportingStructure;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong (getReportByEmployeeId): " + employeeId);
        }
    }
}
