
package it.bz.idm.carsharing;

import it.bz.idm.bdp.dto.carsharing.CarsharingVehicleDto;

public class MyListVehicleOccupancyByStationResponse
{
   public static class VehicleAndOccupancies
   {
      CarsharingVehicleDto     vehicle;
      MyOccupancy[] occupancy;

      public CarsharingVehicleDto getVehicle()
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
