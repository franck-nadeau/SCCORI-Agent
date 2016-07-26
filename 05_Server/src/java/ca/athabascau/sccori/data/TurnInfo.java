package ca.athabascau.sccori.data;
/**
 * This contianer object represent the information 
 * for a single player during one of its turn. That 
 * is, the stock on hand, order received, LD1, LD2,
 * LD3, unit cost, total cost, and the cycle number. 
 *
 */
public class TurnInfo {
	private int theOrderReceived;
	private int theOrderPlaced;
	private int theLD1;
	private int theLD2;
	private int theLD3;
	private int theStockOnHand;
	private double theUnitCost;
	private double theCycleCost;
	private double theTotalCost;
	private int theCycleNumber;
	private int theInfoForRole;
	
	public double getCycleCost() {
		return theCycleCost;
	}
	public void setCycleCost(double theCycleCost) {
		this.theCycleCost = theCycleCost;
	}
	public int getCycleNumber() {
		return theCycleNumber;
	}
	public void setCycleNumber(int theCycleNumber) {
		this.theCycleNumber = theCycleNumber;
	}
	public int getLD1() {
		return theLD1;
	}
	public void setLD1(int theLD1) {
		this.theLD1 = theLD1;
	}
	public int getLD2() {
		return theLD2;
	}
	public void setLD2(int theLD2) {
		this.theLD2 = theLD2;
	}
	public int getLD3() {
		return theLD3;
	}
	public void setLD3(int theLD3) {
		this.theLD3 = theLD3;
	}
	public int getOrderReceived() {
		return theOrderReceived;
	}
	public void setOrderReceived(int theOrderReceived) {
		this.theOrderReceived = theOrderReceived;
	}
	public int getOrderPlaced() {
		return theOrderPlaced;
	}
	public void setOrderPlaced(int theOrderPlaced) {
		this.theOrderPlaced = theOrderPlaced;
	}
	public int getStockOnHand() {
		return theStockOnHand;
	}
	public void setStockOnHand(int theStockOnHand) {
		this.theStockOnHand = theStockOnHand;
	}
	public double getTotalCost() {
		return theTotalCost;
	}
	public void setTotalCost(double theTotalCost) {
		this.theTotalCost = theTotalCost;
	}
	public double getUnitCost() {
		return theUnitCost;
	}
	public void setUnitCost(double theUnitCost) {
		this.theUnitCost = theUnitCost;
	}
	public int getInfoForRole() {
		return theInfoForRole;
	}
	public void setInfoForRole(int theInfoForRole) {
		this.theInfoForRole = theInfoForRole;
	}
	
	public String toString() {
		StringBuffer theStringBuffer = new StringBuffer();
		
		//Order Received
		theStringBuffer.append(getOrderReceived());
		theStringBuffer.append("\t\t");
		//Order Placed
		theStringBuffer.append(getOrderPlaced());
		theStringBuffer.append("\t\t");		
		//LTD1
		theStringBuffer.append(getLD1());
		theStringBuffer.append("\t");		
		//LTD2
		theStringBuffer.append(getLD2());
		theStringBuffer.append("\t");		
		//LTD3
		theStringBuffer.append(getLD3());
		theStringBuffer.append("\t");		
		//On Hand
		theStringBuffer.append(getStockOnHand());
		theStringBuffer.append("\t");
		//Unit Cost
		theStringBuffer.append(getUnitCost());
		theStringBuffer.append("\t\t");
		//Cycle Cost
		theStringBuffer.append(getCycleCost());
		theStringBuffer.append("\t\t");		
		//Total Cost
		theStringBuffer.append(getTotalCost());
		theStringBuffer.append("\t");		
		
		return theStringBuffer.toString(); 
	}	
}