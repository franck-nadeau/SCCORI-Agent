/*
 * RB_PullSpikeAgent.java
 * This agent does its best to minimize "overall" unit costs for spiked runs in
 * the SCCORI "pull" type of simulation.
 * It is not that as good at random runs as the RulesBasedAgent.
 *
 * See PullRandomAgent for a specialized RulesBased agents that is designed
 * to do better in a random run SCCORI simulation environment.
 *
 * See PushAgent for a Rules Based agent agent that does its best to minimize
 * its own costs in a SCCORI "push" type of simulation.
 *
 * Created on July 8, 2006.
 * Created by MaRinus
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
public class RB_PullSpikeAgent implements TurnListener {
	private static final Log theLog = LogFactory
			.getLog(RB_PullSpikeAgent.class);

	public final static String DISPLAY_NAME = "Rule Base Agent (Pull Spike)";
	private final SimulationInfo theSimulationInfo;

	/*
	 * The following parameters assume the simulation uses a 20 ft truck, which
	 * can hold 60 units. To minimize shipping costs, the minimum order is
	 * one-half of that (30 units).
	 * 
	 * In order to respond well to spiked runs, the agent attempts to maintain
	 * stock at maximum levels. This is a compromise for non-spiked runs where
	 * the agent could do about 5% better by maintaining lower levels. It is
	 * also a compromise for spiked runs where the agent could do about 2%
	 * better by maximizing stock before the spike.
	 * 
	 */

	private final static int RETAILER_STOCK_LEVEL = 100;
	private final static int WHOLESALER_STOCK_LEVEL = 80;
	private final static int DISTRIBUTOR_STOCK_LEVEL = 80;
	private final static int MANUFACTURER_STOCK_LEVEL = 65;
	private static boolean isSpiked = false;
	private final int safetyLevel = 40; // Must order if supplier can deliver.
	private final int containerSize = 60; // Nmbr of units container can hold.
	private final int minOrder = 30; // Container must be at least half full.

	/**
	 * Construct a Rules Based agent which will use the SimulationInfo given to
	 * it as a parameter.
	 * 
	 * @param aSimulationInfo -
	 *			  the Information used to decide how much to order
	 */
	public RB_PullSpikeAgent(SimulationInfo aSimulationInfo) {
		theSimulationInfo = aSimulationInfo;
	}

	public void executeTurn(TurnEvent e) {
		TurnInfo theTurnInfo = theSimulationInfo.getMyCurrentTurnInfo();
		TurnInfo supplierTurnInfo; // What can the supplier deliver?
		boolean isRetailerBacklogged = false; // backlogged retailers; bad news
		int stockingLevel; // Each position has a different level.

		int myRole = theSimulationInfo.getAgentRole();

		switch (myRole) {
		case SimulationInfo.RETAILER:
			supplierTurnInfo = theSimulationInfo.getCurrentTurnEvent()
					.getWholesalerInfo();
			stockingLevel = RETAILER_STOCK_LEVEL;
			break;
		case SimulationInfo.WHOLESALER:
			supplierTurnInfo = theSimulationInfo.getCurrentTurnEvent()
					.getDistributorInfo();
			stockingLevel = WHOLESALER_STOCK_LEVEL;
			break;
		case SimulationInfo.DISTRIBUTOR:
			supplierTurnInfo = theSimulationInfo.getCurrentTurnEvent()
					.getManufacturerInfo();
			stockingLevel = DISTRIBUTOR_STOCK_LEVEL;
			break;
		default:
		// In order to keep the compiler happy, we must ALWAYS initialize.
		// So just drop through to the MANUFACTURER case.
		case SimulationInfo.MANUFACTURER:
			// No factory information available, so just get own information
			supplierTurnInfo = theSimulationInfo.getCurrentTurnEvent()
					.getManufacturerInfo();
			stockingLevel = MANUFACTURER_STOCK_LEVEL;
			break;
		}

		// Is the supplier trying to maintain adequate levels?
		// supplierStock = supplierSOH + supplierLD1 + supplierLD2 + supplierLD3
		int supplierTotalStock = supplierTurnInfo.getStockOnHand()
														+ supplierTurnInfo.getLD1()
														+ supplierTurnInfo.getLD2()
														+ supplierTurnInfo.getLD3();

		/***********************************************************************
		 * ====================================================================
		 * The following logic shuns "else" clauses in order to easily match it *
		 * to the antecedent/consequent statements used by Rules-Based agents. *
		 * ====================================================================
		 */
		// Decisions change if the Retailer is badly backlogged. In that case
		// all must strive to maximize their stocks in order to clear the
		// backlog ASAP. Recovering from -15 or worse is too difficult at
		// normal stocking levels. A backlog of -15 is highly unlikely for a
		// reasonable player. To suffer that kind of backlog, the SCCORI
		// simulation would have average a draw of 18 or more seven times in a
		// row right from the start. Since the average draw is 11 over a span
		// of 0 to 22, this is highly unlikely.
		int retailerSOH =		theSimulationInfo.getCurrentTurnEvent()
														.getRetailerInfo().getStockOnHand();
		if(retailerSOH <= -15)	isRetailerBacklogged = true;
		else										isRetailerBacklogged = false;

		// Has the spike hit yet? If not, then all agents must strive to
		// maximize their stocks in order to get ready for the hit.
		if(theSimulationInfo.getCurrentTurnEvent().getRetailerInfo()
						.getOrderReceived() > theSimulationInfo.getMaxInventory()) {
			isSpiked = true;
		}

	// What is needed to attain the maximum stock level?
	// Order = stockingLevel - e.my.OH - e.my.LTD1 - e.my.LTD2 - e.my.LTD3
	int theOrder =  stockingLevel
									- theTurnInfo.getStockOnHand() - theTurnInfo.getLD1()
									- theTurnInfo.getLD2() - theTurnInfo.getLD3();

		// However, order as much as possible if the run has not yet 
		// been spiked or if the retailer is backlogged.
		// The increased levels will propagate up from the manufacturer.
		if(!isSpiked || isRetailerBacklogged) {
			theOrder =  theSimulationInfo.getMaxInventory()
								- theTurnInfo.getStockOnHand() - theTurnInfo.getLD1()
								- theTurnInfo.getLD2() - theTurnInfo.getLD3();
		}

		// Be considerate of the 'considerate' supplier.
		if (		 myRole != SimulationInfo.MANUFACTURER
					&& supplierTotalStock >= safetyLevel) {
			theOrder = Math.min(theOrder, supplierTurnInfo.getStockOnHand());
		}

		// In backlogged situation, we must limit the manufacturer's order
		// so as not to invoke the orderInvalid() method unnecessarily.
		if(myRole == SimulationInfo.MANUFACTURER && isRetailerBacklogged) {
			theOrder = theSimulationInfo.getMaxInventory()
					  - theTurnInfo.getStockOnHand() - theTurnInfo.getLD1()
					  - theTurnInfo.getLD2() - theTurnInfo.getLD3();
		}

		/*
		 * At this point we have an order level that is the minimum of what is
		 * needed and what the supplier has in SOH. That assumes that the
		 * supplier appears to be trying to maintain adequate stock. If the
		 * supplier appears negligent, then the order level is simply what is
		 * needed. This avoids putting good suppliers into a backlog state, but
		 * will punish suppliers who are not doing their job.
		 */

		// Do not order negative quantities
		if (theOrder < 0)
			theOrder = 0;

		// Is an order really needed?
		if (	!isRetailerBacklogged
				&& isSpiked
				&& theTurnInfo.getStockOnHand() >= safetyLevel) {
			theOrder = 0;
		}

 		// To minimize shipping costs, do not order any 'almost empty'
 		// containers; however, ignore shipping costs in backlogged mode.
 		if  (   isSpiked
 				   && !isRetailerBacklogged
 				   && theOrder < minOrder) {
 			   theOrder = 0;
 		}

		// If one container is not enough, then is the remainder enough for
		// another? However, ignore shipping costs in backlogged mode.
		if(		isSpiked
				&& !isRetailerBacklogged
				&& theOrder/containerSize > 0
				&& theOrder%containerSize < minOrder ) {
			theOrder -= (theOrder%containerSize);
		}

		// Now place the order.
		e.setAmountToOrder(theOrder);
	}

	public void orderInvalid(TurnEvent e) {
		int amountOrdered = e.getAmountToOrder();
		if (amountOrdered > 90) {
			e.setAmountToOrder(90);
		} else if (amountOrdered > 60) {
			e.setAmountToOrder(60);
		} else if (amountOrdered > 55) {
			e.setAmountToOrder(55);
		} else if (amountOrdered > 50) {
			e.setAmountToOrder(50);
		} else if (amountOrdered > 45) {
			e.setAmountToOrder(45);
		} else if (amountOrdered > 40) {
			e.setAmountToOrder(40);
		} else if (amountOrdered > 35) {
			e.setAmountToOrder(35);
		} else if (amountOrdered > 30) {
			e.setAmountToOrder(30);
		} else {
			e.setAmountToOrder(0);
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
			theSimulation1.setAgent(new RB_PullSpikeAgent(theSimulation1
					.getSimulationInfo()));

			// Bot 2
			Simulation theSimulation2 = new Simulation("BOT2_COMP667","qwerty");
			theSimulation2.joinMeeting(aRoomName);
			theSimulation2.setAgent(new RB_PullSpikeAgent(theSimulation2
					.getSimulationInfo()));

			// Bot 3
			Simulation theSimulation3 = new Simulation("BOT3_COMP667","qwerty");
			theSimulation3.joinMeeting(aRoomName);
			theSimulation3.setAgent(new RB_PullSpikeAgent(theSimulation3
					.getSimulationInfo()));

			// Bot 4
			final Simulation theSimulation4 = new Simulation("BOT4_COMP667",
					"qwerty");
			theSimulation4.joinMeeting(aRoomName);
			theSimulation4.setAgent(new RB_PullSpikeAgent(theSimulation4
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
		// testFourBots("BOT_ROOM5_PULL_RANDOM", 1);
		testFourBots("BOT_ROOM6_PULL_SPIKED", 1);
		// testFourBots("BOT_ROOM7_PULL_RANDOM", 1);
	}
}
