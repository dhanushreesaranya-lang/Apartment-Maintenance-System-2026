package com.apartment;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;
import java.nio.file.*;
import java.io.PrintWriter;

public class Main {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Apartment Maintenance & Complaint Management ===");

        try (Connection c = DB.connect()) {
            System.out.println("PostgreSQL connection successful!");

            while (true) {
                menu();
                int choice = readInt("Choose: ");

                try {
                    switch (choice) {
                        case 1 -> addResident(c);
                        case 2 -> listResidents(c);
                        case 3 -> addApartment(c);
                        case 4 -> listApartments(c);
                        case 5 -> createBill(c);
                        case 6 -> payBill(c);
                        case 7 -> addComplaint(c);
                        case 8 -> updateComplaint(c);
                        case 9 -> listComplaints(c);
                        case 10 -> exportReports(c);
                        case 0 -> {
                            System.out.println("Thank you!");
                            return;
                        }
                        default -> System.out.println("Invalid choice.");
                    }
                } catch (Exception e) {
                    System.out.println("Operation error: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("Could not connect: " + e.getMessage());
        }
    }

    private static void menu() {
        System.out.println("""

                1 Add resident
                2 List residents
                3 Add apartment
                4 List apartments
                5 Create maintenance bill
                6 Record payment
                7 Register complaint
                8 Update complaint status
                9 List complaints
                10 Export reports
                0 Exit
                """);
    }

    private static String read(String p) {
        System.out.print(p);
        return sc.nextLine().trim();
    }

    private static int readInt(String p) {
        return Integer.parseInt(read(p));
    }

    private static double readDouble(String p) {
        return Double.parseDouble(read(p));
    }

    private static void addResident(Connection c) throws SQLException {
        String sql = "INSERT INTO residents(name,phone,email,apartment_no) VALUES(?,?,?,?)";

        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, read("Name: "));
            p.setString(2, read("Phone: "));
            p.setString(3, read("Email: "));
            p.setString(4, read("Apartment number: "));
            p.executeUpdate();
            System.out.println("Resident added.");
        }
    }

    private static void listResidents(Connection c) throws SQLException {
        String sql = "SELECT id,name,phone,email,apartment_no FROM residents ORDER BY id";

        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {

            System.out.println("ID | NAME | PHONE | EMAIL | APARTMENT");

            while (r.next()) {
                System.out.printf("%d | %s | %s | %s | %s%n",
                        r.getInt("id"),
                        r.getString("name"),
                        r.getString("phone"),
                        r.getString("email"),
                        r.getString("apartment_no"));
            }
        }
    }

    private static void addApartment(Connection c) throws SQLException {
        String sql = """
                INSERT INTO apartments
                (apartment_no,block_number,floor_no,apartment_type,
                 monthly_charge,occupancy_status)
                VALUES(?,?,?,?,?,?)
                """;

        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, read("Apartment number: "));
            p.setString(2, read("Block number: "));
            p.setInt(3, readInt("Floor number: "));
            p.setString(4, read("Apartment type: "));
            p.setDouble(5, readDouble("Monthly charge: "));
            p.setString(6, read("Occupancy status: "));
            p.executeUpdate();
            System.out.println("Apartment added.");
        }
    }

    private static void listApartments(Connection c) throws SQLException {
        String sql = """
                SELECT id,apartment_no,block_number,floor_no,
                       apartment_type,monthly_charge,occupancy_status
                FROM apartments ORDER BY id
                """;

        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {

            System.out.println("ID | APARTMENT | BLOCK | FLOOR | TYPE | CHARGE | STATUS");

            while (r.next()) {
                System.out.printf("%d | %s | %s | %d | %s | %.2f | %s%n",
                        r.getInt("id"),
                        r.getString("apartment_no"),
                        r.getString("block_number"),
                        r.getInt("floor_no"),
                        r.getString("apartment_type"),
                        r.getDouble("monthly_charge"),
                        r.getString("occupancy_status"));
            }
        }
    }

    private static void createBill(Connection c) throws SQLException {
        String sql = """
                INSERT INTO maintenance_bills
                (apartment_id,bill_month,amount,paid_amount,payment_status)
                VALUES(?,?,?,0,'PENDING')
                """;

        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, readInt("Apartment ID: "));
            p.setDate(2, Date.valueOf(LocalDate.parse(
                    read("Bill month (YYYY-MM-DD): "))));
            p.setDouble(3, readDouble("Amount: "));
            p.executeUpdate();
            System.out.println("Bill created.");
        }
    }

    private static void payBill(Connection c) throws SQLException {
        int billId = readInt("Bill ID: ");
        double amount = readDouble("Payment amount: ");
        String method = read("Payment method: ");

        c.setAutoCommit(false);

        try {
            double oldPaid = 0;
            double total = 0;

            String select = """
                    SELECT amount,paid_amount
                    FROM maintenance_bills WHERE bill_id=?
                    FOR UPDATE
                    """;

            try (PreparedStatement p = c.prepareStatement(select)) {
                p.setInt(1, billId);

                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) {
                        System.out.println("Bill not found.");
                        c.rollback();
                        return;
                    }

                    total = r.getDouble("amount");
                    oldPaid = r.getDouble("paid_amount");
                }
            }

            double newPaid = oldPaid + amount;
            String status = newPaid >= total ? "PAID" : "PENDING";

            String update = """
                    UPDATE maintenance_bills
                    SET paid_amount=?,payment_status=?
                    WHERE bill_id=?
                    """;

            try (PreparedStatement p = c.prepareStatement(update)) {
                p.setDouble(1, newPaid);
                p.setString(2, status);
                p.setInt(3, billId);
                p.executeUpdate();
            }

            String insert = """
                    INSERT INTO payments(amount,payment_method,bill_id)
                    VALUES(?,?,?)
                    """;

            try (PreparedStatement p = c.prepareStatement(insert)) {
                p.setDouble(1, amount);
                p.setString(2, method);
                p.setInt(3, billId);
                p.executeUpdate();
            }

            c.commit();
            System.out.println("Payment recorded.");

        } catch (Exception e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(true);
        }
    }

    private static void addComplaint(Connection c) throws SQLException {
        String sql = """
                INSERT INTO maintenance_requests
                (resident_id,title,description,priority,status,category,complaint_date)
                VALUES(?,?,?,?,'OPEN',?,CURRENT_DATE)
                """;

        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, readInt("Resident ID: "));
            p.setString(2, read("Title: "));
            p.setString(3, read("Description: "));
            p.setString(4, read("Priority: "));
            p.setString(5, read("Category: "));
            p.executeUpdate();
            System.out.println("Complaint registered.");
        }
    }

    private static void updateComplaint(Connection c) throws SQLException {
        String sql = """
                UPDATE maintenance_requests
                SET status=?
                WHERE id=?
                """;

        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1,
                    read("Status OPEN / IN_PROGRESS / RESOLVED: ")
                    .toUpperCase());
            p.setInt(2, readInt("Complaint ID: "));
            System.out.println(
                    p.executeUpdate() > 0
                    ? "Status updated."
                    : "Complaint not found.");
        }
    }

    private static void listComplaints(Connection c) throws SQLException {
        String sql = """
                SELECT id,resident_id,title,status,complaint_date
                FROM maintenance_requests ORDER BY id
                """;

        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {

            System.out.println("ID | RESIDENT | TITLE | STATUS | DATE");

            while (r.next()) {
                System.out.printf("%d | %d | %s | %s | %s%n",
                        r.getInt("id"),
                        r.getInt("resident_id"),
                        r.getString("title"),
                        r.getString("status"),
                        r.getDate("complaint_date"));
            }
        }
    }

    private static void exportReports(Connection c) throws Exception {
        Path dir = Path.of("..", "python", "data");
        Files.createDirectories(dir);

        export(c, "SELECT * FROM pending_payment_report",
                "pending_payment_report.csv", dir);

        export(c, "SELECT * FROM complaint_resolution_report",
                "complaint_resolution_report.csv", dir);

        export(c, "SELECT * FROM category_complaint_report",
                "category_complaint_report.csv", dir);

        System.out.println("CSV reports exported.");
    }

    private static void export(Connection c, String query,
                              String file, Path dir) throws Exception {

        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery(query);
             PrintWriter w = new PrintWriter(
                     Files.newBufferedWriter(dir.resolve(file)))) {

            ResultSetMetaData m = r.getMetaData();
            int n = m.getColumnCount();

            for (int i = 1; i <= n; i++) {
                if (i > 1) w.print(",");
                w.print("\"" + m.getColumnLabel(i) + "\"");
            }
            w.println();

            while (r.next()) {
                for (int i = 1; i <= n; i++) {
                    if (i > 1) w.print(",");
                    String v = r.getString(i);
                    w.print("\"" + (v == null
                            ? ""
                            : v.replace("\"", "\"\"")) + "\"");
                }
                w.println();
            }
        }
    }
}