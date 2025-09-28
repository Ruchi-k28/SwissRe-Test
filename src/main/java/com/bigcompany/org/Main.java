package com.bigcompany.org;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.io.IOException;
import java.io.BufferedWriter;

public class Main {
	public static void main(String[] args) {
		try {
			String csvPath = args.length > 0 ? args[0] : "employees.csv";
			String outDir = args.length > 1 ? args[1] : "reports";
			var employees = EmployeeCsvParser.parse(Path.of(csvPath));
			if (employees.isEmpty()) {
				System.err.println("No employees parsed; exiting.");
				System.exit(1);
			}

			var violations = OrgAnalyzer.evaluateManagerSalaries(employees);
			var tooLong = OrgAnalyzer.findTooLongReportingLines(employees);
			System.out.println();
			System.out.println(
					"=================================::::::::::::::::::::::::::::::::|||| ORGANIZATION SALARY ANALYSIS REPORT ||||::::::::::::::::::::::::::::::::=================================");
			System.out.printf("\n%s%n", "*".repeat(85 + 1 + 8 + 1 + 15 * 5 + 6));
			final String title = "\n				    | Managers earning LESS than they should |						";
			final String headerFmt = "%-8s %-28s %15s %15s %15s %15s %15s%n";
			final String rowFmt = "%-8s %-28s %,15.2f %,15.2f %,15.2f %,15.2f %,15.2f%n";
			System.out.println(title);
			System.out.printf("%s%n", "-".repeat(28 + 1 + 8 + 1 + 15 * 5 + 6));
			System.out.printf(headerFmt, "ID", "Manager", "Current", "Avg Sub", "Min(120%)", "Max(150%)",
					"Underpaid By");
			System.out.printf("%s%n", "-".repeat(28 + 1 + 8 + 1 + 15 * 5 + 6));
			boolean anyUnder = false;
			for (var v : violations) {
				if (v.isUnderpaid) {
					anyUnder = true;
					System.out.printf(rowFmt, v.manager.getId(), v.manager.getFullName(), v.manager.getSalary(),
							v.averageSubSalary, 1.2 * v.averageSubSalary, 1.5 * v.averageSubSalary, v.delta);
				}
			}
			if (!anyUnder)
				System.out.println("None");

			final String title1 = "\n				    | Managers earning MORE than they should |						";
			System.out.printf("\n%s%n", "*".repeat(85 + 1 + 8 + 1 + 15 * 5 + 6));
			System.out.println(title1);
			System.out.printf("%s%n", "-".repeat(28 + 1 + 8 + 1 + 15 * 5 + 6));
			System.out.printf(headerFmt, "ID", "Manager", "Current", "Avg Sub", "Min(120%)", "Max(150%)",
					"Overpaid By");
			System.out.printf("%s%n", "-".repeat(28 + 1 + 8 + 1 + 15 * 5 + 6));
			boolean anyOver = false;
			for (var v : violations) {
				if (!v.isUnderpaid) {
					anyOver = true;
					System.out.printf(rowFmt, v.manager.getId(), v.manager.getFullName(), v.manager.getSalary(),
							v.averageSubSalary, 1.2 * v.averageSubSalary, 1.5 * v.averageSubSalary, v.delta);
				}
			}
			if (!anyOver)
				System.out.println("None");

			final String tlTitle = "\n| Employees with TOO-LONG reporting line (> 4 managers between them and CEO) |";
			final String tlHeaderFmt = "%-10s %-30s %22s %14s%n";
			final String tlRowFmt = "%-10s %-30s %22d %14d%n";
			System.out.printf("\n%s%n", "*".repeat(85 + 1 + 8 + 1 + 15 * 5 + 6));
			System.out.println(tlTitle);
			System.out.println("-".repeat(30 + 1 + 10 + 1 + 22 + 1 + 14));
			System.out.printf(tlHeaderFmt, "ID", "Employee", "Managers Between", "Excess By");
			System.out.println("-".repeat(30 + 1 + 10 + 1 + 22 + 1 + 14));
			if (tooLong.isEmpty()) {
				System.out.println("None");
			} else {
				tooLong.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getId())).forEach(entry -> {
					var e = entry.getKey();
					int byHowMuch = entry.getValue();
					System.out.printf(tlRowFmt, e.getId(), e.getFullName(), byHowMuch + 4, byHowMuch);
				});
			}

			// Also write CSV reports
			Path outPath = Path.of(outDir);
			if (!Files.exists(outPath)) {
				Files.createDirectories(outPath);
			}

			// Report 1: Underpaid managers
			Path underpaidCsv = outPath.resolve("report_underpaid_managers.csv");
			try (BufferedWriter bw = Files.newBufferedWriter(underpaidCsv)) {
				bw.write("managerId,managerName,managerSalary,avgDirectReportSalary,minAllowed(120%),delta,status\n");
				for (var v : violations) {
					if (v.isUnderpaid) {
						double minAllowed = 1.2 * v.averageSubSalary;
						bw.write(String.format(Locale.ROOT, "%s,%s,%.2f,%.2f,%.2f,%.2f,UNDERPAID\n", v.manager.getId(),
								v.manager.getFullName().replace(",", " "), v.manager.getSalary(), v.averageSubSalary,
								minAllowed, v.delta));
					}
				}
			}

			// Report 2: Overpaid managers
			Path overpaidCsv = outPath.resolve("report_overpaid_managers.csv");
			try (BufferedWriter bw = Files.newBufferedWriter(overpaidCsv)) {
				bw.write("managerId,managerName,managerSalary,avgDirectReportSalary,maxAllowed(150%),delta,status\n");
				for (var v : violations) {
					if (!v.isUnderpaid) {
						double maxAllowed = 1.5 * v.averageSubSalary;
						bw.write(String.format(Locale.ROOT, "%s,%s,%.2f,%.2f,%.2f,%.2f,OVERPAID\n", v.manager.getId(),
								v.manager.getFullName().replace(",", " "), v.manager.getSalary(), v.averageSubSalary,
								maxAllowed, v.delta));
					}
				}
			}

			// Report 3: Too-long reporting lines
			Path tooLongCsv = outPath.resolve("report_too_long_reporting_lines.csv");
			try (BufferedWriter bw = Files.newBufferedWriter(tooLongCsv)) {
				bw.write("employeeId,employeeName,managersBetweenCEO,excessBy\n");
				tooLong.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getId())).forEach(entry -> {
					var e = entry.getKey();
					int excess = entry.getValue();
					int between = excess + 4;
					try {
						bw.write(String.format(Locale.ROOT, "%s,%s,%d,%d\n", e.getId(),
								e.getFullName().replace(",", " "), between, excess));
					} catch (IOException io) {
						throw new RuntimeException(io);
					}
				});
			}

			System.out.println();
			System.out.println("CSV reports written to: " + outPath.toAbsolutePath());

		} catch (IOException ioe) {
			System.err.println("Failed to read CSV or write reports: " + ioe.getMessage());
			System.exit(2);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(3);
		}
	}
}
