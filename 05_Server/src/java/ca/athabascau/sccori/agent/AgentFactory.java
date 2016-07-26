package ca.athabascau.sccori.agent;

import ca.athabascau.sccori.comm.Simulation;
import ca.athabascau.sccori.comm.TurnListener;

/**
 * 
 * 
 * @author Francois Nadeau
 */
public class AgentFactory {
    /**
     * Retrieve the available agents for each possible simulations.
     * 
     * @param aSimulationScenario
     * @return
     */
    public static final String[] getAvailableAgents() {
        return new String[] { ParanoidAgent.DISPLAY_NAME,
                ConsiderateParanoidAgent.DISPLAY_NAME,
                RulesBasedAgent.DISPLAY_NAME, RB_PushAgent.DISPLAY_NAME,
                RB_PullRandomAgent.DISPLAY_NAME, 
                RB_PullSpikeAgent.DISPLAY_NAME };
    }

    /**
     * Retrieve and register an agent with a simulation.
     * 
     * @param anAgentName
     * @param aSimulation
     * @return
     */
    public static TurnListener getAgent(String anAgentName,
            Simulation aSimulation) {
        if (anAgentName.equals(ParanoidAgent.DISPLAY_NAME)) {
            return new ParanoidAgent(aSimulation.getSimulationInfo());
        }
        
        if (anAgentName.equals(ConsiderateParanoidAgent.DISPLAY_NAME)) {
            return new ConsiderateParanoidAgent(aSimulation.getSimulationInfo());
        }
        
        if (anAgentName.equals(RulesBasedAgent.DISPLAY_NAME)) {
            return new RulesBasedAgent(aSimulation.getSimulationInfo());
        }
        
        if (anAgentName.equals(RB_PushAgent.DISPLAY_NAME)) {
            return new RB_PushAgent(aSimulation.getSimulationInfo());
        }
        
        if (anAgentName.equals(RB_PullRandomAgent.DISPLAY_NAME)) {
            return new RB_PullRandomAgent(aSimulation.getSimulationInfo());
        }
        
        if (anAgentName.equals(RB_PullSpikeAgent.DISPLAY_NAME)) {
            return new RB_PullSpikeAgent(aSimulation.getSimulationInfo());
        }

        return new RB_PushAgent(aSimulation.getSimulationInfo());
    }
}
