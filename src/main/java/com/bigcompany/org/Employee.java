package com.bigcompany.org;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Employee {
	private final String id;
	private final String firstName;
	private final String lastName;
	private final double salary;
	private final String managerId; 
	private final List<Employee> directReports = new ArrayList<>();

	public Employee(String id, String firstName, String lastName, double salary, String managerId) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.salary = salary;
		this.managerId = managerId == null || managerId.isBlank() ? null : managerId.trim();
	}

	public String getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getFullName() {
		return firstName + " " + lastName;
	}

	public double getSalary() {
		return salary;
	}

	public String getManagerId() {
		return managerId;
	}

	public List<Employee> getDirectReports() {
		return directReports;
	}

	@Override
	public String toString() {
		return id + " " + getFullName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Employee))
			return false;
		Employee employee = (Employee) o;
		return Objects.equals(id, employee.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
