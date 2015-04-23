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
                                            long updateTime) throws IOException
   {
      if (vehicleIdsByStationIds == null) // Do a full sync
      {
         vehicleIdsByStationIds = processSyncStations(apiClient, cityUIDs, xmlrpcPusher);
      }
      processPusDatas(apiClient, xmlrpcPusher, vehicleIdsByStationIds, updateTime);
      return vehicleIdsByStationIds;
   }

   static HashMap<String, String[]> processSyncStations(ApiClient apiClient,
                                                        String[] cityUIDs,
                                                        IXMLRPCPusher xmlrpcPusher) throws IOException
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

      result = xmlrpcPusher.syncStations(CARSHARINGCAR_DATASOURCE, responseVehicleDetails.getVehicle());
      if (result instanceof IntegreenException)
      {
         throw new IOException("IntegreenException");
      }
      return vehicleIdsByStationIds;
   }

   static void processPusDatas(ApiClient apiClient,
                               IXMLRPCPusher xmlrpcPusher,
                               HashMap<String, String[]> vehicleIdsByStationIds,
                               long updateTime) throws IOException
   {
      ///////////////////////////////////////////////////////////////
      // Read vehicles occupancy and calculate summaries
      ///////////////////////////////////////////////////////////////

      String created = String.valueOf(updateTime);

      // Current and forecast
      for (long forecast : new long[] { 0, 30L * 60L * 1000L })
      {
         ArrayList<HashMap<String, String>> stationOccupancies = new ArrayList<>();
         ArrayList<HashMap<String, String>> vehicleOccupancies = new ArrayList<>();

         String begin = String.valueOf(updateTime + forecast);
         // TODO begin buffer depends on car type
         String begin_carsharing = SIMPLE_DATE_FORMAT.format(new Date(updateTime - 30L * 60L * 1000L + forecast));
         String end = SIMPLE_DATE_FORMAT.format(new Date(updateTime + INTERVALL + forecast));

         String[] stationIds = vehicleIdsByStationIds.keySet().toArray(new String[0]);
         Arrays.sort(stationIds);

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
               HashMap<String, String> vehicleData = new HashMap<String, String>();
               vehicleData.put(CarsharingVehicleDto.IDENTIFIER, vehicleOccupancy.getVehicle().getId());
               vehicleData.put(CarsharingVehicleDto.STATE, String.valueOf(state));
               vehicleData.put(CarsharingVehicleDto.TIMESTAMP, begin);
               vehicleData.put(CarsharingVehicleDto.CREATED_ON, created);
               vehicleOccupancies.add(vehicleData);
            }

            HashMap<String, String> stationData = new HashMap<String, String>();
            stationData.put(CarsharingStationDto.IDENTIFIER, stationId);
            stationData.put(CarsharingStationDto.VALUE_IDENTIFIER, String.valueOf(free));
            stationData.put(CarsharingStationDto.TIMESTAMP, begin);
            stationData.put(CarsharingStationDto.CREATED_ON, created);
            stationOccupancies.add(stationData);

         }

         ///////////////////////////////////////////////////////////////
         // Write data to integreen
         ///////////////////////////////////////////////////////////////

         Object result = xmlrpcPusher.pushData(CARSHARINGSTATION_DATASOURCE, stationOccupancies.toArray());
         if (result instanceof IntegreenException)
         {
            throw new IOException("IntegreenException");
         }

         result = xmlrpcPusher.pushData(CARSHARINGCAR_DATASOURCE, vehicleOccupancies.toArray());
         if (result instanceof IntegreenException)
         {
            throw new IOException("IntegreenException");
         }
      }
   }
}
