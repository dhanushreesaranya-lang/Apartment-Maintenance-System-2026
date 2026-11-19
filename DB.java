package com.apartment;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
public class DB {
 public static Connection connect() throws Exception {
  Properties p=new Properties();
  try(InputStream in=DB.class.getClassLoader().getResourceAsStream("db.properties")) {
   if(in==null) throw new IllegalStateException("db.properties file not found");
   p.load(in);
  }
  if("CHANGE_ME".equals(p.getProperty("db.password")))
   throw new IllegalStateException("Set your PostgreSQL password in db.properties");
  return DriverManager.getConnection(p.getProperty("db.url"),p.getProperty("db.user"),p.getProperty("db.password"));
 }
}
