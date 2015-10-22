package com.thomsonreuters.testproject.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface GreetingService extends RemoteService {
	String greetServer(String[] data) throws IllegalArgumentException;
	String getUser() throws IllegalArgumentException;
	List<String[]> queryDataByUser(String user) throws IllegalArgumentException;
	String[] getDates() throws IllegalArgumentException;
}
