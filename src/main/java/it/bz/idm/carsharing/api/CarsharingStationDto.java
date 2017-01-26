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

package it.bz.idm.carsharing.api;

import it.bz.idm.carsharing.dto.StationDto;

/**
 * 
 * @author Davide Montesin <d@vide.bz>
 */
public class CarsharingStationDto extends StationDto
{
   boolean  hasFixedParking;
   BookMode bookMode;

   public void setUid(String uid)
   {
      this.setId(uid);
   }

   public void setHasFixedParking(boolean hasFixedParking)
   {
      this.hasFixedParking = hasFixedParking;
   }

   public boolean isHasFixedParking()
   {
      return this.hasFixedParking;
   }

   public void setGeoPos(GeoPos geoPos)
   {
      this.setLatitude(Double.parseDouble(geoPos.getLat()));
      this.setLongitude(Double.parseDouble(geoPos.getLon()));
   }

   public void setBookMode(BookMode bookMode)
   {
      this.bookMode = bookMode;
   }

   public BookMode getBookMode()
   {
      return this.bookMode;
   }
}
