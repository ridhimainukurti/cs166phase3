-- indexing is for fast lookup of rows when searching certain columns

-- this is dropping the indexes first just to make sure that there are no duplicates and conflicts when they are created again
DROP INDEX IF EXISTS idx_flightnumber_flight;
DROP INDEX IF EXISTS idx_flightnumber_schedule;
DROP INDEX IF EXISTS idx_flightnumber_flightinstance;
DROP INDEX IF EXISTS idx_flightdate_flightinstance;
DROP INDEX IF EXISTS idx_reservationid_reservation;
DROP INDEX IF EXISTS idx_customerid_reservation;
DROP INDEX IF EXISTS idx_customerid_customer;
DROP INDEX IF EXISTS idx_technicianid_repair;
DROP INDEX IF EXISTS idx_planeid_repair;
DROP INDEX IF EXISTS idx_repairdate_repair;
DROP INDEX IF EXISTS idx_planeid_plane;
DROP INDEX IF EXISTS idx_pilotid_pilot;
DROP INDEX IF EXISTS idx_pilotid_maintenancerequest;

-- For Flight Tables
-- Any searches that use FlightNumber from Flight table will speed up the searches with a fligt number
CREATE INDEX idx_flightnumber_flight ON Flight(FlightNumber);
-- If we need to find the schedule for a given flight, we can quickly find it using the index
CREATE INDEX idx_flightnumber_schedule ON Schedule(FlightNumber);
-- When you have the FlightNumber, this index will help to find the fight instances on the different days
CREATE INDEX idx_flightnumber_flightinstance ON FlightInstance(FlightNumber);
-- If there are any queries that index by date, this will allow put an index for finding all the flights scheduled on that day 
CREATE INDEX idx_flightdate_flightinstance ON FlightInstance(FlightDate);

-- For Customer and Reservation Tables
-- To find the reservation details from ReservationID, this index helps for faster lookup
CREATE INDEX idx_reservationid_reservation ON Reservation(ReservationID);
-- To find all the reservations made by a specifc customer, using CustomerID, this index speeds up the process to find those reservation details
CREATE INDEX idx_customerid_reservation ON Reservation(CustomerID);
-- If there is a need to search for a customer given their ID, this index allows for this to be faster process
CREATE INDEX idx_customerid_customer ON Customer(CustomerID);

-- For Repair-related tables
-- We have a query where we need to find all the repairs that a certain Technician has done, and this index help speed up this process
CREATE INDEX idx_technicianid_repair ON Repair(TechnicianID);
-- If we need information regarding the repairs on a specific plane, this index optimizes that
CREATE INDEX idx_planeid_repair ON Repair(PlaneID);
-- If we need to see which repairs were done within a certain date timeframe, this index is targeting that
CREATE INDEX idx_repairdate_repair ON Repair(RepairDate);

-- For Plane table
-- This allows for indexing on finding a specific plane using the PlaneID
CREATE INDEX idx_planeid_plane ON Plane(PlaneID);

-- For Pilot table
-- To find pilotIDs quickly, this allows us to find them quickly
CREATE INDEX idx_pilotid_pilot ON Pilot(PilotID);

-- Create Index for MaintenanceRequest table
-- If there are maintanance request for a certain pilot, this allows for those requests to be found quickly 
CREATE INDEX idx_pilotid_maintenancerequest ON MaintenanceRequest(PilotID);

--Login Indexes are not here bc this system automatically does indexing for that