/*
 * ConsiderateParanoidAgent.java
 *
 * Created on July 4, 2006, 6:38 PM
 * Created by MaRinus
 *
 */

package ca.athabascau.sccori.agent;

import ca.athabascau.sccori.data.*;
import ca.athabascau.sccori.comm.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author MaRinus
 */
public class ConsiderateParanoidAgent implements TurnListener {
	private static final Log theLog = LogFactory
			.getLog(ConsiderateParanoidAgent.class);

	public final static String DISPLAY_NAME = "Considerate Paranoid Agent";

	private final SimulationInfo theSimulationInfo;

	/**
	 * Construct a Considerate Paranoid agent which will use the SimulationInfo
	 * given to it as a parameter.
	 * 
	 * @param aSimulationInfo -
	 *			  the Information used to decide how much to order
	 */
	public ConsiderateParanoidAgent(SimulationInfo aSimulationInfo) {
		theSimulationInfo = aSimulationInfo;
	}

	public void executeTurn(TurnEvent e) {
		TurnInfo theTurnInfo = theSimulationInfo.getMyCurrentTurnInfo();
		TurnInfo supplierTurnInfo;

		int myRole = theSimulationInfo.getAgentRole();

		switch (myRole) {
		case SimulationInfo.RETAILER:
			supplierTurnInfo = theSimulationInfo.getCurrentTurnEvent()
					.getWholesalerInfo();
			break;
		case SimulationInfo.WHOLESALER:
			supplierTurnInfo = theSimulationInfo.getCurrentTurnEvent()
					.getDistributorInfo();
			break;
		case SimulationInfo.DISTRIBUTOR:
			supplierTurnInfo = theSimulationInfo.getCurrentTurnEvent()
					.getManufacturerInfo();
			break;
		default:
		// In order to keep the compiler happy, we MUST always initialize.
		// So just drop through to the MANUFACTURER case.
		case SimulationInfo.MANUFACTURER:
			// No factory information available, so just get own information
			supplierTurnInfo = theSimulationInfo.getCurrentTurnEvent()
					.getManufacturerInfo();
			break;
		}

		// Order = Sim.invMax - e.my.OH - e.my.LTD1 - e.my.LTD2 - e.my.LTD3
		int theOrder = theSimulationInfo.getMaxInventory()
				- theTurnInfo.getStockOnHand() - theTurnInfo.getLD1()
				- theTurnInfo.getLD2() - theTurnInfo.getLD3();

		// supplierStock = supplierSOH + supplierLD1 + supplierLD2 + supplierLD3
		int supplierTotalStock = supplierTurnInfo.getStockOnHand()
				+ supplierTurnInfo.getLD1() + supplierTurnInfo.getLD2()
				+ supplierTurnInfo.getLD3();

		// Be considerate of the supplier
		if (myRole == SimulationInfo.MANUFACTURER) {
			// no change required, the factory cannot be backlogged
		} else { // consideration decision
			if (supplierTotalStock >= theSimulationInfo.getSafetyStock()) {
				theOrder = Math
						.min(theOrder, supplierTurnInfo.getStockOnHand());
			}
		}

		// Do not order negative quantities
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
			theSimulation1.setAgent(new ConsiderateParanoidAgent(theSimulation1
					.getSimulationInfo()));

			// Bot 2
			Simulation theSimulation2 = new Simulation("BOT2_COMP667","qwerty");
			theSimulation2.joinMeeting(aRoomName);
			theSimulation2.setAgent(new ConsiderateParanoidAgent(theSimulation2
					.getSimulationInfo()));

			// Bot 3
			Simulation theSimulation3 = new Simulation("BOT3_COMP667","qwerty");
			theSimulation3.joinMeeting(aRoomName);
			theSimulation3.setAgent(new ConsiderateParanoidAgent(theSimulation3
					.getSimulationInfo()));

			// Bot 4
			final Simulation theSimulation4 = new Simulation("BOT4_COMP667",
					"qwerty");
			theSimulation4.joinMeeting(aRoomName);
			theSimulation4.setAgent(new ConsiderateParanoidAgent(theSimulation4
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
		// testFourBots("BOT_ROOM5_PULL_RANDOM", 30);
		// testFourBots("BOT_ROOM6_PULL_SPIKED", 1);
	}
}
