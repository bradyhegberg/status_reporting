package com.thomsonreuters.testproject.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.thomsonreuters.testproject.client.GreetingService;
import com.thomsonreuters.testproject.shared.FieldVerifier;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {

	private static final Logger log = Logger.getLogger(GreetingServiceImpl.class.getName());
	 
	public String greetServer(String[] input) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidName(input[0])) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"Name must be at least 4 characters long");
		}

		UserService userService = UserServiceFactory.getUserService();
		User currentUser = userService.getCurrentUser();
		if (currentUser == null) {
			return "Error: Unable to get userid.";
			//resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
		}
		
		//String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");

		// Escape data from the client to avoid cross-site script vulnerabilities.
		String name = escapeHtml(input[0]);
		String dateBeg = escapeHtml(input[1]);
		userAgent = escapeHtml(userAgent);

		try {
			for (String addr: input[9].split(",")) {
				sendEmail(input, addr);
			}
		} catch(Exception e) {
			return "Error sending email.";
		}
		persistData(input, currentUser);
		
		return "Data has been saved and sent for " + name + "<br><br> for week of " + dateBeg;
	}

	public String getUser() throws IllegalArgumentException {
		UserService userService = UserServiceFactory.getUserService();
		User currentUser = userService.getCurrentUser();

		log.setLevel(Level.INFO);
		log.info("RequestURI: " + getThreadLocalRequest().getRequestURI());
		
		String response = null;
		if (currentUser == null) {
			response = userService.createLoginURL("");    //getThreadLocalRequest().getRequestURI()
		} else {
			response = currentUser.getNickname();
		}
		
		return response;
	}

	public List<String[]> queryDataByUser(String user) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userEqualsFilter =  new FilterPredicate("userid",
										FilterOperator.EQUAL,
										user);
		Query q = new Query("Status")
					.setFilter(userEqualsFilter)
					.addSort("date", SortDirection.DESCENDING);
		PreparedQuery pq = datastore.prepare(q);
		
		List<String[]> rows = new ArrayList<String[]>();
		for (Entity result : pq.asIterable()) {
			String[] row = new String[11];
			
			row[0] = (String) result.getProperty("name");
			row[1] = (String) result.getProperty("date_begin");
			row[2] = (String) result.getProperty("date_end");
			row[3] = (String) result.getProperty("client_name");
			row[4] = (String) result.getProperty("accomplishments");
			row[5] = (String) result.getProperty("risks");
			row[6] = (String) result.getProperty("next_week");
			row[7] = (String) result.getProperty("hours");
			row[8] = (String) result.getProperty("pto");
			row[9] = (String) result.getProperty("distribution");
			row[10] = (String) result.getProperty("submitted");
			
			rows.add(row);
		}
		return rows;
	}
	
	@Override
	public String[] getDates() throws IllegalArgumentException {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format1 = new SimpleDateFormat("MM/dd/yyyy");
		String mon = "";
		String fri = "";
		String[] dates = new String[8];
		
		while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
			cal.add(Calendar.DAY_OF_WEEK, 1);
		}
		cal.add(Calendar.DAY_OF_WEEK, -21);

		for (int i = 0; i < 8; i++) {
			cal.add(Calendar.DAY_OF_WEEK, 3);
			mon = format1.format(cal.getTime());
			cal.add(Calendar.DAY_OF_WEEK, 4);
			fri = format1.format(cal.getTime());
			dates[i] = mon + "-" + fri;
		}
		return dates;
	}

	private void persistData(String[] input, User currentUser) {
	    String weeklystatusName = "weeklystatuslist";
	    Key statusKey = KeyFactory.createKey("WeeklyStatus", weeklystatusName);
	    Date date = new Date();
	    Entity status = new Entity("Status", statusKey);
	    status.setProperty("userid", currentUser.getNickname());
	    status.setProperty("date", date);
	    status.setProperty("name", input[0]);
	    status.setProperty("date_begin", input[1]);
	    status.setProperty("date_end", input[2]);
	    status.setProperty("client_name", input[3]);
	    status.setProperty("accomplishments", input[4]);
	    status.setProperty("risks", input[5]);
	    status.setProperty("next_week", input[6]);
	    status.setProperty("hours", input[7]);
	    status.setProperty("pto", input[8]);
	    status.setProperty("distribution", input[9]);
	    status.setProperty("submitted", input[10]);

	    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	    datastore.put(status);
	}
	
	private void sendEmail(String[] input, String toAddr) {
		Properties prop = new Properties();
	    Session session = Session.getDefaultInstance(prop, null);
		String msgBody = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional //EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
				"<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
				"<head>" +
				"<title>Weekly Status</title>" +
				"</head>" +
				"<body>" +
				"<table>" +
				"<tr><td style=\"font-weight:bold;\">Name:</td><td>" + input[0] + "</td></tr>" +
				"<tr><td style=\"font-weight:bold;\">Dates:</td><td>" + input[1] + " - " + input[2] + "</td></tr>" +
				"<tr><td style=\"font-weight:bold;\">Client Name:</td><td>" + input[3] + "</td></tr>" +
				"<tr><td style=\"font-weight:bold;\">Accomplishments/<br>Milestones:</td><td>" + input[4] + "</td></tr>" +
				"<tr><td style=\"font-weight:bold;\">Risks/Concern Areas:</td><td>" + input[5] + "</td></tr>" +
				"<tr><td style=\"font-weight:bold;\">Next Week’s Plan:</td><td>" + input[6] + "</td></tr>" +
				"<tr><td style=\"font-weight:bold;\">Hours:</td><td>" + input[7] + "</td></tr>" +
				"<tr><td style=\"font-weight:bold;\">PTO:</td><td>" + input[8] + "</td></tr>" +
				"</table>" +
				"</body>" +
				"</html>";
		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("bradyh@gmail.com"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddr));
			msg.setSubject("Weekly Status for " + input[0] + " - week of " + input[1]);
			msg.setContent(msgBody, "text/html; charset=utf-8");
			Transport.send(msg);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				   .replaceAll(">", "&gt;");
	}
}
