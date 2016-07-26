package ca.athabascau.sccori.comm;

/**
 * Simple interface used to monitor errors which are discarded
 * by the EternalThreadedRefreshHandler. 
 */
public interface ErrorNotifier {
	
	public void notify(Exception e);

}
