package com.bigcompany.org;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class OrgAnalyzerTest {

	@Test
	public void testExampleDataset() throws Exception {
		var employees = EmployeeCsvParser.parse(Path.of("employees.csv"));
		assertEquals(5, employees.size(), "should parse 5 employees");

		var ceo = OrgAnalyzer.findCeo(employees);
		assertTrue(ceo.isPresent());
		assertEquals("123", ceo.get().getId());

		var violations = OrgAnalyzer.evaluateManagerSalaries(employees);
	
		var mart = violations.stream().filter(v -> v.manager.getId().equals("124")).findFirst();
		assertTrue(mart.isPresent());
		assertTrue(mart.get().isUnderpaid);
		assertEquals(15000.0, Math.round(mart.get().delta)); // approx exact


		assertTrue(violations.stream().noneMatch(v -> !v.isUnderpaid));


		var tooLong = OrgAnalyzer.findTooLongReportingLines(employees);
		assertTrue(tooLong.isEmpty());
	}
}
