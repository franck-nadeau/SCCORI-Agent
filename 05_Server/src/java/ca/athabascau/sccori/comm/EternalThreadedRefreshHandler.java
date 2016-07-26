package ca.athabascau.sccori.comm;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.RefreshHandler;
import com.gargoylesoftware.htmlunit.WaitingRefreshHandler;

public class EternalThreadedRefreshHandler implements RefreshHandler {
	List<ErrorNotifier> theErrorNotifiers = new LinkedList<ErrorNotifier>();
	private Vector<ThreadPageUrlSeconds> vector = new Vector<ThreadPageUrlSeconds>();
	
    /**
     * Refreshes the specified page using the specified URL after the specified number
     * of seconds.
     * @param page The page that is going to be refreshed.
     * @param url The URL where the new page will be loaded.
     * @param seconds The number of seconds to wait before reloading the page.
     */
    public void handleRefresh(final Page page, final URL url, final int seconds) {
        final Thread thread = new Thread( "ThreadedRefreshHandler Thread" ) {
            public void run() {
                try {
                    new WaitingRefreshHandler().handleRefresh(page, url, seconds);
                }
                catch(Exception e ) {
                	for(ErrorNotifier aErrorNotifier : theErrorNotifiers) {
                		aErrorNotifier.notify(e);
                	}
                }
            }
        };
        thread.start();
	ThreadPageUrlSeconds theTpus = new ThreadPageUrlSeconds(thread, page, url, seconds);
	vector.add(theTpus);
    }

    public void stopRefreshing(Page page, URL url, int seconds) {
	for (int i=0;i<vector.size();i++) {
            ThreadPageUrlSeconds theTpus = vector.get(i);
	    if (theTpus.getPage()==page && theTpus.getUrl()==url && theTpus.getSeconds()==seconds) {
		Thread thread = theTpus.getThread();
		thread.stop();
		vector.remove(i);
		return;
	    }
 	}
    }
    
    public void addErrorNotifier(ErrorNotifier notifier) {
    	theErrorNotifiers.add(notifier);
    }

    private class ThreadPageUrlSeconds {
	private Thread thread;
	private Page page;
	private URL url;
	private int seconds;

	public ThreadPageUrlSeconds(Thread thread, Page page, URL url, int seconds) {
	    this.thread = thread;
	    this.page = page;
	    this.url = url;
	    this.seconds = seconds;
	}

	public Thread getThread() { return thread; }
	public Page getPage() { return page; }
	public URL getUrl() { return url; }
	public int getSeconds() { return seconds; }
    }

}
