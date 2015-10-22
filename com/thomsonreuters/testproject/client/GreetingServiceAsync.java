package com.thomsonreuters.testproject.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
	void greetServer(String[] input, AsyncCallback<String> callback) throws IllegalArgumentException;
	void getUser(AsyncCallback<String> callback) throws IllegalArgumentException;
	void queryDataByUser(String user, AsyncCallback<List<String[]>> callback) throws IllegalArgumentException;
	void getDates(AsyncCallback<String[]> callback) throws IllegalArgumentException;
}
