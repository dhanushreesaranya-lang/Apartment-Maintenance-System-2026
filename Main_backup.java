package com.apartment;
import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;
import java.nio.file.*;
import java.io.PrintWriter;

public class Main {
 private static final Scanner sc=new Scanner(System.in);
 public static void main(String[] args) {
  System.out.println("=== Apartment Maintenance & Complaint Management ===");
  try(Connection c=DB.connect()) {
   System.out.println("PostgreSQL connection successful!");
   while(true) {
    menu(); int choice=readInt("Choose: ");
    try {
     switch(choice) {
      case 1 -> addResident(c); case 2 -> listResidents(c);
      case 3 -> addApartment(c); case 4 -> listApartments(c);
      case 5 -> createBill(c); case 6 -> payBill(c);
      case 7 -> addComplaint(c); case 8 -> updateComplaint(c);
      case 9 -> listComplaints(c); case 10 -> exportReports(c);
      case 0 -> { System.out.println("Thank you!"); return; }
      default -> System.out.println("Invalid choice.");
     }
    } catch(Exception e) { System.out.println("Operation error: "+e.getMessage()); }
   }
  } catch(Exception e) {
   System.out.println("Could not connect: "+e.getMessage());
   System.out.println("Check PostgreSQL service, database and db.properties.");
  }
 }
 private static void menu() {
  System.out.println("\n1 Add resident\n2 List residents\n3 Add apartment\n4 List apartments"+
   "\n5 Create maintenance bill\n6 Record payment\n7 Register complaint"+
   "\n8 Update complaint status\n9 List complaints\n10 Export reports\n0 Exit");
 }
 private static String read(String p){System.out.print(p);return sc.nextLine().trim();}
 private static int readInt(String p){return Integer.parseInt(read(p));}
 private static double readDouble(String p){return Double.parseDouble(read(p));}
 private static void addResident(Connection c)throws SQLException {
  String sql="INSERT INTO residents(full_name,phone,email,apartment_id) VALUES(?,?,?,?)";
  try(PreparedStatement p=c.prepareStatement(sql)){
   p.setString(1,read("Name: "));p.setString(2,read("Phone: "));p.setString(3,read("Email: "));
   p.setInt(4,readInt("Apartment ID: "));p.executeUpdate();System.out.println("Resident added.");
  }
 }
 private static void listResidents(Connection c)throws SQLException {
  String q="SELECT r.resident_id,r.full_name,r.phone,r.email,COALESCE(a.flat_number,'-') FROM residents r LEFT JOIN apartments a ON a.apartment_id=r.apartment_id ORDER BY 1";
  try(Statement s=c.createStatement();ResultSet r=s.executeQuery(q)){
   System.out.println("ID | NAME | PHONE | EMAIL | FLAT");
   while(r.next())System.out.printf("%d | %s | %s | %s | %s%n",r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5));
  }
 }
 private static void addApartment(Connection c)throws SQLException {
  String q="INSERT INTO apartments(flat_number,block_name,floor_number,monthly_charge) VALUES(?,?,?,?)";
  try(PreparedStatement p=c.prepareStatement(q)){
   p.setString(1,read("Flat number: "));p.setString(2,read("Block: "));
   p.setInt(3,readInt("Floor: "));p.setDouble(4,readDouble("Monthly charge: "));
   p.executeUpdate();System.out.println("Apartment added.");
  }
 }
 private static void listApartments(Connection c)throws SQLException {
  try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM apartments ORDER BY apartment_id")){
   System.out.println("ID | FLAT | BLOCK | FLOOR | CHARGE");
   while(r.next())System.out.printf("%d | %s | %s | %d | %.2f%n",r.getInt(1),r.getString(2),r.getString(3),r.getInt(4),r.getDouble(5));
  }
 }
 private static void createBill(Connection c)throws SQLException {
  try(PreparedStatement p=c.prepareStatement("INSERT INTO maintenance_bills(resident_id,bill_month,amount) VALUES(?,?,?)")){
   p.setInt(1,readInt("Resident ID: "));p.setDate(2,Date.valueOf(LocalDate.parse(read("Month (YYYY-MM-DD): "))));
   p.setDouble(3,readDouble("Amount: "));p.executeUpdate();System.out.println("Bill created.");
  }
 }
 private static void payBill(Connection c)throws SQLException {
  String q="UPDATE maintenance_bills SET paid_amount=?,payment_status=CASE WHEN ?>=amount THEN 'PAID' WHEN ?>0 THEN 'PARTIAL' ELSE 'PENDING' END,paid_on=CASE WHEN ?>0 THEN CURRENT_DATE ELSE NULL END WHERE bill_id=?";
  try(PreparedStatement p=c.prepareStatement(q)){
   double v=readDouble("Total paid amount: ");p.setDouble(1,v);p.setDouble(2,v);p.setDouble(3,v);p.setDouble(4,v);p.setInt(5,readInt("Bill ID: "));
   System.out.println(p.executeUpdate()>0?"Payment updated.":"Bill not found.");
  }
 }
 private static void addComplaint(Connection c)throws SQLException {
  try(PreparedStatement p=c.prepareStatement("INSERT INTO complaints(resident_id,subject,description) VALUES(?,?,?)")){
   p.setInt(1,readInt("Resident ID: "));p.setString(2,read("Subject: "));p.setString(3,read("Description: "));
   p.executeUpdate();System.out.println("Complaint registered.");
  }
 }
 private static void updateComplaint(Connection c)throws SQLException {
  try(PreparedStatement p=c.prepareStatement("UPDATE complaints SET status=? WHERE complaint_id=?")){
   p.setString(1,read("Status OPEN / IN_PROGRESS / RESOLVED: ").toUpperCase());
   p.setInt(2,readInt("Complaint ID: "));System.out.println(p.executeUpdate()>0?"Status updated.":"Complaint not found.");
  }
 }
 private static void listComplaints(Connection c)throws SQLException {
  try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT complaint_id,resident_id,subject,status,complaint_date FROM complaints ORDER BY 1")){
   System.out.println("ID | RESIDENT | SUBJECT | STATUS | DATE");
   while(r.next())System.out.printf("%d | %d | %s | %s | %s%n",r.getInt(1),r.getInt(2),r.getString(3),r.getString(4),r.getTimestamp(5));
  }
 }
 private static void exportReports(Connection c)throws Exception {
  Path dir=Path.of("..","python","data");Files.createDirectories(dir);
  export(c,"SELECT * FROM payment_report","payment_report.csv",dir);
  export(c,"SELECT * FROM complaint_report","complaint_report.csv",dir);
  System.out.println("CSV files exported to python/data.");
 }
 private static void export(Connection c,String query,String file,Path dir)throws Exception {
  try(Statement s=c.createStatement();ResultSet r=s.executeQuery(query);
      PrintWriter w=new PrintWriter(Files.newBufferedWriter(dir.resolve(file)))){
   ResultSetMetaData m=r.getMetaData();int n=m.getColumnCount();
   for(int i=1;i<=n;i++){if(i>1)w.print(",");w.print("\""+m.getColumnLabel(i)+"\"");}w.println();
while(r.next()){for(int i=1;i<=n;i++){if(i>1)w.print(",");String v=r.getString(i);w.print("\""+(v==null?"":v.replace("\"","\"\""))+"\"");}w.println();}  }
 }
}
mm