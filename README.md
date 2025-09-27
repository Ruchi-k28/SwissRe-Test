# Org Salary Structure

A simple Java SE (Maven) console application that reads an employees CSV and reports:
- managers who earn **less than** they should (by how much)
- managers who earn **more than** they should (by how much)
- employees whose reporting line to the CEO is **too long** (more than 4 managers in between), and by how much

## Rules
- For any manager with at least one direct report, compute the average salary of the **direct** subordinates.
- The manager's salary must be **at least 20% more** than that average and **no more than 50% more**.
- For reporting length, we count the number of **intermediate managers between the employee and the CEO**. If that number is **> 4**, it's too long by `count - 4`.

## Build & Run

```bash
mvn -q -e -DskipTests package
java -jar target/org-structure-analyzer-1.0.0-jar-with-dependencies.jar employees.csv
```

If no CSV path is provided, the app looks for `employees.csv` in the working directory.

## Input CSV format

```
Id,firstName,lastName,salary,managerId
123,Joe,Doe,60000,
124,Martin,Chekov,45000,123
...
```

- `managerId` is blank for the CEO.
- Up to 1000 rows as per spec.
- Salaries can be integer or decimal; they are parsed as `double`.

## Assumptions

- The CEO is the unique employee with an empty `managerId`.
- Reporting length counts managers **between** the employee and the CEO (excludes the employee and the CEO).
- Any line with malformed or missing required fields is skipped with a warning to stderr.
- Output is deterministic and sorted for readability.

## Tests

Run unit tests:

```bash
mvn test
```

The test suite covers parsing, average computations, salary rule checks, and reporting-line lengths using the provided example dataset.
