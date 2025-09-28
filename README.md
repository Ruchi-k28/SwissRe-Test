# Org Salary Structure

A Java SE (Maven) console application that reads an **employees CSV**, analyzes the salary structure, and produces both:

- **Console output** (Excel-like tables)
- **CSV report files** for further analysis

---

## Features

- Identify managers who earn **less than they should** (underpaid) and show the shortfall  
- Identify managers who earn **more than they should** (overpaid) and show the excess  
- Detect employees whose reporting line to the CEO is **too long** (more than 4 managers in between)  

For each case, results are:
- **Printed in the console** in a tabular format
- **Exported to CSV files** under a `reports/` folder:
  - `report_underpaid_managers.csv`
  - `report_overpaid_managers.csv`
  - `report_too_long_reporting_lines.csv`

---

## Rules

- For any manager with at least one direct report:
  - Salary must be **≥ 120%** of the average salary of their direct reports
  - Salary must be **≤ 150%** of that average  
- Reporting length:
  - Count the number of **intermediate managers between the employee and the CEO**  
  - If the count is **> 4**, the reporting chain is too long by `count - 4`

---

## Build & Run

```bash
# Build project
mvn -q -e -DskipTests clean package

# Run the fat JAR (console + CSV outputs)
java -jar target/org-salary-calculation-1.0.0-jar-with-dependencies.jar employees.csv
```

- If no CSV path is provided, the app looks for `employees.csv` in the working directory.  
- Optionally, pass a second argument to specify the output directory for reports:
  ```bash
  java -jar target/org-salary-calculation-1.0.0-jar-with-dependencies.jar employees.csv out_reports
  ```

---

## Input CSV Format

```csv
Id,firstName,lastName,salary,managerId
123,Joe,Doe,60000,
124,Martin,Chekov,45000,123
125,Alice,Smith,50000,124
...
```

- `managerId` is blank for the CEO  
- Up to **1000 rows** supported  
- Salaries may be integers or decimals (parsed as `double`)  

---

## Assumptions

- Exactly one CEO exists (the only employee with no `managerId`)  
- Reporting length excludes the employee and CEO, and counts only managers in between  
- Malformed or incomplete lines are skipped with warnings to `stderr`  
- Output is deterministic and **sorted** for readability  

---

## Reports

CSV files generated in the output folder:

1. **report_underpaid_managers.csv**  
   Contains managers whose salary is below the allowed minimum.  

2. **report_overpaid_managers.csv**  
   Contains managers whose salary exceeds the allowed maximum.  

3. **report_too_long_reporting_lines.csv**  
   Contains employees whose reporting chain to the CEO is too long.  

---

## Tests

Run unit tests with:

```bash
mvn test
```

The test suite covers:
- CSV parsing  
- Salary validation rules  
- Underpaid / overpaid detection  
- Reporting line length calculation  
