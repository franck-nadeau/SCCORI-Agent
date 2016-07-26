package ca.athabascau.sccori.message;

import java.util.HashMap;

import ca.athabascau.sccori.comm.Simulation;
import ca.athabascau.sccori.comm.TurnAdapter;
import ca.athabascau.sccori.data.SimulationInfo;

/**
 * 
 * 
 */
public class MessageChannel {
    private static HashMap<SimulationInfo, MessageChannel> theMessageChannels =
            new HashMap<SimulationInfo, MessageChannel>();

    private final int theSourceAgent;

    private final SimulationInfo theSimulationInfo;

    /**
     * 
     * @param aSourceAgent
     */
    private MessageChannel(Simulation aSimulation) {
        theSourceAgent = aSimulation.getSimulationInfo().getAgentRole();
        theSimulationInfo = aSimulation.getSimulationInfo();

        theMessageChannels.put(aSimulation.getSimulationInfo(), this);

        // Add a listener to remove this Channel once the simulation has
        // completed
        aSimulation.addMyTurnListener(new TurnAdapter() {
            @Override
            public void simulationHasEnded() {
                theMessageChannels.remove(theSimulationInfo);
            }
        });
    }

    /**
     * <p>
     * Does ...
     * </p>
     * 
     * 
     * @param aSimulationId
     * @return
     */
    public static MessageChannel getMessageChannel(Simulation aSimulation) {
        // Check if this MessageChannel already exits
        MessageChannel theChannel = theMessageChannels.get(aSimulation
                .getSimulationInfo());
        if (theChannel != null) {
            // This message channel already exits, so return it
            return theChannel;
        } else {
            // This message channel does not exits, so create one...
            theChannel = new MessageChannel(aSimulation);
            // and return it by testing that it was properly added...
            return getMessageChannel(aSimulation);
        }
    }

    /**
     * <p>
     * Does ...
     * </p>
     * 
     * 
     * @param aMessage
     */
    public void postMessage(Message aMessage) {
        // Set the source of this message
        aMessage.setSourceAgent(theSourceAgent);

        // Look for the agent that this message is destined for
        for (SimulationInfo aSimulationInfo : theMessageChannels.keySet()) {
            if (aSimulationInfo.getSimulationId() == theSimulationInfo
                    .getSimulationId()
                    && aSimulationInfo.getAgentRole() == aMessage
                            .getTargetAgent()) {
                aSimulationInfo.addMessage(aMessage);
                return;
            }
        }

        // If code get here then the message is lost.
    }
}