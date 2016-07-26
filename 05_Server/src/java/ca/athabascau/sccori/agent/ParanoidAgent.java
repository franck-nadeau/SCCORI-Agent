package ca.athabascau.sccori.agent;

import ca.athabascau.sccori.data.*;
import ca.athabascau.sccori.comm.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the Paranoid agent. The Paranoid Agent is a purely reactive agent.
 * Each turn, this agent will order the amount of items which is missing from
 * its On Hand inventory in order to have it 100% full.
 * 
 * @see http://www.sccori.francoisnadeau.com/index.php/Paranoid_Agent
 */
public class ParanoidAgent implements TurnListener {
	private static final Log theLog = LogFactory.getLog(ParanoidAgent.class);

	public final static String DISPLAY_NAME = "Paranoid Agent";

	private final SimulationInfo theSimulationInfo;

	/**
	 * Construct a Paranoid agent which will use the SimulationInfo given to it
	 * as a parameter.
	 * 
	 * @param aSimulationInfo
	 *			  the Information used to decide how much to order
	 */
	public ParanoidAgent(SimulationInfo aSimulationInfo) {
		theSimulationInfo = aSimulationInfo;
	}

	public void executeTurn(TurnEvent e) {
		TurnInfo theTurnInfo = theSimulationInfo.getMyCurrentTurnInfo();

		// Order = Sim.invMax - e.my.OH - e.my.LTD1 - e.my.LTD2 - e.my.LTD3
		int theOrder = theSimulationInfo.getMaxInventory()
				- theTurnInfo.getStockOnHand() - theTurnInfo.getLD1()
				- theTurnInfo.getLD2() - theTurnInfo.getLD3() - 1;

		if (theOrder < 0)
			theOrder = 0;

		e.setAmountToOrder(theOrder);
	}

	public void orderInvalid(TurnEvent e) {
		TurnInfo theTurnInfo = theSimulationInfo.getMyCurrentTurnInfo();
		int orderReceived = theTurnInfo.getOrderReceived();

		if (e.getAmountToOrder() <= orderReceived) {
			e.setAmountToOrder(0);
		} else {
			e.setAmountToOrder(orderReceived);
		}

		theLog.info("orderInvalid=" + e.getAmountToOrder());
	}

	public void simulationHasEnded() {
	}

	public String toString() {
		return DISPLAY_NAME;
	}

	private static void testFourBots(final String aRoomName, final int aTime) {
		try {
			final long theStartTime = System.currentTimeMillis();

			// Bot 1
			Simulation theSimulation1 = new Simulation("BOT1_COMP667","qwerty");
			theSimulation1.joinMeeting(aRoomName);
			theSimulation1.setAgent(new ParanoidAgent(theSimulation1
					.getSimulationInfo()));

			// Bot 2
			Simulation theSimulation2 = new Simulation("BOT2_COMP667","qwerty");
			theSimulation2.joinMeeting(aRoomName);
			theSimulation2.setAgent(new ParanoidAgent(theSimulation2
					.getSimulationInfo()));

			// Bot 3
			Simulation theSimulation3 = new Simulation("BOT3_COMP667","qwerty");
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
		// testFourBots("BOT_ROOM3_PUSH_SPIKED", 1);
		// testFourBots("BOT_ROOM4_PUSH_RANDOM", 1000);
		// testFourBots("BOT_ROOM5_PULL_RANDOM", 30);
		// testFourBots("BOT_ROOM6_PULL_SPIKED", 1);
		// testFourBots("BOT_ROOM7_PULL_RANDOM", 10);

	}
}
