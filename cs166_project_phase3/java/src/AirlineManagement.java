/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.util.UUID;
import javax.swing.*;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class AirlineManagement {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of AirlineManagement
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public AirlineManagement(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end AirlineManagement

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            AirlineManagement.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      AirlineManagement esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the AirlineManagement object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new AirlineManagement (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
               
                if (authorisedUser.equals("Manager")) {
                //**the following functionalities should only be able to be used by Management**
                System.out.println("1. View Flights");
                System.out.println("2. View Flight Seats");
                System.out.println("3. View Flight Status");
                System.out.println("4. View Flights of the day");  
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Traveler Information");
                System.out.println("7. View Plane Information");
                System.out.println("8. View Technician Repairs");
                System.out.println("9. View Plane Repair History");
                System.out.println("10. View Flight Statistics");

                } else if (authorisedUser.equals("Customer")) {
                //**the following functionalities should only be able to be used by customers**
                System.out.println("11. Search Flights");
                System.out.println("12. View Ticket Costs");
                System.out.println("13. View Airplane Type for Flight");
                System.out.println("14. Reserve a Flight (Waitlist if Needed)");

                } else if (authorisedUser.equals("Pilot")) {
                //**the following functionalities should ony be able to be used by Pilots**
                System.out.println("15. View Pilot Maintenance Requests");

                } else if (authorisedUser.equals("Technicians")) {
                //**the following functionalities should ony be able to be used by Technicians**
                System.out.println("16. View Plane Repair History");
                System.out.println("17. View Maintenace Requests");
                System.out.println("18. View Repair Information");

                }

                System.out.println("20. Log out");

                switch (readChoice()){
                   case 1: if (authorisedUser.equals("Manager")) feature1(esql); break;
                   case 2: if (authorisedUser.equals("Manager")) feature2(esql); break;
                   case 3: if (authorisedUser.equals("Manager")) feature3(esql); break;
                   case 4: if (authorisedUser.equals("Manager")) feature4(esql); break;
                   case 5: if (authorisedUser.equals("Manager")) feature5(esql); break;
                   case 6: if (authorisedUser.equals("Manager")) feature6(esql); break;
                   case 7: if (authorisedUser.equals("Manager")) feature7(esql); break;
                   case 8: if (authorisedUser.equals("Manager")) feature8(esql); break;
                   case 9: if (authorisedUser.equals("Manager")) feature9(esql); break;
                   case 10: if (authorisedUser.equals("Manager")) feature10(esql); break;

                   case 11: if (authorisedUser.equals("Customer")) feature11(esql); break;
                   case 12: if (authorisedUser.equals("Customer")) feature12(esql); break;
                   case 13: if (authorisedUser.equals("Customer")) feature13(esql); break;
                   case 14: if (authorisedUser.equals("Customer")) feature14(esql); break;

                   case 15: if (authorisedUser.equals("Pilot")) feature15(esql); break; 

                   case 16: if (authorisedUser.equals("Technician")) feature16(esql); break; 
                   case 17: if (authorisedUser.equals("Technician")) feature17(esql); break;
                   case 18: if (authorisedUser.equals("Technician")) feature18(esql); break;

                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   //HELPER FUNCTION 
   public static boolean isValidPassword(String password) {
      if (password.length() < 6) {
         System.out.println("The password must be at least 6 characters long.");
         return false; 
      }

      if (!password.matches(".*[A-Z].*")) {
         System.out.println("The password must contain at least one uppercase letter.");
         return false;
      }

      if (!password.matches(".*[!@#$%^&*()].*")) {
         System.out.println("The password must contain at least one special character (!@#$%^&*()).");
         return false;
      }
      return true;
   }

   public static void CreateUser(AirlineManagement esql) {
      try {
         String userName, password, role;

         //figure out what type of role the user is 
         do {
            System.out.println("Please enter your role (Customer, Pilot, Technician, Manager): ");
            role = in.readLine().trim();
            role = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
            if (!(role.equals("Customer") || role.equals("Pilot") || role.equals("Technician") || role.equals("Manager"))) {
               System.out.println("This is an invalid role. Please correctly enter Customer, Pilot, Technician, or Manager.");
               role = "";
            }
         } while (role.isEmpty());

         //now getting the userName and password 
         do {
            System.out.println("Please enter a username: "); 
            userName = in.readLine().trim();
         } while (userName.isEmpty()); 

         do {
            System.out.println("Please enter your password: "); 
            password = in.readLine().trim(); 
         } while (!isValidPassword(password)); 

         String userID = ""; 

         if (role.equals("Customer")) {
            //extra information needed for customer 
            String firstName, lastName, gender, dob, address, phoneNumber, zipcode;
            
            do {
               System.out.print("Please Enter Your First Name: ");
               firstName = in.readLine().trim();
            } while (firstName.isEmpty());
            
            do {
               System.out.print("Please Enter Your Last Name: ");
               lastName = in.readLine().trim();
            } while (lastName.isEmpty());
            
            do {
               System.out.print("Please Enter Your Gender (M/F): ");
               gender = in.readLine().trim().toUpperCase();
               if (!gender.equals("M") && !gender.equals("F")) {
                  System.out.println("This is an Invalid gender. Please enter M or F.");
                  gender = "";
               }
            } while (gender.isEmpty());
            
            do {
               System.out.print("Please Enter DOB (YYYY-MM-DD): ");
               dob = in.readLine().trim();
               if (!dob.matches("\\d{4}-\\d{2}-\\d{2}")) {
                  System.out.println("This is an Invalid date format. Use YYYY-MM-DD.");
                  dob = "";
               }
            } while (dob.isEmpty());

            do {
               System.out.print("Please Enter Your Address: ");
               address = in.readLine().trim();
            } while (address.isEmpty());

            do {
               System.out.print("Please Enter Your Phone Number: ");
               phoneNumber = in.readLine().trim();
               if (!phoneNumber.matches("[0-9()+\\-\\.x ]{7,30}")) {
                  System.out.println("This is an Invalid phone number format.");
                  phoneNumber = "";
               }
            } while (phoneNumber.isEmpty());

            do {
                  System.out.print("Please Enter Your Zipcode: ");
                  zipcode = in.readLine().trim();
                  if (!zipcode.matches("\\d{5}")) {
                     System.out.println("The Zipcode must be exactly 5 digits.");
                     zipcode = "";
                  }
            } while (zipcode.isEmpty());

            String query = "SELECT MAX(CustomerID) FROM Customer;";
            List<List<String>> resultList = esql.executeQueryAndReturnResult(query);
            
            //Creating Unqiue CustomerID
            int nextCustomerID = 1;
            if (!resultList.isEmpty() && resultList.get(0).get(0) != null) {
                  nextCustomerID = Integer.parseInt(resultList.get(0).get(0)) + 1;
               }

            userID = Integer.toString(nextCustomerID);
            
            //New User Intersertion 
            String insertCustomerQuery = String.format(
               "INSERT INTO Customer (CustomerID, FirstName, LastName, Gender, DOB, Address, Phone, Zip) " + 
               "VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s');", 
               nextCustomerID, firstName, lastName, gender, dob, address, phoneNumber, zipcode);

            esql.executeUpdate(insertCustomerQuery);

         } else if (role.equals("Pilot")) {
            //checking if there is an existing pilotID (validiation)
            do {
               System.out.println("Please enter the existing Pilot ID: "); 
               String pilotID = in.readLine().trim(); 

               if (pilotID.isEmpty()) {
                  System.out.println("The pilot ID can't be empty");
                  continue; 
               }

               String query = String.format("SELECT * FROM Pilot WHERE PilotID = '%s';", pilotID);
               if (esql.executeQuery(query) > 0) {
                  userID = pilotID;
                  break;
               } else {
                  System.out.println("This is an invalid Pilot ID.");
               }
            } while (true);
         } else if (role.equals("Technician")) {
            //check if there is an existing technician id
            do {
               System.out.println("Please enter tthe existing technician ID: "); 
               String technicianID = in.readLine().trim();

               if (technicianID.isEmpty()) {
                  System.out.println("The technician ID can't be empty.");
                  continue;
               }

               String query = String.format("SELECT * FROM Technician WHERE TechnicianID = '%s';", technicianID);
               if (esql.executeQuery(query) > 0) {
                  userID = technicianID; 
                  break; 
               } else {
                  System.out.println("This is an invalid technician ID.");
               }
            } while (true);

         } else if (role.equals("Manager")) {
            //manager has just a pretend id 
            String managerQuery = "SELECT MIN(CAST(userID AS INT)) FROM Login WHERE userID ~ '^[-]?[0-9]+$';";
            List<List<String>> resultList = esql.executeQueryAndReturnResult(managerQuery);

            int minID = -1;
            if (!resultList.isEmpty() && resultList.get(0).get(0) != null) {
               minID = Integer.parseInt(resultList.get(0).get(0));
               if (minID < 0) {
                  userID = Integer.toString(minID - 1);
               } else {
                  userID = "-1";
               }
            } else {
               userID = "-1";
            }
         }
      
      //putting information into LOGIN table 
      String insertLoginQuery = String.format( 
         "INSERT INTO Login (userID, username, password, role) " +
         "VALUES ('%s', '%s', '%s', '%s');",
         userID, userName, password, role); 
      
      esql.executeUpdate(insertLoginQuery);


      //inserting this customer 
      System.out.println("User was successfully created with role: " + role);
      
      } catch (Exception e) {
         System.err.println("There Was An Error in CreateUser: " + e.getMessage());
      }
   }
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(AirlineManagement esql){
      try {
         System.out.print("Please Enter Your Username: ");
         String userName = in.readLine();

         System.out.print("Please Enter Your Password: ");
         String password = in.readLine();

         String query = String.format(
            "SELECT role FROM Login WHERE userName = '%s' AND password = '%s';", 
            userName, password);

         List<List<String>> resultList = esql.executeQueryAndReturnResult(query);

         if (resultList.size() > 0) {
            String role = resultList.get(0).get(0);
            System.out.println("The Login Was Successful! Successfully logged in as: " + role);
            return role;
         } else {
            System.out.println("The Login Was Not Successful. Wrong username or password");
            return null;
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return null;
      }
   }//end

   // Rest of the functions definition go in here

   //given the flight number, get the flight's schedule for for the week
   //using Schedule Table
   public static void feature1(AirlineManagement esql) {
      try {
         System.out.print("Please Enter Your Flight Number: ");
         String flightNum = in.readLine();

         String query = String.format("SELECT DayOfWeek, DepartureTime, ArrivalTime " + 
         "FROM Schedule " + 
         "WHERE FlightNumber = '%s' " +
         "ORDER BY CASE " + 
         "WHEN DayOfWeek = 'Monday' THEN 1 " + 
         "WHEN DayOfWeek = 'Tuesday' THEN 2 " + 
         "WHEN DayOfWeek = 'Wednesday' THEN 3 " +
         "WHEN DayOfWeek = 'Thursday' THEN 4 " +
         "WHEN DayOfWeek = 'Friday' THEN 5 " +
         "WHEN DayOfWeek = 'Saturday' THEN 6 " +
         "WHEN DayOfWeek = 'Sunday' THEN 7 " +
         "END;", flightNum);

         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There is no schedule for this flight number.");
         }
      } catch (Exception e){
         System.err.println(e.getMessage());
      }
   }

   //given the flight and date, get the number of seats still available and the number of seats sold
   //using FlightInstance Table
   public static void feature2(AirlineManagement esql) {
      try {
         System.out.print("Please Enter Flight Number: ");
         String flightNum = in.readLine();

         System.out.print("Please Enter the Date of your Flight (MM/DD/YY): ");
         String flightDate = in.readLine();

         String query = String.format(
            "SELECT SeatsTotal - SeatsSold AS SeatsAvailable, SeatsSold " +
            "FROM FlightInstance " +
            "WHERE FlightNumber = '%s' AND FlightDate = '%s';",
            flightNum, flightDate);

         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There is no flight instance found for this flight number and date.");
         }
         
      } catch (Exception e){
         System.err.println(e.getMessage());
      }
   }

   //given the flight and date, find whether the flight departed on time and arrived on time
   //using FlightInstance Table
   public static void feature3(AirlineManagement esql) {
      try {
         System.out.print("Please Enter Flight Number: ");
         String flightNum = in.readLine();

         System.out.print("Please Enter the Date of your Flight (MM/DD/YY): ");
         String flightDate = in.readLine();

         String query = String.format(
            "SELECT DepartedOnTime, ArrivedOnTime " +
            "FROM FlightInstance " +
            "WHERE FlightNumber = '%s' AND FlightDate = '%s';",
            flightNum, flightDate);
            
         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There is no flight instance found for this flight number and date.");
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   //given a date, get all flight scheduled on that day
   //using FlightInstance Table
   public static void feature4(AirlineManagement esql) {
      try {
         System.out.print("Please Enter Flight Date (MM/DD/YY): ");
         String flightDate = in.readLine();

         String query = String.format(
            "SELECT FlightNumber, NumOfStops, TicketCost " +
            "FROM FlightInstance " +
            "WHERE FlightDate = '%s';",
            flightDate);
         
         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There is no flights scheduled for this flight date.");
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   //given a fight and date, get a list of passengers who made reservations, are on the waiting list, actually flew, on the flight (for flights already completed)
   //using Reservation Table and Customer Table
   public static void feature5(AirlineManagement esql) {
      try {
         System.out.println("Please Enter Reservation ID: ");
         String reserveID = in.readLine();

         String query = String.format(
            "SELECT C.FirstName, C.LastName, C.Gender, C.DOB, C.Address, C.Phone, C.Zip " +
            "FROM Reservation R, Customer C " +
            "WHERE R.ReservationID = '%s' AND R.CustomerID = C.CustomerID;",
            reserveID);

            int rowCount = esql.executeQueryAndPrintResult(query);

            if (rowCount == 0) {
            System.out.println("There are no reservations found for this ReservationID.");
            }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }


   //Given a reservation number, retrieve information about the traverlers under that number (First/last name, geneder, dob, address, phone number, zipcode)
   //Using Reservation and Customer Table 
   public static void feature6(AirlineManagement esql) {
      try {
         System.out.println("Please enter reservation ID: "); 
         String reserveID = in.readLine(); 

         String query = String.format(
            "SELECT C.FirstName, C.LastName, C.Gender, C.DOB, C.Address, C.Phone, C.Zip " +
            "FROM Customer C, Reservation R " +
            "WHERE R.ReservationID = '%s' AND R.CustomerID = C.CustomerID;",
            reserveID);
         
         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There is no information found for this Reservation ID");
         }
      } catch (Exception e) {
         System.err.println(e.getMessage()); 
      }
   }

   //given a plane number, get its make, model, age, last repair date 
   //Use Plane Table
   public static void feature7(AirlineManagement esql) {
      try {
         System.out.println("Please enter Plane ID: ");
         String planeID = in.readLine(); 

         String query = String.format(
            "SELECT Make, Model, (EXTRACT(YEAR FROM CURRENT_DATE) - Year) AS Age, LastRepairDate " +
            "FROM Plane " +
            "WHERE PlaneID = '%s';",
            planeID);
         
         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There is no information found for this Reservation ID");
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   //given a maintanence technician id, list all repairs made by that person 
   //Use Repair Table 
   public static void feature8(AirlineManagement esql) {
      try {
         System.out.println("Please enter technician ID: "); 
         String techID = in.readLine();

         String query = String.format(
            "SELECT PlaneID, RepairCode, RepairDate " +
            "FROM Repair " +
            "WHERE TechnicianID = '%s' " +
            "ORDER BY RepairDate ASC;",
            techID);

         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There is no repairs found for this Technician ID");
         }

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   //given a plane id and date range, list all the dates and codes for repairs performed 
   //Use Repair Table
   public static void feature9(AirlineManagement esql) {
      try {
         System.out.println("Please enter Plane ID: "); 
         String planeID = in.readLine();

         System.out.println("Please enter start date (YYYY-MM-DD): ");
         String startDate = in.readLine(); 

         System.out.println("Please enter end date (YYYY-MM-DD): ");
         String endDate = in.readLine(); 

         String query = String.format(
            "SELECT RepairDate, RepairCode " +
            "FROM Repair " +
            "WHERE PlaneID = '%s' " +
            "AND RepairDate BETWEEN '%s' AND '%s' " +
            "ORDER BY RepairDate ASC;",
            planeID, startDate, endDate);

         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There is no repairs found for this Plane ID and date range");
         }

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }


   //given a flight and range of date (start date and end date), show the statistics of the flight 
   //number of days the flight departed and arrived, number of sold and unsold tickets
   //Use FlightInstance Table
   public static void feature10(AirlineManagement esql) {
      try {
         System.out.println("Please enter flight number: "); 
         String flightNum = in.readLine();

         System.out.println("Please enter start date (MM/DD/YY): ");
         String startDate = in.readLine(); 

         System.out.println("Please enter end date (MM/DD/YY): ");
         String endDate = in.readLine(); 

         String query = String.format(
            "SELECT " +
            "COUNT(CASE WHEN DepartedOnTime = TRUE THEN 1 END) AS DepartedOnTimeCount, " +
            "COUNT(CASE WHEN ArrivedOnTime = TRUE THEN 1 END) AS ArrivedOnTimeCount, " +
            "SUM(SeatsSold) AS TotalSeatsSold, " +
            "SUM(SeatsTotal - SeatsSold) AS TotalSeatsUnsold " +
            "FROM FlightInstance " +
            "WHERE FlightNumber = '%s' " +
            "AND FlightDate BETWEEN '%s' AND '%s';",
            flightNum, startDate, endDate);

         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There is no flight instantances found for this Flight Number and date range");
         }

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   //new york, miami, 5/5/25
   //given a destination and a departure city, find all fights on a given date 
   //(must return departure and arrival time, number of stops scheduled and ontimerecord as a percentage)
   //using Flight table and FlightInstance table and Schedule Table
   public static void feature11(AirlineManagement esql) {
      try {
      System.out.print("Please Enter Departure City: ");
      String departureCity = in.readLine();

      System.out.print("Please Enter Arrival City: ");
      String arrivalCity = in.readLine();

      System.out.print("Please  Flight Date (MM/DD/YY): ");
      String flightDate = in.readLine();

      String query = String.format(
            "SELECT S.DepartureTime, S.ArrivalTime, FI.NumOfStops, " +
            "ROUND(100.0 * " +
            "(SELECT COUNT(*) FROM FlightInstance FI2 " +
            " WHERE FI2.FlightNumber = F.FlightNumber " +
            " AND FI2.DepartedOnTime = TRUE AND FI2.ArrivedOnTime = TRUE) / " +
            "(SELECT COUNT(*) FROM FlightInstance FI3 " +
            " WHERE FI3.FlightNumber = F.FlightNumber), 2) AS OnTimePercentage " +
            "FROM Flight F " +
            "JOIN FlightInstance FI ON F.FlightNumber = FI.FlightNumber " +
            "JOIN Schedule S ON F.FlightNumber = S.FlightNumber " +
            "WHERE F.DepartureCity = '%s' AND F.ArrivalCity = '%s' " +
            "AND FI.FlightDate = '%s';",
            departureCity, arrivalCity, flightDate);

         int rowCount = esql.executeQueryAndPrintResult(query);

            if (rowCount == 0) {
            System.out.println("There are no flights found for this city and date.");
            }
            
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   //given a flight number find the ticket cost 
   //Use FlightInstance Table 
   public static void feature12(AirlineManagement esql) {
      try {
         System.out.println("Please enter flight number: "); 
         String flightNum = in.readLine(); 

         String query = String.format(
            "SELECT DISTINCT TicketCost " +
            "FROM FlightInstance " +
            "WHERE FlightNumber = '%s';",
            flightNum);

         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There is no ticket costs found for this Flight Number");
         }

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   //given a flight number, find the airplane type (make, model)
   //Use Flight and Plane Table
   public static void feature13(AirlineManagement esql) {
      try {
         System.out.print("Please Enter Flight Number: ");
         String flightNum = in.readLine();

         String query = String.format(
            "SELECT P.Make, P.Model " +
            "FROM Flight F " +
            "JOIN Plane P ON F.PlaneID = P.PlaneID " +
            "WHERE F.FlightNumber = '%s';", 
            flightNum);

         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There are no planes found for this Flight Number.");
            }

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   //given a make reservation for a flight, get on the waitlist for a flight if the flight is full
   //Use Reservation and FlightInstance Table
   public static void feature14(AirlineManagement esql) {
      try {
         System.out.print("Please enter Customer ID: ");
         String customID = in.readLine();

         System.out.println("Please enter Flight Instance ID: ");
         String flightInstantceID = in.readLine(); 

         //checking for the number of seats
         String checkingSeatsQuery = String.format(
            "SELECT SeatsSold, SeatsTotal " +
            "FROM FlightInstance " +
            "WHERE FlightInstanceID = %s;",
            flightInstantceID);
         
         List<List<String>> resultList = esql.executeQueryAndReturnResult(checkingSeatsQuery);

         if (resultList.isEmpty()) {
            System.out.println("Thi is the wrong flight instance ID.");
            return;
         }

         int seatsSold = Integer.parseInt(resultList.get(0).get(0));
         int seatsTotal = Integer.parseInt(resultList.get(0).get(1));

         String currStatus;
         if (seatsSold < seatsTotal) {
            currStatus = "reserved"; 
         } else {
            currStatus = "waitlisted";
         }
         
         //creating unique reserationIDs
         String reserveID = "R" + UUID.randomUUID().toString().substring(0, 8);

         //inserting the reservation 
         String insertReservation = String.format(
            "INSERT INTO Reservation (ReservationID, CustomerID, FlightInstanceID, Status) " +
            "VALUES ('%s', %s, %s, '%s');",
            reserveID, customID, flightInstantceID, currStatus); 
         
         esql.executeUpdate(insertReservation);

         //if status is reserved then we will need to increment the seats sold 
         if (currStatus.equals("reserved")) {
            String updateSeatsQuery = String.format(
               "UPDATE FlightInstance " +
               "SET SeatsSold = SeatsSold + 1 " +
               "WHERE FlightInstanceID = %s;", flightInstantceID); 
            
            esql.executeUpdate(updateSeatsQuery);
         }

         System.out.println("Reservation " + (currStatus.equals("reserved") ? "confirmed" : "waitlisted") + ". Your Reservation ID is: " + reserveID);
         
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   //give a plane id and a date range, list all the dates and the codes for repairs performed
   //using Repair Table
   public static void feature15(AirlineManagement esql) {
      try {
         System.out.print("Please Enter Plane ID: ");
         String planeID = in.readLine();

         System.out.print("Please Enter Start Date (YYYY-MM-DD): ");
         String startDate = in.readLine();

         System.out.print("Please Enter End Date (YYYY-MM-DD): ");
         String endDate = in.readLine();

         String query = String.format(
            "SELECT RepairDate, RepairCode " +
            "FROM Repair " +
            "WHERE PlaneID = '%s' " +
            "AND RepairDate BETWEEN '%s' AND '%s' " +
            "ORDER BY RepairDate ASC;",
            planeID, startDate, endDate);

         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There are no repairs found for this planeID and and date range.");
            }

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   //given a plane Id and date range , list all the dates and the codes for repairs performed 
   //Use Repair Table
   public static void feature16(AirlineManagement esql) {
      try {
         System.out.println("Please enter Plane ID: "); 
         String planeID = in.readLine();

         System.out.println("Please enter start date (YYYY-MM-DD): ");
         String startDate = in.readLine(); 

         System.out.println("Please enter end date (YYYY-MM-DD): ");
         String endDate = in.readLine(); 

         String query = String.format(
            "SELECT RepairDate, RepairCode " +
            "FROM Repair " +
            "WHERE PlaneID = '%s' " +
            "AND RepairDate BETWEEN '%s' AND '%s' " +
            "ORDER BY RepairDate ASC;",
            planeID, startDate, endDate);

         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There is no repairs found for this Plane ID and date range");
         }

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   //given a pilot ID, list all maintenance request made by that pilot 
   //Use MaintenanceRequest Table 
   public static void feature17(AirlineManagement esql) {
      try {
         System.out.print("Please Enter Pilot ID: ");
         String pilotID = in.readLine();

         String query = String.format(
           "SELECT PlaneID, RepairCode, RequestDate " +
            "FROM MaintenanceRequest " +
            "WHERE PilotID = '%s' " +
            "ORDER BY RequestDate ASC;",
            pilotID);

         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount == 0) {
            System.out.println("There are no maintenance requests found for this Plane ID.");
            }

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   //try, PL005, RC555, 2025-06-10, T003
   //after each repair, make an entry showing planeID, repair code, and date of repair
   //Use Repair Table 
   public static void feature18(AirlineManagement esql) {
      try {
         System.out.print("Please Enter Plane ID: ");
         String planeID = in.readLine();

         System.out.print("Please Enter Repair Code: ");
         String repairCode = in.readLine();

         System.out.print("Please Enter Repair Date (YYYY-MM-DD): ");
         String repairDate = in.readLine();

         System.out.print("Please Enter Technician Id: ");
         String technicianID = in.readLine();

         String getMaxIDQuery = "SELECT MAX(RepairID) FROM Repair;";
         List<List<String>> resultList = esql.executeQueryAndReturnResult(getMaxIDQuery);
         int newRepairID = 1;
         if (!resultList.isEmpty() && resultList.get(0).get(0) != null) {
            newRepairID = Integer.parseInt(resultList.get(0).get(0)) + 1;
         }

         String insertQuery = String.format(
            "INSERT INTO Repair (RepairID, PlaneID, RepairCode, RepairDate, TechnicianID) " +
            "VALUES (%d, '%s', '%s', '%s', '%s');",
            newRepairID, planeID, repairCode, repairDate, technicianID);  
         
         esql.executeUpdate(insertQuery);

         System.out.println("The repair entry was added successfully with the RepairID: " + newRepairID);

      } catch (Exception e) {
         System.err.println(e.getMessage());
      } 
   }
} //end AirlineManagement
