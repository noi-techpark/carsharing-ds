
package it.bz.idm.carsharing.dto;

public class MyListVehicleOccupancyByStationResponse
{
   public static class VehicleAndOccupancies
   {
      MyCarsharingVehicleDto     vehicle;
      MyOccupancy[] occupancy;

      public MyCarsharingVehicleDto getVehicle()
      {
         return this.vehicle;
      }

      public MyOccupancy[] getOccupancy()
      {
         return this.occupancy;
      }
   }

   VehicleAndOccupancies[] vehicleAndOccupancies;

   public VehicleAndOccupancies[] getVehicleAndOccupancies()
   {
      return this.vehicleAndOccupancies;
   }
}
