//package cecs.pkg323.java.term.project;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mimi Opkins with some tweaking from Dave Brown
 */
public class CECS323JavaTermProject {
    //  Database credentials
    static String USER;
    static String PASS;
    static String DBNAME;
    static Scanner INPUT = new Scanner(System.in);
// JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    static String DB_URL = "jdbc:derby://localhost:1527/";
//            + "testdb;user=";
/**
 * Takes the input string and outputs "N/A" if the string is empty or null.
 * @param input The string to be mapped.
 * @return  Either the input string or "N/A" as appropriate.
 */
    public static String dispNull (String input) {
        //because of short circuiting, if it's null, it never checks the length.
        if (input == null || input.length() == 0)
            return "N/A";
        else
            return input;
    }
    
    public static void main(String[] args) {
        databaseInput();
        Connection conn = connectToDB();
        String sel = displayOptions();
        while(!sel.equals("9")) {
            switch(sel) {
                case "1":
                    displayResultSet(executeStatement("SELECT * FROM WritingGroup", conn));
                    break;
                case "2":
                    displayResultSet(executePreparedStatement(conn, "SELECT * FROM WritingGroup WHERE GroupName = ?", getGroupInfo()));
                    break;
                case "3":
                    displayResultSet(executeStatement("SELECT * FROM Publisher", conn));
                    break;
                case "4":
                    displayResultSet(executePreparedStatement(conn, "SELECT * FROM Book WHERE BookTitle = ?", getBookInfo()));
                    break;
                case "5":
                    displayResultSet(executeStatement("SELECT * FROM Book", conn));
                    break;
                case "6":
                    prepareStatementForBookInsert(conn, "INSERT INTO BOOK (GroupName, BookTitle, PublisherName, YearPublished, NumberPages) "
                            + "Values(?,?,?,?,?)");
                    break;
                case "7":
                    prepareStatementForBookRemove(conn, "DELETE FROM BOOK WHERE BookTitle = ?");
                    break;
                case "8":
                    insertNewPublisherAndReplace(conn, "INSERT INTO PUBLISHER (PublisherName, PublisherAddress, PublisherPhone, "
                        + "PublisherEmail) VALUES (?, ?, ?, ?)");
                    break;
                    
            }
            
            System.out.println();
            sel = displayOptions();
        
        }
        
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(CECS323JavaTermProject.class.getName()).log(Level.SEVERE, null, ex);
        }  
        INPUT.close();
    }
 
    
    public static void databaseInput() {
        System.out.print("Name of the database (not the user account): ");
        DBNAME = INPUT.nextLine();
        System.out.print("Database user name: ");
        USER = INPUT.nextLine();
        System.out.print("Database password: ");
        PASS = INPUT.nextLine();
    }
    
    public static String displayOptions() {
        System.out.println("1. List all writing groups.");
        System.out.println("2. List all information from a specific writing group.");
        System.out.println("3. List all publishers.");
        System.out.println("4. List all information from a specific book.");
        System.out.println("5. List all books.");
        System.out.println("6. Insert a Book.");
        System.out.println("7. Remove a Book.");
        System.out.println("8. Insert a new publisher.");
        System.out.println("Any other input will exit the program.");

        String select = INPUT.nextLine();

        return select;
       
    }
    
    public static Connection connectToDB() {
        Connection conn = null;
        //Constructing the database URL connection string
        DB_URL = DB_URL + DBNAME + ";user="+ USER + ";password=" + PASS;
        
        try {
            //STEP 2: Register JDBC driver
            Class.forName("org.apache.derby.jdbc.ClientDriver");

            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL);
            
            System.out.println("Succesfully connected to DB!");
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } 
         
        return conn;
    }
    
       
    public static void prepareStatementForBookInsert(Connection conn, String stmt) {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(stmt);
        } catch (SQLException ex) {
            Logger.getLogger(CECS323JavaTermProject.class.getName()).log(Level.SEVERE, null, ex);
        }
        boolean continueEntry = false;
        
        do {
        System.out.println("Enter the Group Name: ");
        String groupName = INPUT.nextLine();
        System.out.println("Enter the Book Title: ");
        String bookTitle = INPUT.nextLine();
        System.out.println("Enter the Publisher Name: ");
        String publisherName = INPUT.nextLine();
        System.out.println("Enter the Year Published: ");
        String yearPublished = INPUT.nextLine();
        System.out.println("Enter the Number of Pages: ");
        String numberPages = INPUT.nextLine();
        
            try {
                pstmt.setString(1, groupName);
                pstmt.setString(2, bookTitle);
                pstmt.setString(3, publisherName);
                pstmt.setString(4, yearPublished);
                pstmt.setString(5, numberPages);
                continueEntry = false;
                pstmt.executeUpdate();
            } catch (SQLIntegrityConstraintViolationException ex) {
                //System.out.println(ex.getMessage());
                if(ex.getMessage().contains("BOOK_FK01")) {
                    System.out.println("ERROR: Group does not exist in database.");
                } else if (ex.getMessage().contains("BOOK_FK02")) {
                    System.out.println("ERROR: Publisher does not exist in database.");
                }
                System.out.println("Want to try again? (Y/N)");
                String input = INPUT.nextLine();
                input = input.toLowerCase();
                continueEntry = (input.equals("y"));
            } catch (SQLDataException ex) {
                //System.out.println(ex.getMessage());
                if(ex.getMessage().equals("The syntax of the string representation of a date/time value is incorrect.")) {
                    System.out.println("ERROR: Entered date incorrectly. Please enter as 'mm-dd-yyyy.'");
                } else if (ex.getMessage().equals("Invalid character string format for type INTEGER.")) {
                    System.out.println("ERROR: Entered not an integer for number of pages. Please enter as an integer.");
                }
                
                System.out.println("Want to try again? (Y/N)");
                String input = INPUT.nextLine();
                input = input.toLowerCase();
                continueEntry = (input.equals("y"));
            } catch (SQLException ex) {
                Logger.getLogger(CECS323JavaTermProject.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while(continueEntry);
        
        
        
    }
    
        public static void prepareStatementForBookRemove(Connection conn, String stmt) {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(stmt);
        } catch (SQLException ex) {
            Logger.getLogger(CECS323JavaTermProject.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Enter the Book Title: ");
        String bookTitle = INPUT.nextLine();
            
        try {
            pstmt.setString(1, bookTitle);
            int result = pstmt.executeUpdate();
            if(result == 1) {
                System.out.println(bookTitle + " was removed from database.");
            } else {
                System.out.println(bookTitle + " does not exist.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(CECS323JavaTermProject.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static void insertNewPublisherAndReplace (Connection conn, String stmt) {
        
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(stmt);
        } catch (SQLException ex) {
            Logger.getLogger(CECS323JavaTermProject.class.getName()).log(Level.SEVERE, null, ex);
        }
        String name = null;
        try {
            System.out.println("Please enter the publisher name: ");
            name = INPUT.nextLine();
            pstmt.setString(1, name);
            System.out.println("Please enter the publisher address: ");
            String addr = INPUT.nextLine();
            pstmt.setString(2, addr);
            System.out.println("Please enter the publisher phone number: ");
            String phone = INPUT.nextLine();
            pstmt.setString(3, phone);
            System.out.println("Please enter the publisher email: ");
            String email = INPUT.nextLine();
            pstmt.setString(4, email);
        } catch (SQLException ex) {
            System.out.println("Database error.");
        }
        
        try {
            pstmt.execute();
        } catch (SQLException ex) {
            System.out.println("Encountered database error while trying to execute instruction.");
            Logger.getLogger(CECS323JavaTermProject.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        displayResultSet(executeStatement("SELECT PublisherName FROM Publisher", conn));
        
        replacePublishers(conn, name);
    }
    
    public static void replacePublishers (Connection conn, String pubName) {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("UPDATE BOOK SET PublisherName = ? WHERE PublisherName = ?");
        } catch (SQLException ex) {
            Logger.getLogger(CECS323JavaTermProject.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Which publisher should be replaced? (All books will be updated.)");
        String oldPub = INPUT.nextLine();
        
        try {
            pstmt.setString(1, pubName);
            pstmt.setString(2, oldPub);
            pstmt.execute();
        } catch (SQLException ex) {
            
        }
        
    }
    public static String getGroupInfo () {
        System.out.println("Please enter the group name: ");
        String name = INPUT.nextLine();
        return name;      
    }
    
    public static String getBookInfo() {
        System.out.println("Please enter the book name: ");
        String name = INPUT.nextLine();
        return name;
    }
    
    public static ResultSet executePreparedStatement(Connection conn, String stmt, String bindVar) {
        ResultSet returnSet = null;
        PreparedStatement pstmt = null;
        try {
//            System.out.println(stmt);
//            System.out.println(bindVar);
            pstmt = conn.prepareStatement(stmt);
            pstmt.setString(1, bindVar);
            returnSet = pstmt.executeQuery();
        } catch (SQLException ex) {
             Logger.getLogger(CECS323JavaTermProject.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnSet;
    }
    public static ResultSet executeStatement(String instr, Connection conn) {

        ResultSet returnRS = null;
        try {
            Statement stmt = conn.createStatement();
            returnRS = stmt.executeQuery(instr);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return returnRS;
    }
    
    public static void displayResultSet(ResultSet result) {
        ResultSetMetaData data = null;
        try {
            data = result.getMetaData();
            ArrayList<String> colNames = new ArrayList<>();
            
            //get default col size
            int maxSize = data.getColumnDisplaySize(1);
            //get column names
            for (int i = 1; i <= data.getColumnCount(); i++) {
                colNames.add(data.getColumnName(i));
                if (data.getColumnDisplaySize(i)>maxSize) {
                    maxSize = data.getColumnDisplaySize(i);  
                }
            }
            
            String displayFormat = "%-" + maxSize + "s";
          
            for (int i = 0; i<colNames.size(); i++) {
                System.out.printf(displayFormat, colNames.get(i));
            }
            System.out.println();
            //display columns
            while (result.next()) {
                for (int i = 0; i < colNames.size(); i++) {
                    System.out.printf(displayFormat, result.getString(colNames.get(i)));
                }
                System.out.println();
             }
        } catch (SQLException ex) {
            Logger.getLogger(CECS323JavaTermProject.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
    




