package ca.athabascau.sccori.message;

/**
 * 
 * 
 */
public class Message {
    /** Represents a message that states how many items they will purchase */
    public static int AMOUNT_I_WILL_BUY_NEXT_TURN = 50;

    /**
     * Represents a message asking an other agent to buy a number of items by a
     * certain turn
     */
    public static int AMOUNT_YOU_SHOULD_BUY_NEXT_TURN = 51;

    private final int theTargetAgent;

    private int theSourceAgent;

    private final int theMessageType;

    private final int theValue;

    /**
     * <p>
     * Construct a message to be sent to an other agent.
     * </p>
     * 
     * @param aTargetAgent
     * @param aMessageType
     */
    public Message(int aTargetAgent, int aMessageType, int aValue) {
        // your code here
        theTargetAgent = aTargetAgent;
        theMessageType = aMessageType;
        theValue = aValue;
    }

    /**
     * This function will be used by the MessageChannel class to set the agent
     * who is sending the message.
     * <P>
     * This function is restricted to the default level on purpose to only allow
     * the MessageChannel class rights to modify this value.
     * 
     * @param aSource
     */
    void setSourceAgent(int aSource) {
        theSourceAgent = aSource;
    }

    /**
     * 
     * @return
     */
    public int getTargetAgent() {
        return theTargetAgent;
    }

    /**
     * <p>
     * Does ...
     * </p>
     * 
     * @return
     */
    public int getSourceAgent() {
        return theSourceAgent;
    }

    public int getValue() {
        return theValue;
    }

    /**
     * <p>
     * Does ...
     * </p>
     * 
     */
    public int getMessageType() {
        return theMessageType;
    }
}
