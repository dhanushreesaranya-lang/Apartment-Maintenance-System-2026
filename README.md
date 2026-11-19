# Apartment Maintenance & Complaint Management System

Java 17, Maven and PostgreSQL command-line application for apartment administration.

## Confirmed functions
- Add/list residents
- Add/list apartments
- Create maintenance bills
- Record payments
- Register/update/list complaints
- Export supported reports as CSV

## Stack
Java 17 • Apache Maven • PostgreSQL • JDBC • Python (optional data retrieval, preprocessing and charts)

## Run Java application
1. Start PostgreSQL and confirm `apartment_db` exists.
2. Configure the local `db.properties` file expected by the Java application.
3. Open terminal in `java-app`.
4. Run `mvn clean package` then `mvn exec:java`.

## Python analysis
From `python/`: install `requirements.txt`; copy `.env.example` to `.env` and enter local DB credentials; run `python retrieve_database.py`, `python preprocess_data.py`, then `python charts_from_csv.py`.

## Security
Never commit a real database password, `.env`, sensitive resident data or database dumps. The `.gitignore` excludes local secrets and build output.

## Documentation
See `documents/`, `diagrams/`, `sql/` and `testing/`.

## Important accuracy note
The ER diagram and relational model are reference designs, not verified exports of the current database. Run `sql/01_inspect_existing_schema.sql` and update names/relationships to match the live schema. Test cases show `NOT RUN` until executed; replace with actual observed results.

## GitHub
Create a repository, copy this pack and the existing `java-app` source into it, verify secrets are excluded, then commit and push. Add the resulting repository URL to your college submission.
