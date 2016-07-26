package ca.athabascau.sccori.data;

import java.util.LinkedList;

import ca.athabascau.sccori.message.Message;

/**
 * <P>
 * This is a container object representing a simulation, and all of the event
 * information that the agent has been shown so far.
 */
public class SimulationInfo {
    /** Represents the Retailer player */
    public static final int RETAILER = 0;

    /** Represents the Wholesaler player */
    public static final int WHOLESALER = 1;

    /** Represents the Distributor player */
    public static final int DISTRIBUTOR = 2;

    /** Represents the Manufacturer player */
    public static final int MANUFACTURER = 3;

    /** The simulation id used by the SCCORI site */
    private int theSimulationId;

    /** The role of the player for with this SimulationInfo is for */
    private int theAgentRole = -1;

    /** */
    private double theInitialCost;

    /** */
    private double theHandlingCost;

    /** */
    private double theBackLogCost;

    /** */
    private int theCurrentInventory;

    /** */
    private int theSafetyStock;

    /** */
    private int theMaxInventory;

    /** */
    private LinkedList<TurnEvent> theTurnEvents = new LinkedList<TurnEvent>();

    /** */
    private LinkedList<Message> theMessages = new LinkedList<Message>();

    /**
     * Copy the values from a Simulation Info into this Simulation info.
     * 
     * @param aSimulationInfo
     *            the information to be used
     */
    public void copyValueFrom(SimulationInfo aSimulationInfo) {
        theSimulationId = aSimulationInfo.theSimulationId;
        theAgentRole = aSimulationInfo.theAgentRole;
        theInitialCost = aSimulationInfo.theInitialCost;
        theHandlingCost = aSimulationInfo.theHandlingCost;
        theBackLogCost = aSimulationInfo.theBackLogCost;
        theCurrentInventory = aSimulationInfo.theCurrentInventory;
        theSafetyStock = aSimulationInfo.theSafetyStock;
        theMaxInventory = aSimulationInfo.theMaxInventory;
    }

    /**
     * Add a message to this Simulation Info. This message is comming from an
     * other agent, and is added by the Facilator class MessageChannel.
     * 
     * @see ca.athabascau.sccori.message.MessageChannel
     */
    public void addMessage(Message aMessage) {
        theMessages.add(aMessage);
    }

    /**
     * Pop the next message which was recieved from the message stack. This
     * follows a FIFO sequence.
     * 
     * @return the next message, or null if there are no more messages.
     */
    public Message nextMessage() {
        if (theMessages.size() != 0) {
            return theMessages.remove(0);
        } else {
            return null;
        }
    }

    /**
     * Retrieve the Simulation id for this simulation. This value is assigned by
     * the SCCORI web site.
     * 
     * @return the Simulation ID
     */
    public int getSimulationId() {
        return theSimulationId;
    }

    public void setSimulationId(int anId) {
        theSimulationId = anId;
    }

    /**
     * Retrieve the agent role for this Simulation Information. This is the role
     * of the agent which is using a Simulation to interface with a SCCORI
     * simulation.
     * 
     * @return the agent role
     */
    public int getAgentRole() {
        return theAgentRole;
    }

    public void setAgentRole(int theAgentRole) {
        this.theAgentRole = theAgentRole;
    }

    /**
     * Set the agent role.
     * 
     * @param anAgentRole
     *            a string representing the agent role as displayed on the
     *            SCCORI web site.
     */
    public void setAgentRole(String anAgentRole) {
        if (anAgentRole.equals("Retailer")) {
            setAgentRole(RETAILER);
        }
        if (anAgentRole.equals("Wholesaler")) {
            setAgentRole(WHOLESALER);
        }
        if (anAgentRole.equals("Distributor")) {
            setAgentRole(DISTRIBUTOR);
        }
        if (anAgentRole.equals("Manufacturer")) {
            setAgentRole(MANUFACTURER);
        }
    }

    /**
     * <P>
     * Retrieve the cost of back logs in the simulation.
     * <P>
     * The default SCCORI value for this is $1.
     * 
     * @return the cost of back loged items
     */
    public double getBackLogCost() {
        return theBackLogCost;
    }

    public void setBackLogCost(double theBackLogCost) {
        this.theBackLogCost = theBackLogCost;
    }

    /**
     * Get the current cycle number in the SCCORI simulation.
     * 
     * @return the current cycle number in the SCCORI simualtion.
     */
    public int getCurrentCycleNumber() {
        if(theTurnEvents.size() == 0) {
            return 0;
        }else {
            return theTurnEvents.getLast().getCycleNumber();
        }
    }

    /**
     * <P>
     * Retrieve the starting inventory.
     * <P>
     * The default SCCORI value for this is 40 items.
     * 
     * @param theCurrentInventory
     *            the inventory at the begining of the game
     */
    public int getCurrentInventory() {
        return theCurrentInventory;
    }

    public void setCurrentInventory(int theCurrentInventory) {
        this.theCurrentInventory = theCurrentInventory;
    }

    /**
     * Retrieve the cost of handling.
     * 
     * @return the handling cost
     */
    public double getHandlingCost() {
        return theHandlingCost;
    }

    public void setHandlingCost(double theHandlingCost) {
        this.theHandlingCost = theHandlingCost;
    }

    /**
     * Retrieve the cost of the items.
     * 
     * @return the cost of items
     */
    public double getInitialCost() {
        return theInitialCost;
    }

    public void setInitialCost(double theInitialCost) {
        this.theInitialCost = theInitialCost;
    }

    /**
     * <P>
     * Retrieve the maximum inventory allowed by the simualtion.
     * <P>
     * The default SCCORI value for this is 100.
     * 
     * @return the maximum-stock on hand allowed
     */
    public int getMaxInventory() {
        return theMaxInventory;
    }

    public void setMaxInventory(int theMaxInventory) {
        this.theMaxInventory = theMaxInventory;
    }

    /**
     * <P>
     * Retrieve the default number of items that each player should keep in
     * their inventory.
     * <P>
     * The default SCCORI value for this is 40.
     * 
     * @return the safety stock level for warning purposes
     */
    public int getSafetyStock() {
        return theSafetyStock;
    }

    public void setSafetyStock(int theSafetyStock) {
        this.theSafetyStock = theSafetyStock;
    }

    /**
     * Add a turn event to this information. TurnEvent represent a players turn.
     * i.e. what ordered, and all the information available to them when their
     * turn occured.
     * 
     * @param aTurnEvent
     *            the latest TurnEvent
     */
    public void addTurnEvent(TurnEvent aTurnEvent) {
        theTurnEvents.add(aTurnEvent);
    }

    /**
     * Retrieve the next TurnEvent. This TurnEvent may or may not have been
     * executed. That is, its order may not have been placed yet.
     * 
     * @return the last event to be added on the TurnEvent list.
     */
    public TurnEvent getCurrentTurnEvent() {
        if (theTurnEvents.isEmpty()) {
            return null;
        } else {
            return theTurnEvents.getLast();
        }
    }

    /**
     * <P>
     * Retrieve the TurnInformation belonging to the agent represented by this
     * Information container.
     * 
     * @return the TurnInfo for the agent owning this information container
     */
    public TurnInfo getMyCurrentTurnInfo() {
        switch (getAgentRole()) {
        case RETAILER:
            return getCurrentTurnEvent().getRetailerInfo();
        case WHOLESALER:
            return getCurrentTurnEvent().getWholesalerInfo();
        case DISTRIBUTOR:
            return getCurrentTurnEvent().getDistributorInfo();
        case MANUFACTURER:
            return getCurrentTurnEvent().getManufacturerInfo();
        default:
            return null;
        }
    }

    /**
     * SimualtionInfo are equal if they are for the same SCCORI simulation, and
     * the same agent role.
     * 
     * @param o
     *            the SimulationInfo to be compared.
     * @return true if same SCCORI simulation and same agent role
     */
    public boolean equals(Object o) {
        if (!(o instanceof SimulationInfo)) {
            return false;
        }

        SimulationInfo theSimulationInfo = (SimulationInfo) o;

        // Equal if same agent for the same simulation
        return theSimulationInfo.getAgentRole() == getAgentRole()
                && theSimulationInfo.getSimulationId() == getSimulationId();
    }

    /**
     * 
     * 
     * @param o
     *            the SimulationInfo to be compared
     * @return the difference between the two SCCORI simulation ids.
     */
    public int compareTo(Object o) {
        if (!(o instanceof SimulationInfo)) {
            return -99999;
        }

        SimulationInfo theSimulationInfo = (SimulationInfo) o;

        return theSimulationInfo.getSimulationId() - getSimulationId();
    }
}