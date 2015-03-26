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
   static final Logger logger = LogManager.getLogger(ConnectorServlet.class);

   Thread              backgroundTask;
   boolean             destroy;

   String              endpoint;
   String              user;
   String              password;

   @Override
   public void init(ServletConfig config) throws ServletException
   {
      logger.debug("init(ServletConfig): begin");
      super.init(config);

      this.endpoint = this.getServletContext().getInitParameter("endpoint");
      if (this.endpoint == null || this.endpoint.trim().length() == 0)
      {
         String msg = "endpoint not configured. For dev put a context.xml file in /src/main/tomcatconf; for production in [tomcat]/conf/[engine-name]/[server-name]/[webapp].xml\n"
                      + "with the following content (replace ... with values):\n"
                      + "<Context>\n"
                      + "   <Parameter name=\"endpoint\" value=\"...\"/>\n"
                      + "   <Parameter name=\"user\" value=\"...\"/>\n"
                      + "   <Parameter name=\"password\" value=\"...\"/>\n"
                      + "</Context>\n";
         logger.error(msg);
         throw new ServletException(msg);
      }
      this.user = this.getServletContext().getInitParameter("user");
      this.password = this.getServletContext().getInitParameter("password");

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
      ApiClient apiClient = null;
      boolean first = true;
      while (true)
      {
         try
         {
            logger.debug("run(): iteration begin");
            if (!first)
            {
               Thread.sleep(15 * 60 * 1000);
            }
            first = false;
            long start = System.currentTimeMillis();
            try
            {
               if (apiClient == null)
               {
                  apiClient = new ApiClient(this.endpoint, this.user, this.password);
               }
               // TODO Do processing ...
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
