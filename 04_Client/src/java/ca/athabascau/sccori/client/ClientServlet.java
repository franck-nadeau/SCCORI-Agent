package ca.athabascau.sccori.client;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.webcontainer.WebContainerServlet;
/**
 * This is the Servlet which is serving the application. 
 * This is required by the Echo2 framework, and is 
 * configured inside the web.xml file.  
 * 
 * @author franck
 */
public class ClientServlet extends WebContainerServlet {	
    /**
     * 
     */
    public ApplicationInstance newApplicationInstance() {        
        ClientApplication theApplication =new ClientApplication();
        SimulationPool.getInstance().addApplication(theApplication);
        return theApplication;
    }
}