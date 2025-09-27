package com.bigcompany.org;

import java.util.*;
import java.util.stream.Collectors;

public class OrgAnalyzer {

	public static class SalaryViolation {
		public final Employee manager;
		public final double averageSubSalary;
		public final double delta; 
		public final boolean isUnderpaid; 

		public SalaryViolation(Employee manager, double averageSubSalary, double delta, boolean isUnderpaid) {
			this.manager = manager;
			this.averageSubSalary = averageSubSalary;
			this.delta = delta;
			this.isUnderpaid = isUnderpaid;
		}
	}

	public static Optional<Employee> findCeo(List<Employee> employees) {
		return employees.stream().filter(e -> e.getManagerId() == null).findFirst();
	}

	public static Map<String, Employee> indexById(List<Employee> employees) {
		return employees.stream().collect(Collectors.toMap(Employee::getId, e -> e));
	}

	public static List<SalaryViolation> evaluateManagerSalaries(List<Employee> employees) {
		List<SalaryViolation> out = new ArrayList<>();
		for (Employee m : employees) {
			List<Employee> subs = m.getDirectReports();
			if (subs.isEmpty())
				continue;
			double avg = subs.stream().mapToDouble(Employee::getSalary).average().orElse(0.0);
			double minAllowed = 1.2 * avg;
			double maxAllowed = 1.5 * avg;
			double sal = m.getSalary();
			if (sal < minAllowed) {
				out.add(new SalaryViolation(m, avg, minAllowed - sal, true));
			} else if (sal > maxAllowed) {
				out.add(new SalaryViolation(m, avg, sal - maxAllowed, false));
			}
		}
		out.sort(Comparator.comparing(v -> v.manager.getId()));
		return out;
	}

	
	 // Counts the number of managers strictly between employee e and the CEO.
	 
	public static int managersBetween(Employee e, Map<String, Employee> byId) {
		int between = 0;
		String currentMgrId = e.getManagerId();
		while (currentMgrId != null) {
			Employee mgr = byId.get(currentMgrId);
			if (mgr == null)
				break;
			if (mgr.getManagerId() == null) {
				between++;
				break;
			} else {
				between++;
				currentMgrId = mgr.getManagerId();
			}
		}
		return between;
	}

	public static Map<Employee, Integer> findTooLongReportingLines(List<Employee> employees) {
		Map<String, Employee> byId = indexById(employees);
		Map<Employee, Integer> tooLong = new TreeMap<>(Comparator.comparing(Employee::getId));
		for (Employee e : employees) {
			if (e.getManagerId() == null)
				continue; // CEO excluded
			int between = managersBetween(e, byId);
			if (between > 4) {
				tooLong.put(e, between - 4);
			}
		}
		return tooLong;
	}
}
