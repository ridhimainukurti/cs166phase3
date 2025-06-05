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

                //**the following functionalities should only be able to be used by customers**
                System.out.println("11. Search Flights");
                System.out.println(".........................");
                System.out.println(".........................");

                //**the following functionalities should ony be able to be used by Pilots**
                System.out.println("15. Maintenace Request");
                System.out.println(".........................");
                System.out.println(".........................");

               //**the following functionalities should ony be able to be used by Technicians**
                System.out.println(".........................");
                System.out.println(".........................");

                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: feature1(esql); break;
                   case 2: feature2(esql); break;
                   case 3: feature3(esql); break;
                   case 4: feature4(esql); break;
                   case 5: feature5(esql); break;
                   case 6: feature6(esql); break;
                   case 7: feature7(esql); break;
                   case 8: feature8(esql); break;
                   case 9: feature9(esql); break;


                   case 11: feature11(esql); break;



                   case 15: feature15(esql); break; 
                   



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

   public static void CreateUser(AirlineManagement esql) {
      try {
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
        int uniqueCustomerID = 1;
        if (!resultList.isEmpty() && resultList.get(0).get(0) != null) {
            uniqueCustomerID = Integer.parseInt(resultList.get(0).get(0)) + 1;
         }
        
      //New User Intersertion 
      String insertQuery = String.format("INSERT INTO Customer (CustomerID, FirstName, LastName, Gender, DOB, Address, Phone, Zip) " + "VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s');", uniqueCustomerID, firstName, lastName, gender, dob, address, phoneNumber, zipcode);

      esql.executeUpdate(insertQuery);
      System.out.println("User was successfully created!");
      
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
         System.out.print("Please Enter Your First Name: ");
         String firstName = in.readLine();

         System.out.print("Please Enter Your Last Name: ");
         String lastName = in.readLine();

         String query = String.format("SELECT CustomerID FROM Customer WHERE FirstName = '%s' AND LastName = '%s';", firstName, lastName);

         List<List<String>> resultList = esql.executeQueryAndReturnResult(query);
         if (resultList.size() > 0) {
            System.out.println("The Login Was Successful!");
            return firstName;
         } else {
            System.out.println("The Login Was Not Successful.");
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














} //end AirlineManagement

