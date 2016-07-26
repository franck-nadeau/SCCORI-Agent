package ca.athabascau.sccori.comm;

import ca.athabascau.sccori.data.TurnEvent;

public interface TurnListener {
	public void executeTurn(TurnEvent e);
	
	public void orderInvalid(TurnEvent e);

	public void simulationHasEnded();
}
