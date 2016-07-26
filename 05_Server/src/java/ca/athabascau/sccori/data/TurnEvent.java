package ca.athabascau.sccori.data;

/**
 * This is a container object representing all of the information presented to
 * an agent during a single simualiton turn. This object also contains the
 * amount of items ordered by the agent during this turn.
 */
public class TurnEvent {
	private final int theCycleNumber;

	private int theAmountToOrder = 0;

	private TurnInfo theRetailerInfo;

	private TurnInfo theWholesalerInfo;

	private TurnInfo theDistributorInfo;

	private TurnInfo theManufacturerInfo;

	/**
	 * 
	 * @param aCycleNumber
	 */
	public TurnEvent(int aCycleNumber) {
		theCycleNumber = aCycleNumber;
	}

	public int getCycleNumber() {
		return theCycleNumber;
	}

	public void setAmountToOrder(int anAmount) {
		theAmountToOrder = anAmount;
	}

	public int getAmountToOrder() {
		return theAmountToOrder;
	}

	public TurnInfo getDistributorInfo() {
		return theDistributorInfo;
	}

	public void setDistributorInfo(TurnInfo theDistributorInfo) {
		this.theDistributorInfo = theDistributorInfo;
	}

	public TurnInfo getManufacturerInfo() {
		return theManufacturerInfo;
	}

	public void setManufacturerInfo(TurnInfo theManufacturerInfo) {
		this.theManufacturerInfo = theManufacturerInfo;
	}

	public TurnInfo getRetailerInfo() {
		return theRetailerInfo;
	}

	public void setRetailerInfo(TurnInfo theRetailerInfo) {
		this.theRetailerInfo = theRetailerInfo;
	}

	public TurnInfo getWholesalerInfo() {
		return theWholesalerInfo;
	}

	public void setWholesalerInfo(TurnInfo theWholesalerInfo) {
		this.theWholesalerInfo = theWholesalerInfo;
	}
	
	/**
	 * Overriden for debuging purposes.
	 */
	public String toString() {
		StringBuffer theStringBuffer = new StringBuffer();
		
		theStringBuffer.append("Turn Event for Cycle ");
		theStringBuffer.append(getCycleNumber());
		theStringBuffer.append("\n");
		theStringBuffer.append("Role\tOrder Received\tOrder Placed\tLTD1\tLTD2\tLTD3\tOn Hand\tUnit Cost\tCycle Cost\tTotal Cost");
		theStringBuffer.append("\nRetailer\t");
		theStringBuffer.append(getRetailerInfo());
		theStringBuffer.append("\nWholesaler\t");
		theStringBuffer.append(getWholesalerInfo());
		theStringBuffer.append("\nDistributor\t");
		theStringBuffer.append(getDistributorInfo());
		theStringBuffer.append("\nManufacture\t");
		theStringBuffer.append(getManufacturerInfo());
		
		return theStringBuffer.toString(); 
	}
}