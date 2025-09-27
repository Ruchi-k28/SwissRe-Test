package com.bigcompany.org;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class EmployeeCsvParser {

	public static List<Employee> parse(Path csvPath) throws IOException {
		List<Employee> employees = new ArrayList<>();
		try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
			String header = br.readLine();
			if (header == null) {
				throw new IOException("Empty CSV file: " + csvPath);
			}
			String line;
			int lineNo = 1;
			while ((line = br.readLine()) != null) {
				lineNo++;
				if (line.trim().isEmpty())
					continue;
				String[] parts = Arrays.stream(line.split(",")).map(String::trim).toArray(String[]::new);
				if (parts.length < 4) {
					System.err.println("Skipping malformed line " + lineNo + ": " + line);
					continue;
				}
				String id = parts[0];
				String first = parts[1];
				String last = parts[2];
				double salary;
				try {
					salary = Double.parseDouble(parts[3]);
				} catch (NumberFormatException nfe) {
					System.err.println("Skipping line " + lineNo + " due to invalid salary: " + parts[3]);
					continue;
				}
				String managerId = parts.length >= 5 ? parts[4] : null;
				employees.add(new Employee(id, first, last, salary, managerId));
			}
		}

		// Build manager-direct reports relationships
		Map<String, Employee> byId = employees.stream().collect(Collectors.toMap(Employee::getId, e -> e));
		for (Employee e : employees) {
			if (e.getManagerId() != null) {
				Employee mgr = byId.get(e.getManagerId());
				if (mgr != null) {
					mgr.getDirectReports().add(e);
				} else {
					System.err.println("Warning: Employee " + e + " references missing managerId=" + e.getManagerId());
				}
			}
		}
		return employees;
	}
}
