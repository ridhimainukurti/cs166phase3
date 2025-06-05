-- Drop Indexes if they exist first
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

-- Create Indexes for Flight-related tables
CREATE INDEX idx_flightnumber_flight ON Flight(FlightNumber);
CREATE INDEX idx_flightnumber_schedule ON Schedule(FlightNumber);
CREATE INDEX idx_flightnumber_flightinstance ON FlightInstance(FlightNumber);
CREATE INDEX idx_flightdate_flightinstance ON FlightInstance(FlightDate);

-- Create Indexes for Customer and Reservation
CREATE INDEX idx_reservationid_reservation ON Reservation(ReservationID);
CREATE INDEX idx_customerid_reservation ON Reservation(CustomerID);
CREATE INDEX idx_customerid_customer ON Customer(CustomerID);

-- Create Indexes for Repair-related tables
CREATE INDEX idx_technicianid_repair ON Repair(TechnicianID);
CREATE INDEX idx_planeid_repair ON Repair(PlaneID);
CREATE INDEX idx_repairdate_repair ON Repair(RepairDate);

-- Create Index for Plane table
CREATE INDEX idx_planeid_plane ON Plane(PlaneID);

-- Create Index for Pilot table
CREATE INDEX idx_pilotid_pilot ON Pilot(PilotID);

-- Create Index for MaintenanceRequest table
CREATE INDEX idx_pilotid_maintenancerequest ON MaintenanceRequest(PilotID);

