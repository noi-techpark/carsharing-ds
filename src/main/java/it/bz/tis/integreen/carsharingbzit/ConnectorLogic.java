/*
carsharing-ds: car sharing datasource for the integreen cloud

Copyright (C) 2015 TIS Innovation Park - Bolzano/Bozen - Italy

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package it.bz.tis.integreen.carsharingbzit;

import it.bz.tis.integreen.carsharingbzit.api.ApiClient;
import it.bz.tis.integreen.carsharingbzit.api.GetStationRequest;
import it.bz.tis.integreen.carsharingbzit.api.GetStationResponse;
import it.bz.tis.integreen.carsharingbzit.api.GetVehicleRequest;
import it.bz.tis.integreen.carsharingbzit.api.GetVehicleResponse;
import it.bz.tis.integreen.carsharingbzit.api.ListStationsByCityRequest;
import it.bz.tis.integreen.carsharingbzit.api.ListStationsByCityResponse;
import it.bz.tis.integreen.carsharingbzit.api.ListVehicleOccupancyByStationRequest;
import it.bz.tis.integreen.carsharingbzit.api.ListVehicleOccupancyByStationResponse;
import it.bz.tis.integreen.carsharingbzit.api.ListVehicleOccupancyByStationResponse.VehicleAndOccupancies;
import it.bz.tis.integreen.carsharingbzit.api.ListVehiclesByStationsRequest;
import it.bz.tis.integreen.carsharingbzit.api.ListVehiclesByStationsResponse;
import it.bz.tis.integreen.carsharingbzit.api.ListVehiclesByStationsResponse.StationAndVehicles;
import it.bz.tis.integreen.dto.carsharing.*;
import it.bz.tis.integreen.carsharingbzit.tis.IXMLRPCPusher;
import it.bz.tis.integreen.dto.DataTypeDto;
import it.bz.tis.integreen.dto.SimpleRecordDto;
import it.bz.tis.integreen.dto.TypeMapDto;
import it.bz.tis.integreen.dto.carsharing.CarsharingStationDto;
import it.bz.tis.integreen.dto.carsharing.CarsharingVehicleDto;
import it.bz.tis.integreen.util.IntegreenException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import util.IntegreenException;

/**
 * 
 * @author Davide Montesin <d@vide.bz>
 */
public class ConnectorLogic
{
   final static long             INTERVALL                    = 10L * 60L * 1000L;

   public static final String    CARSHARINGSTATION_DATASOURCE = "Carsharingstation";
   public static final String    CARSHARINGCAR_DATASOURCE     = "Carsharingcar";

   static final SimpleDateFormat SIMPLE_DATE_FORMAT           = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"); // 2014-09-15T12:00:00

   static HashMap<String, String[]> process(ApiClient apiClient,
                                            String[] cityUIDs,
                                            IXMLRPCPusher xmlrpcPusher,
                                            HashMap<String, String[]> vehicleIdsByStationIds,
                                            long updateTime,
                                            ActivityLog activityLog,
                                            ArrayList<ActivityLog> lock) throws IOException
   {
      if (vehicleIdsByStationIds == null) // Do a full sync
      {
         vehicleIdsByStationIds = processSyncStations(apiClient, cityUIDs, xmlrpcPusher, activityLog, lock);
      }
      processPusDatas(apiClient, xmlrpcPusher, vehicleIdsByStationIds, updateTime, activityLog, lock);
      return vehicleIdsByStationIds;
   }

   static HashMap<String, String[]> processSyncStations(ApiClient apiClient,
                                                        String[] cityUIDs,
                                                        IXMLRPCPusher xmlrpcPusher,
                                                        ActivityLog activityLog,
                                                        ArrayList<ActivityLog> lock) throws IOException
   {
      ///////////////////////////////////////////////////////////////
      // Stations by city
      ///////////////////////////////////////////////////////////////

      ListStationsByCityRequest request = new ListStationsByCityRequest(cityUIDs);
      ListStationsByCityResponse response = apiClient.callWebService(request, ListStationsByCityResponse.class);
      CarsharingStationDto[] stations = response.getCityAndStations()[0].getStation();

      ///////////////////////////////////////////////////////////////
      // Stations details
      ///////////////////////////////////////////////////////////////

      String[] stationIds = new String[stations.length];
      for (int i = 0; i < stations.length; i++)
      {
         stationIds[i] = stations[i].getId();
      }

      GetStationRequest requestGetStation = new GetStationRequest(stationIds);
      GetStationResponse responseGetStation = apiClient.callWebService(requestGetStation, GetStationResponse.class);

      ///////////////////////////////////////////////////////////////
      // Vehicles by stations 
      ///////////////////////////////////////////////////////////////

      ListVehiclesByStationsRequest vehicles = new ListVehiclesByStationsRequest(stationIds);
      ListVehiclesByStationsResponse responseVehicles = apiClient.callWebService(vehicles,
                                                                                 ListVehiclesByStationsResponse.class);

      ///////////////////////////////////////////////////////////////
      // Vehicles details 
      ///////////////////////////////////////////////////////////////

      HashMap<String, String[]> vehicleIdsByStationIds = new HashMap<>();
      ArrayList<String> veichleIds = new ArrayList<String>();
      for (StationAndVehicles stationVehicles : responseVehicles.getStationAndVehicles())
      {
         String[] vehicleIds = new String[stationVehicles.getVehicle().length];
         vehicleIdsByStationIds.put(stationVehicles.getStation().getId(), vehicleIds);
         for (int i = 0; i < stationVehicles.getVehicle().length; i++)
         {
            CarsharingVehicleDto carsharingVehicleDto = stationVehicles.getVehicle()[i];
            veichleIds.add(carsharingVehicleDto.getId());
            vehicleIds[i] = carsharingVehicleDto.getId();
         }
      }

      GetVehicleRequest requestVehicleDetails = new GetVehicleRequest(veichleIds.toArray(new String[0]));
      GetVehicleResponse responseVehicleDetails = apiClient.callWebService(requestVehicleDetails,
                                                                           GetVehicleResponse.class);

      ///////////////////////////////////////////////////////////////
      // Write data to integreen
      ///////////////////////////////////////////////////////////////

      Object result = xmlrpcPusher.syncStations(CARSHARINGSTATION_DATASOURCE, responseGetStation.getStation());
      if (result instanceof IntegreenException)
      {
         throw new IOException("IntegreenException");
      }

      synchronized (lock)
      {
         activityLog.report += "syncStations("
                               + CARSHARINGSTATION_DATASOURCE
                               + "): "
                               + responseGetStation.getStation().length
                               + "\n";
      }

      result = xmlrpcPusher.syncStations(CARSHARINGCAR_DATASOURCE, responseVehicleDetails.getVehicle());
      if (result instanceof IntegreenException)
      {
         throw new IOException("IntegreenException");
      }

      synchronized (lock)
      {
         activityLog.report += "syncStations("
                               + CARSHARINGCAR_DATASOURCE
                               + "): "
                               + responseVehicleDetails.getVehicle().length
                               + "\n";
      }
      return vehicleIdsByStationIds;
   }

   static void processPusDatas(ApiClient apiClient,
                               IXMLRPCPusher xmlrpcPusher,
                               HashMap<String, String[]> vehicleIdsByStationIds,
                               long updateTime,
                               ActivityLog activityLog,
                               ArrayList<ActivityLog> lock) throws IOException
   {
      ///////////////////////////////////////////////////////////////
      // Read vehicles occupancy and calculate summaries
      ///////////////////////////////////////////////////////////////

      String created = String.valueOf(updateTime);

      // Current and forecast
      for (long forecast : new long[] { 0, 30L * 60L * 1000L })
      {
         String begin = String.valueOf(updateTime + forecast);
         // TODO begin buffer depends on car type
         String begin_carsharing = SIMPLE_DATE_FORMAT.format(new Date(updateTime - 30L * 60L * 1000L + forecast));
         String end = SIMPLE_DATE_FORMAT.format(new Date(updateTime + INTERVALL + forecast));

         String[] stationIds = vehicleIdsByStationIds.keySet().toArray(new String[0]);
         Arrays.sort(stationIds);
         HashMap<String, TypeMapDto> stationData = new HashMap<String, TypeMapDto>();
         HashMap<String, TypeMapDto> vehicleData = new HashMap<String, TypeMapDto>();
         for (String stationId : stationIds)
         {
            String[] vehicleIds = vehicleIdsByStationIds.get(stationId);
            ListVehicleOccupancyByStationRequest occupancyByStationRequest = new ListVehicleOccupancyByStationRequest(begin_carsharing,
                                                                                                                      end,
                                                                                                                      stationId,
                                                                                                                      vehicleIds);

            ListVehicleOccupancyByStationResponse responseOccupancy = apiClient.callWebService(occupancyByStationRequest,
                                                                                               ListVehicleOccupancyByStationResponse.class);

            VehicleAndOccupancies[] occupancies = responseOccupancy.getVehicleAndOccupancies();
            if (occupancies== null) 
            		continue;
            if (occupancies.length != vehicleIds.length) // Same number of responses as the number to requests
            {
               throw new IllegalStateException();
            }
            int free = 0;

            for (VehicleAndOccupancies vehicleOccupancy : occupancies)
            {
               if (vehicleOccupancy.getOccupancy().length > 1)
               {
                  throw new IllegalStateException("Why???");
               }
               int state = 0; // free
               if (vehicleOccupancy.getOccupancy().length == 1)
               {
                  state = 1;
               }
               else
               {
                  free++;
               }
               TypeMapDto typeMap = new TypeMapDto();
               vehicleData.put(vehicleOccupancy.getVehicle().getId(), typeMap);
               String type = "unknown";
               if (begin.equals(created))
            	   type = DataTypeDto.AVAILABILITY;
               else
            	   type = DataTypeDto.FUTURE_AVAILABILITY;
               Set<SimpleRecordDto> dtos = typeMap.getRecordsByType().get(type);
               if (dtos == null){
            	   dtos = new HashSet<SimpleRecordDto>();
            	   typeMap.getRecordsByType().put(type, dtos);
               }
               dtos.add(new SimpleRecordDto(updateTime + forecast,state+0.,600));
            }
            
            Set<SimpleRecordDto> dtos = new HashSet<SimpleRecordDto>();
            TypeMapDto typeMap = new TypeMapDto();
         	typeMap.getRecordsByType().put(DataTypeDto.NUMBER_AVAILABE, dtos);
            if (begin.equals(created))
            	dtos.add(new SimpleRecordDto(updateTime + forecast, free+0.,600));
			stationData.put(stationId, typeMap );

         }

         ///////////////////////////////////////////////////////////////
         // Write data to integreen
         ///////////////////////////////////////////////////////////////

         Object result = xmlrpcPusher.pushData(CARSHARINGSTATION_DATASOURCE, new Object[]{stationData});
         if (result instanceof IntegreenException)
         {
            throw new IOException("IntegreenException");
         }
         synchronized (lock)
         {
            activityLog.report += "pushData(" + CARSHARINGSTATION_DATASOURCE + "): " + stationData.size()+ "\n";
         }
         result = xmlrpcPusher.pushData(CARSHARINGCAR_DATASOURCE, new Object[]{vehicleData});
         if (result instanceof IntegreenException)
         {
            throw new IOException("IntegreenException");
         }
         synchronized (lock)
         {
            activityLog.report += "pushData(" + CARSHARINGCAR_DATASOURCE + "): " + vehicleData.size() + "\n";
         }
      }
   }
}
