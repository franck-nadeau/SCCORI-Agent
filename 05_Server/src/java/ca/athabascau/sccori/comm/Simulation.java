package ca.athabascau.sccori.comm;

import java.io.IOException;
import java.lang.NumberFormatException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
//import java.util.SimpleDateFormat;
import java.util.logging.Level;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Jdk14Logger;
//import org.apache.axis.client.Call;
//import org.apache.axis.client.Service;
//import org.apache.axis.client.*;
//import org.apache.axis.utils.XMLUtils;
import javax.xml.namespace.QName;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import ca.athabascau.sccori.agent.ParanoidAgent;
import ca.athabascau.sccori.data.SimulationInfo;
import ca.athabascau.sccori.data.TurnEvent;
import ca.athabascau.sccori.data.TurnInfo;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindowAdapter;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * This class represents a single player's wrapper for a SCCORI simulation. That
 * is, four instance of this class will exist when four agents are playing a
 * single SCCORI simulation.
 */
public class Simulation {
    private static final Log theLog = LogFactory.getLog(Simulation.class);

    private static final String SIMULATION_VIEW = "http://www.sccori.com/sim/newSimulator.aspx";

    private static final String LOGIN_PAGE = "http://www.sccori.com/sim/Login.aspx";
    private static final String SIM_WAIT = "http://www.sccori.com/sim/newSimulator.aspx";

    private final URL theSimulationWaitURL;

    private final URL theSimulationViewURL;

    private HtmlPage theCurrentPage;

    private Map<String, String> theMeetingNames = new HashMap<String, String>();

    private List<TurnListener> theMyTurnListeners = new LinkedList<TurnListener>();

    final WebClient theWebClient;

    private final URL theSiteURL;

    private final SimulationInfo theSimulationInfo = new SimulationInfo();

    private WebWindowAdapter theAdapter;

    private TurnListener theAgent;

    private TurnEvent theLastEvent = new TurnEvent(0);

    private Date theLastTurnTime = new Date();

    private String theUserName = "";

    private String theMeeting = "";

    private EternalThreadedRefreshHandler theRefershHandler;

    /**
     * Create a Simulation wrapper to be used by a certain user.
     * 
     * @param aUserName
     *            the UserName used on the SCCORI login page
     * @param aPassword
     *            the password used on the SCCORI login page
     * @throws Exception
     *             if wrong password/wrong username/site is down.
     */
    public Simulation(String aUserName, String aPassword) throws Exception {
        // Turn annoying loggin off...
        Log theFirstLog = LogFactory
                .getLog("org.apache.commons.httpclient.HttpMethodBase");
        Log theSecondLog = LogFactory
                .getLog("org.apache.commons.httpclient.HttpMethodDirector");
        Log theThirdLog = LogFactory
                .getLog("org.apache.commons.httpclient.SimpleHttpConnectionManager");
        if (theFirstLog instanceof Jdk14Logger) {
            ((Jdk14Logger) theFirstLog).getLogger().setLevel(Level.OFF);
        }
        if (theSecondLog instanceof Jdk14Logger) {
            ((Jdk14Logger) theSecondLog).getLogger().setLevel(Level.OFF);
        }
        if (theThirdLog instanceof Jdk14Logger) {
            ((Jdk14Logger) theThirdLog).getLogger().setLevel(Level.OFF);
        }

        // Build some URLs needed for surfing the site
        theSiteURL = new URL(LOGIN_PAGE);
        theSimulationViewURL = new URL(SIMULATION_VIEW);
	theSimulationWaitURL = new URL(SIM_WAIT);

        theUserName = aUserName;

        // Init the Headless web browser
	BrowserVersion Firefox2 = BrowserVersion.FIREFOX_2;
        theWebClient = new WebClient(Firefox2);
        theRefershHandler = new EternalThreadedRefreshHandler();
        theRefershHandler.addErrorNotifier(new ErrorNotifier() {
            public void notify(Exception e) {
                // Get the Current Page - incase of redirects
                theCurrentPage = (HtmlPage) theWebClient.getCurrentWindow()
                        .getEnclosedPage();
                if (theCurrentPage.getTitleText().equals("SimResults")) {
                    unHookAgent();
                } else {
                    goToSimPage();
                }
            }
        });
        theWebClient.setRefreshHandler(theRefershHandler);
        theWebClient.setRedirectEnabled(true);
        theWebClient.setJavaScriptEnabled(true);
        theWebClient.setPrintContentOnFailingStatusCode(true);
        theWebClient.setThrowExceptionOnFailingStatusCode(true);

        // Login the Site
        login(aUserName, aPassword);

        // Add listener to keep track of turn played
        theMyTurnListeners.add(new TurnAdapter() {
            public void executeTurn(TurnEvent e) {
                theLastTurnTime = new Date();
            }
        });
    }

    public void setAgent(TurnListener anAgent) {
        // Remove the old agent if it exist
        theMyTurnListeners.remove(theAgent);

        theAgent = anAgent;
        theMyTurnListeners.add(theAgent);
    }

    public TurnListener getAgent() {
        return theAgent;
    }

    public TurnEvent getLastEvent() {
        return theLastEvent;
    }

    public Date getWaitTime() {
        return theLastTurnTime;
    }

    public SimulationInfo getSimulationInfo() {
        return theSimulationInfo;
    }

    /**
     * Add a listner to be notified every time it is this user's turn to play.
     * 
     * @param anAction
     */
    public void addMyTurnListener(TurnListener anAction) {
        theMyTurnListeners.add(anAction);
    }

    public String[] getAvailableMeeting() {
        return theMeetingNames.keySet().toArray(new String[] {});
    }

    public void destroy() {
        if (theAdapter != null) {
            theWebClient.removeWebWindowListener(theAdapter);
        }
    }

    public String getSelectedMeeting() {
        return theMeeting;
    }

    public String getUserName() {
        return theUserName;
    }

    private void login(String aUserName, String aPassword) throws Exception {
        theCurrentPage = (HtmlPage) theWebClient.getPage(theSiteURL);

        {// Login page
            HtmlForm form = theCurrentPage.getFormByName("Form1");
            HtmlSubmitInput button = (HtmlSubmitInput) form
                    .getInputByName("btnLogin");
            HtmlTextInput theUserNameField = (HtmlTextInput) form
                    .getInputByName("txtUserName");
            HtmlPasswordInput thePasswordField = (HtmlPasswordInput) form
                    .getInputByName("txtPassword");

            // Change the value of the text field
	    //theWebClient.setJavaScriptEnabled(false);

            theUserNameField.setValueAttribute(aUserName);
            thePasswordField.setValueAttribute(aPassword);

            //theWebClient.setJavaScriptEnabled(true);

            theCurrentPage = (HtmlPage) button.click();
        }// End - Login Page
        // ------------------------------------------------------------------
        {// SIMULATOR CENTER Page
            HtmlForm theForm = theCurrentPage.getFormByName("Form1");
            HtmlSubmitInput button = (HtmlSubmitInput) theForm
                    .getInputByName("btnEnterSim");

            theCurrentPage = (HtmlPage) button.click();
        }// End - SIMULATOR CENTER Page
        // ------------------------------------------------------------------
        {// SIMULATOR SETUP Page
            HtmlForm form = theCurrentPage.getFormByName("Form1");
            HtmlSelect theSimulationSelect = form
                    .getSelectByName("ddlSimulations");

            // Get all of the available meeting rooms
            for (Object anObject : theSimulationSelect.getOptions()) {
                HtmlOption theOption = (HtmlOption) anObject;
                if (!theOption.asText().equals("-Select a Meeting-")) {
                    theMeetingNames.put(theOption.asText(), theOption
                            .getValueAttribute());
                }
            }
        }// End - SIMULATOR SETUP Page
        // ------------------------------------------------------------------
        theLastTurnTime = new Date();
    }

    /**
     */
    public void joinMeeting(String aMeetingName) throws Exception {
	//System.out.println("@@@@@@@ Joining meeting "+aMeetingName);
        theMeeting = aMeetingName;

        // Start monitoring the simulation
        theAdapter = new WebWindowAdapter() {
            public void webWindowContentChanged(WebWindowEvent event) {
		//System.out.println("@@@@@ Web window content changed!");
                // Get the Current Page - incase of redirects
                theCurrentPage = (HtmlPage) theWebClient.getCurrentWindow()
                        .getEnclosedPage();

                // Is this a SCCORI server Error page?
                if (theCurrentPage.getTitleText().equals("Runtime Error")) {
                    // Refresh the page
		    System.out.println("Runtime error detected, reloading");
                    goToSimPage();
                    return;
                }

                // Only follow through if the simulation has started
		String curPagTex = theCurrentPage.asText();
                if (!curPagTex.equals("SCCORI Simulator")) {
                    // Refresh the page
		    System.out.println(curPagTex+": Not the desired page");
                    //goToSimPage();
                    return;
                }

                try {
                    processTurn();
                } catch (ElementNotFoundException enfE) {
                    if (theCurrentPage.getTitleText().equals("SimResults")) {
			System.out.println("Sim done!");
                        unHookAgent();
                    } else {
			enfE.printStackTrace();
        	    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FailingHttpStatusCodeException failException) {
                    goToSimPage();
                } catch (Exception exc) {
		    exc.printStackTrace();
		}

            }
        };
        theWebClient.addWebWindowListener(theAdapter);

        // Enter the simulation
        enterMeeting(aMeetingName);
    }

    private void unHookAgent() {
        // This occurs when the simulation has ended.
        // Remove this listener
        theWebClient.removeWebWindowListener(theAdapter);

        // Notify all listners
        for (TurnListener theAction : theMyTurnListeners) {
            theAction.simulationHasEnded();
        }

        // Remove all listeners
        theMyTurnListeners.clear();
    }

    /**
     * This function is called every time the simulation page refreshes. It is
     * also called when the game ends, but will throw a
     * ElementNotFoundException. This exception will also be thrown should the
     * SCCORI server crash (which happens sometimes).
     * 
     * @throws ElementNotFoundException
     * @throws IOException
     */
    private void processTurn() throws ElementNotFoundException, IOException {
	//System.out.println("@@@@@@@@ Processing turn!!!!!!");
        FrameWindow testFrame = theCurrentPage.getFrameByName("SimulatorFrame");
        HtmlPage theFramePage = (HtmlPage) testFrame.getEnclosedPage();
	//System.out.println(theFramePage.asXml());
        HtmlForm theForm = theFramePage.getFormByName("Form1");
        HtmlTextInput theOrderTextField = (HtmlTextInput) theForm
                .getInputByName("txtOrder1");

        if (!theOrderTextField.isDisabled()) {
            // Get this turns information
            //System.out.println("It is our turn to play");
            TurnEvent theEvent = getTurnEvent(theFramePage);

            // Check if this turn has been played
            TurnEvent thePreviousEvent = theSimulationInfo
                    .getCurrentTurnEvent();
            if (thePreviousEvent != null
                    && thePreviousEvent.getCycleNumber() == theEvent
                            .getCycleNumber()) {
                // Wake up all of the listeners...
                for (TurnListener theAction : theMyTurnListeners) {
                    theAction.orderInvalid(thePreviousEvent);
                }
                theEvent = thePreviousEvent;
            } else {
                // Add it to the simualtion info
                theSimulationInfo.addTurnEvent(theEvent);

                // Wake up all of the listeners...
                for (TurnListener theAction : theMyTurnListeners) {
                    theAction.executeTurn(theEvent);
                }
            }

            // Place the order
            theOrderTextField.setValueAttribute(""
                    + theEvent.getAmountToOrder());
            theForm.getInputByName("btnOrder").click();
            theLastEvent = theEvent;
        } else {
            //System.out.println("I will pass");
            // Not his turn, so do nothing....
        }
    }

    private void goToSimPage() {
        try {
            theCurrentPage = (HtmlPage) theWebClient
                    .getPage(theSimulationViewURL);
            // processTurn();
        } catch (IOException ioExp) {
            // ioExp.printStackTrace();
            theLog.error(ioExp);
            theLog.info("Lost Network Connection to SCCORI site - "
                    + "I will try again");
            // Let's be nice and give the SCCORI server 2 seconds to come back
            // up before we start hammering it....
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            goToSimPage();
        }
    }

    private TurnEvent getTurnEvent(HtmlPage aFramePage) {
        // Get the cycle information, and create a new TurnEvent
        TurnEvent theTurnEvent = new TurnEvent(Integer.valueOf(
                aFramePage.getHtmlElementById("lblCycle").asText()).intValue());

        // Get info table
        // Turn Role Order Received Order Placed LTD1 LTD2 LTD3 On Hand Ship.
        // Mode Unit Cost Cycle Cost Cycle Details Total Cost
        HtmlTable theTable = (HtmlTable) aFramePage
                .getHtmlElementById("dgSimulator");

        // Retailer info
        theTurnEvent.setRetailerInfo(parseInformation(theTable.getRow(1)));

        // Wholesaler info
        theTurnEvent.setWholesalerInfo(parseInformation(theTable.getRow(2)));

        // Distributor info
        theTurnEvent.setDistributorInfo(parseInformation(theTable.getRow(3)));

        // Manufacturer info
        theTurnEvent.setManufacturerInfo(parseInformation(theTable.getRow(4)));

        return theTurnEvent;
    }

    private TurnInfo parseInformation(HtmlTableRow aRow) {
        TurnInfo theTurnInfo = new TurnInfo();

        theTurnInfo.setOrderReceived(Integer.valueOf(aRow.getCell(2).asText())
                .intValue());

        theTurnInfo.setOrderPlaced(Integer.valueOf(aRow.getCell(3).asText())
                .intValue());

        theTurnInfo
                .setLD1(Integer.valueOf(aRow.getCell(4).asText()).intValue());

        theTurnInfo
                .setLD2(Integer.valueOf(aRow.getCell(5).asText()).intValue());

        theTurnInfo
                .setLD3(Integer.valueOf(aRow.getCell(6).asText()).intValue());

        theTurnInfo.setStockOnHand(Integer.valueOf(aRow.getCell(7).asText())
                .intValue());

        String value = aRow.getCell(9).asText().replace('$', ' ').replaceAll(
                ",", "");
        theTurnInfo.setUnitCost(Double.valueOf(value).doubleValue());

        value = aRow.getCell(10).asText().replace('$', ' ').replaceAll(",", "");
        theTurnInfo.setCycleCost(Double.valueOf(value).doubleValue());

        value = aRow.getCell(12).asText().replace('$', ' ').replaceAll(",", "");
        theTurnInfo.setTotalCost(Double.valueOf(value).doubleValue());

        return theTurnInfo;
    }

    private void enterMeeting(String aMeetingName) throws Exception {
        HtmlForm form = theCurrentPage.getFormByName("Form1");
        HtmlSelect theSimulationSelect = form.getSelectByName("ddlSimulations");
	theSimulationSelect.focus();
        theSimulationSelect.setSelectedAttribute(theMeetingNames.get(aMeetingName), true);
	theSimulationSelect.click();
	theCurrentPage = (HtmlPage) (form.submit()); //Ugly hack: it should be done with Javascript but the focus(), setSelectedAttribute(), and click() above don't seem to trigger the event...
	//System.out.println(theCurrentPage.asXml());

        // Set the simulation ID to the room ID
        theSimulationInfo.setSimulationId(Integer.parseInt(theMeetingNames
                .get(aMeetingName)));

        // Page refreshes after selecting the meeting, so get the form again...
        form = theCurrentPage.getFormByName("Form1");

        HtmlSubmitInput button = (HtmlSubmitInput) form
                .getInputByName("btnEnterSim");

        // -- Bot's role
        theSimulationInfo.setAgentRole(form.getHtmlElementById("lblUserRole")
                .asText());

        // -- Initial Cost
	try {
            theSimulationInfo.setInitialCost(Double.valueOf(
                form.getInputByName("txtItemCost").getValueAttribute())
                .doubleValue());
	} catch (NumberFormatException e) {
	    theSimulationInfo.setInitialCost(0.5);
	}
        // -- Handling Cost
	try {
            theSimulationInfo.setHandlingCost(Double.valueOf(
                form.getInputByName("txtHandlingCost").getValueAttribute())
                .doubleValue());
	} catch (NumberFormatException e) {
	    theSimulationInfo.setHandlingCost(0.03);
	}
        // -- BackLog Cost
	try {
            theSimulationInfo.setBackLogCost(Double.valueOf(
                form.getInputByName("txtBackLogCost").getValueAttribute())
                .doubleValue());
	} catch (NumberFormatException e) {
	    theSimulationInfo.setBackLogCost(1);
	}
        // -- Current Inventory
	try {
            theSimulationInfo.setCurrentInventory(Integer
                .valueOf(
                        form.getInputByName("txtStartingInventory")
                                .getValueAttribute()).intValue());
	} catch (NumberFormatException e) {
	    theSimulationInfo.setCurrentInventory(80);
	}
        // -- Max Inventory
        try {
            theSimulationInfo.setMaxInventory(Integer.valueOf(
                form.getInputByName("txtMaxInventory").getValueAttribute())
                .intValue());
	} catch (NumberFormatException e) {
	    theSimulationInfo.setMaxInventory(100);
	}
        // -- Safety Stock
	try {
            theSimulationInfo.setSafetyStock(Integer.valueOf(
                form.getInputByName("txtSafetyStock").getValueAttribute())
                .intValue());
	} catch (NumberFormatException e) {
		theSimulationInfo.setSafetyStock(40);
	}

        theCurrentPage = (HtmlPage) button.click();
    }

    private static void testFourBots(final String aRoomName, final int aTime) {
        try {
            final long theStartTime = System.currentTimeMillis();

            // Bot 1
            Simulation theSimulation1 = new Simulation("BOT1_COMP667", "qwerty");
            theSimulation1.joinMeeting(aRoomName);
            theSimulation1.setAgent(new ParanoidAgent(theSimulation1
                    .getSimulationInfo()));

            // Bot 2
            Simulation theSimulation2 = new Simulation("BOT2_COMP667", "qwerty");
            theSimulation2.joinMeeting(aRoomName);
            theSimulation2.setAgent(new ParanoidAgent(theSimulation2
                    .getSimulationInfo()));

            // Bot 3
            Simulation theSimulation3 = new Simulation("BOT3_COMP667", "qwerty");
            theSimulation3.joinMeeting(aRoomName);
            theSimulation3.setAgent(new ParanoidAgent(theSimulation3
                    .getSimulationInfo()));

            // Bot 4
            final Simulation theSimulation4 = new Simulation("BOT4_COMP667",
                    "qwerty");
            theSimulation4.joinMeeting(aRoomName);
            theSimulation4.setAgent(new ParanoidAgent(theSimulation4
                    .getSimulationInfo()));

            theSimulation4.addMyTurnListener(new TurnAdapter() {
                public void simulationHasEnded() {
                    theLog.info("Simulation - "
                            + aRoomName
                            + " - "
                            + theSimulation4.getSimulationInfo()
                                    .getSimulationId() + " took "
                            + (System.currentTimeMillis() - theStartTime)
                            / 60000 + " minutes.");

                    if (aTime > 1) {
                        testFourBots(aRoomName, aTime - 1);
                    }
                }
            });

            theLog.info("Simulation - " + aRoomName + " -"
                    + theSimulation1.getSimulationInfo().getSimulationId()
                    + " has just started with 4 bots. ");

        } catch (Exception e) {
            theLog.info("Initialization problems for room " + aRoomName);
            theLog.error(e);
        }
    }

    /**
     * Test wrapper.
     * 
     * @param args
     */
    public static void main(String[] args) {
        // testFourBots("BOT_ROOM3_PUSH_SPIKED", 1000);
        // testFourBots("BOT_ROOM4_PUSH_RANDOM", 1000);
        testFourBots("BOT_ROOM5_PULL_RANDOM", 1000);
        // testFourBots("BOT_ROOM6_PULL_SPIKED", 1000);
    }
}
