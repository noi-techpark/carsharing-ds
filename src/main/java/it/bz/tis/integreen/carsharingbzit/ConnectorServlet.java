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
import it.bz.tis.integreen.carsharingbzit.tis.CarSharingXMLRPCPusher;
import it.bz.tis.integreen.carsharingbzit.tis.FakeConnector;
import it.bz.tis.integreen.carsharingbzit.tis.IXMLRPCPusher;
import java.util.Calendar;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author Davide Montesin <d@vide.bz>
 */
public class ConnectorServlet extends HttpServlet implements Runnable
{
   static final Logger logger    = LogManager.getLogger(ConnectorServlet.class);

   Thread              backgroundTask;
   boolean             destroy;

   ApiClient           apiClient = null;

   String[]            cityUIDs;

   IXMLRPCPusher       xmlrpcPusher;

   @Override
   public void init(ServletConfig config) throws ServletException
   {
      logger.debug("init(ServletConfig): begin");
      super.init(config);

      String endpoint = config.getInitParameter("endpoint");
      if (endpoint == null || endpoint.trim().length() == 0)
      {
         String msg = "endpoint not configured. Please configure it in the web.xml";
         logger.error(msg);
         throw new ServletException(msg);
      }
      String user = config.getInitParameter("user");
      String password = config.getInitParameter("password");
      String initCityUIDs = config.getInitParameter("cityUIDs");
      this.cityUIDs = initCityUIDs.split("\\s*,\\s*");

      this.apiClient = new ApiClient(endpoint, user, password);
      String xmlrpcPusherParam = config.getInitParameter("xmlrpcpusher");
      if (xmlrpcPusherParam != null && xmlrpcPusherParam.equals("fake"))
      {
         this.xmlrpcPusher = new FakeConnector();
      }
      else
      {
         this.xmlrpcPusher = new CarSharingXMLRPCPusher();
      }

      this.destroy = false;
      this.backgroundTask = new Thread(this);
      this.backgroundTask.setName("background-task");
      this.backgroundTask.start();
      logger.debug("init(ServletConfig): end");
   }

   @Override
   public void run()
   {
      logger.debug("run(): begin");
      boolean full = true;
      long updateTime = calcLastPastIntervall();
      while (true)
      {
         long start = System.currentTimeMillis();
         try
         {
            logger.debug("run(): iteration begin");
            ConnectorLogic.process(this.apiClient, this.cityUIDs, this.xmlrpcPusher, full, updateTime);
         }
         catch (Throwable exxx)
         {
            logger.debug("run(): exception executing task (catched)", exxx);
         }
         finally
         {
            long stop = System.currentTimeMillis();
            logger.debug(String.format("run(): iteration end in %08d millis!\n", stop - start));
         }
         try
         {
            updateTime += ConnectorLogic.INTERVALL;
            long sleep = updateTime - System.currentTimeMillis();
            if (sleep > 0)
            {
               Thread.sleep(sleep);
            }
         }
         catch (InterruptedException ixxx)
         {
            synchronized (this)
            {
               if (this.destroy)
               {
                  logger.debug("run(): InterruptedException received for destroy, exiting");
                  break;
               }
               else
               {
                  logger.error("run(): InterruptedException received not by a destroy !!! Why?");
               }
            }

         }
      }
      logger.debug("run(): end");
   }

   static long calcLastPastIntervall()
   {
      long now = System.currentTimeMillis();
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(now);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      long midnight = cal.getTimeInMillis();
      long daytime = now - midnight;
      long alreadyCompleteIntervalls = daytime / ConnectorLogic.INTERVALL;
      return midnight + alreadyCompleteIntervalls * ConnectorLogic.INTERVALL;
   }

   @Override
   public void destroy()
   {
      logger.debug("destroy(): begin");
      super.destroy();
      synchronized (this)
      {
         this.destroy = true;
      }
      this.backgroundTask.interrupt();
      try
      {
         this.backgroundTask.join();
      }
      catch (InterruptedException e)
      {
         logger.error("destroy(): join interrupted !!! Why?");
      }
      finally
      {
         logger.debug("destroy(); end");
      }
   }

}
