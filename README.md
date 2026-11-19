# Apartment Maintenance and Resident Complaint Management System

Integrated college project: Java 17 + PostgreSQL + JDBC + Python/Pandas/Matplotlib.

## Features
- Resident and apartment management
- Monthly maintenance bills and payment status
- Complaint registration and status tracking (OPEN, IN_PROGRESS, RESOLVED)
- Payment and complaint reports
- CSV export and Python visualizations

## Requirements
Java JDK 17+, PostgreSQL with pgAdmin 4, Apache Maven, Python 3.10+.

## Database setup
1. Open pgAdmin 4 and connect to your PostgreSQL server.
2. Create a database named `apartment_db`.
3. Open Query Tool for `apartment_db`.
4. Execute `database/schema.sql`.
5. Optionally execute `database/sample_data.sql`.

## Configure Java
Open `java-app/src/main/resources/db.properties`.
Set your PostgreSQL username and password. Default username is usually `postgres`.
Replace `CHANGE_ME` with the password you created during installation. Never share your password.

## Run Java
Open a terminal inside `java-app`:
```bash
mvn clean compile
mvn exec:java
```
Maven downloads the PostgreSQL JDBC driver on the first build. If `mvn` is not recognized, install Apache Maven and add it to PATH.

## Run Python charts
First use Java menu option 10 to export CSV reports. Then:
```bash
cd python
python -m pip install -r requirements.txt
python reports.py
```
Charts are saved to `python/output/`.

## Menu
1 Add resident | 2 List residents | 3 Add apartment | 4 List apartments
5 Create maintenance bill | 6 Record payment | 7 Register complaint
8 Update complaint status | 9 List complaints | 10 Export reports | 0 Exit

## Submission
Run and test the application on your computer, then add genuine screenshots to `evidence/`. Do not submit placeholder screenshots as execution proof.
