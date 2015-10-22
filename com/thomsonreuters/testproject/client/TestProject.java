package com.thomsonreuters.testproject.client;

//import com.thomsonreuters.testproject.shared.FieldVerifier;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TestProject implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Button sendButton = new Button("Save");
		final TextBox nameField = new TextBox();
		final TextBox dateBeginField = new TextBox();
		final TextBox dateEndField = new TextBox();
		final ListBox dateSelector = new ListBox();
		final TextBox clientNameField = new TextBox();
		final TextArea accomplishmentsField = new TextArea();
		final TextArea risksField = new TextArea();
		final TextArea nextWeekField = new TextArea();
		final TextBox hoursField = new TextBox();
		final TextBox ptoField = new TextBox();
		final Hidden userName = new Hidden();
		final TextArea distributionField = new TextArea();
		
		accomplishmentsField.setCharacterWidth(60);
		risksField.setCharacterWidth(60);
		nextWeekField.setCharacterWidth(60);
		distributionField.setCharacterWidth(60);

		nameField.setText("");
		dateBeginField.setText("");
		dateEndField.setText("");
		clientNameField.setText("");
		
		final Label errorLabel = new Label();
		final Label loggedInMsgLabel = new Label();

		// We can add style names to widgets
		sendButton.addStyleName("sendButton");

		// Add objects to the RootPanel
		RootPanel.get("loggedInMsgLabelContainer").add(loggedInMsgLabel);
		RootPanel.get("nameFieldContainer").add(nameField);
		RootPanel.get("dateBeginFieldContainer").add(dateBeginField);
		RootPanel.get("dateEndFieldContainer").add(dateEndField);
		RootPanel.get("dateSelectorContainer").add(dateSelector);
		RootPanel.get("clientNameFieldContainer").add(clientNameField);
		RootPanel.get("accomplishmentsFieldContainer").add(accomplishmentsField);
		RootPanel.get("risksFieldContainer").add(risksField);
		RootPanel.get("nextWeekFieldContainer").add(nextWeekField);
		RootPanel.get("hoursFieldContainer").add(hoursField);
		RootPanel.get("ptoFieldContainer").add(ptoField);
		RootPanel.get("distributionFieldContainer").add(distributionField);
//		RootPanel.get("").add(userName);
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);

		// Focus the cursor on the name field when the app loads
		nameField.setFocus(true);
		nameField.selectAll();

		// Create the popup dialog box
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText("Save Data");
		dialogBox.setAnimationEnabled(true);
		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		final Label textToServerLabel = new Label();
		final HTML serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>Saving data to the server:</b>"));
		dialogVPanel.add(textToServerLabel);
		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				sendButton.setEnabled(true);
				sendButton.setFocus(true);
			}
		});

		// Add a handler for selecting dates
		dateSelector.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				String dates = dateSelector.getValue(dateSelector.getSelectedIndex());
				dateBeginField.setText(dates.split("-")[0]);
				dateEndField.setText(dates.split("-")[1]);
			}
		});

		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				sendDataToServer();
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendDataToServer();
				}
			}

			private void getData(String user) {
				// try to get the user during page load
				greetingService.queryDataByUser(user,
						new AsyncCallback<List<String[]>>() {
							public void onFailure(Throwable caught) {
								// Show the RPC error message to the user
								dialogBox.setText("Remote Procedure Call - Failure");
								serverResponseLabel.addStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(SERVER_ERROR);
								dialogBox.center();
								closeButton.setFocus(true);
							}

							public void onSuccess(List<String[]> result) {
								if (result != null && result.size() > 0) {
									popPage(result.get(0));
									buildTable(result);
								}
							}
						});
			}
			
			protected void popPage(String[] data) {
				nameField.setText(data[0]);
				dateBeginField.setText(data[1]);
				dateEndField.setText(data[2]);
				clientNameField.setText(data[3]);
				accomplishmentsField.setText(data[4]);
				risksField.setText(data[5]);
				nextWeekField.setText(data[6]);
				hoursField.setText(data[7]);
				ptoField.setText(data[8]);
				distributionField.setText(data[9]);
//				userName.setValue(data[9]);
			}

			/**
			 * Send the data to the server and wait for a response.
			 */
			private void sendDataToServer() {
				// First, we validate the input.
				errorLabel.setText("");
				loggedInMsgLabel.setText("");
				String[] textToServer = new String[5];
				textToServer[0] = nameField.getText();
				textToServer[1] = dateBeginField.getText();
				textToServer[2] = dateEndField.getText();
				textToServer[3] = clientNameField.getText();
				textToServer[4] = accomplishmentsField.getText();
				textToServer[5] = risksField.getText();
				textToServer[6] = nextWeekField.getText();
				textToServer[7] = hoursField.getText();
				textToServer[8] = ptoField.getText();
				textToServer[9] = distributionField.getText();
				textToServer[10] = "F";
				
//				if (!FieldVerifier.isValidName(textToServer[0])) {
//					errorLabel.setText("Please enter at least four characters");
//					return;
//				}

				// Then, we send the input to the server.
				sendButton.setEnabled(false);
				textToServerLabel.setText(textToServer[0]);
				serverResponseLabel.setText("");
				
				greetingService.greetServer(textToServer,
						new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								// Show the RPC error message to the user
								dialogBox.setText("Remote Procedure Call - Failure");
								serverResponseLabel.addStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(SERVER_ERROR);
								dialogBox.center();
								closeButton.setFocus(true);
							}

							public void onSuccess(String result) {
								dialogBox.setText("Remote Procedure Call");
								serverResponseLabel.removeStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(result);
								dialogBox.center();
								closeButton.setFocus(true);
							}
						});
			}
		}

		// Add a handler to send the data to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);

		// try to get the user during page load
		greetingService.getUser(
				new AsyncCallback<String>() {
					public void onFailure(Throwable caught) {
						// Show the RPC error message to the user
						dialogBox.setText("Remote Procedure Call - Failure While Getting User");
						serverResponseLabel.addStyleName("serverResponseLabelError");
						serverResponseLabel.setHTML(SERVER_ERROR);
						dialogBox.center();
						closeButton.setFocus(true);
					}

					public void onSuccess(String result) {
						if (result.startsWith("https")) {
							Window.Location.assign(result);
						} else {
							userName.setDefaultValue(result);
							loggedInMsgLabel.setText("You are logged in as " + result + ", if this is incorrect please exit your browser and start from a new one.");
							MyHandler handler = new MyHandler();
							handler.getData(result);
						}
					}
				});
		
		// try to get the list of dates during page load
		greetingService.getDates(
				new AsyncCallback<String[]>() {
					public void onFailure(Throwable caught) {
						// Show the RPC error message to the user
						dialogBox.setText("Remote Procedure Call - Failure While Getting Dates");
						serverResponseLabel.addStyleName("serverResponseLabelError");
						serverResponseLabel.setHTML(SERVER_ERROR);
						dialogBox.center();
						closeButton.setFocus(true);
					}

					public void onSuccess(String[] result) {
						closeButton.setFocus(true);
						popDateSelector(dateSelector, result);
					}
				});
	}

	private void buildTable(List<String[]> data) {
		CellTable<StatusDisp> table = new CellTable<StatusDisp>();
		
		// name column.
		TextColumn<StatusDisp> nameColumn = new TextColumn<StatusDisp>() {
			@Override
			public String getValue(StatusDisp status) {
				return status.name;
			}
		};
		
		// datespan column.
		TextColumn<StatusDisp> datespanColumn = new TextColumn<StatusDisp>() {
			@Override
			public String getValue(StatusDisp status) {
				return status.datespan;
			}
		};

		// clientName column.
		TextColumn<StatusDisp> clientNameColumn = new TextColumn<StatusDisp>() {
			@Override
			public String getValue(StatusDisp status) {
				return status.clientName;
			}
		};

		// accomplishments column.
		TextColumn<StatusDisp> accomplishmentsColumn = new TextColumn<StatusDisp>() {
			@Override
			public String getValue(StatusDisp status) {
				return status.accomplishments;
			}
		};

		// accomplishments column.
		TextColumn<StatusDisp> risksColumn = new TextColumn<StatusDisp>() {
			@Override
			public String getValue(StatusDisp status) {
				return status.risks;
			}
		};

		// nextWeek column.
		TextColumn<StatusDisp> nextWeekColumn = new TextColumn<StatusDisp>() {
			@Override
			public String getValue(StatusDisp status) {
				return status.nextWeek;
			}
		};

		// hours column.
		TextColumn<StatusDisp> hoursColumn = new TextColumn<StatusDisp>() {
			@Override
			public String getValue(StatusDisp status) {
				return status.hours;
			}
		};

		// pto column.
		TextColumn<StatusDisp> ptoColumn = new TextColumn<StatusDisp>() {
			@Override
			public String getValue(StatusDisp status) {
				return status.pto;
			}
		};

		// distribution column.
		TextColumn<StatusDisp> distributionColumn = new TextColumn<StatusDisp>() {
			@Override
			public String getValue(StatusDisp status) {
				return status.distribution;
			}
		};

		table.addColumn(nameColumn, "Name");
		table.addColumn(datespanColumn, "Dates");
		table.addColumn(clientNameColumn, "Client Name");
		table.addColumn(accomplishmentsColumn, "Accomplishments");
		table.addColumn(risksColumn, "Risks");
		table.addColumn(nextWeekColumn, "Next Week");
		table.addColumn(hoursColumn, "Hours");
		table.addColumn(ptoColumn, "PTO");
		table.addColumn(distributionColumn, "Distribution");
		
		ListDataProvider<StatusDisp> dataProvider = new ListDataProvider<StatusDisp>();
		dataProvider.addDataDisplay(table);
		List<StatusDisp> list = dataProvider.getList();
		for (String[] dataRow : data) {
			StatusDisp status = new StatusDisp(dataRow[0], dataRow[1], dataRow[2], dataRow[3], dataRow[4], dataRow[5], dataRow[6], dataRow[7], dataRow[8], dataRow[9]);
			list.add(status);
	    }
		RootPanel.get("statusHistoryTableContainer").add(table);
	}
	
	// A simple data type that represents a status
	private static class StatusDisp {
		private final String name;
		private final String datespan;
		private final String clientName;
		private final String accomplishments;
		private final String risks;
		private final String nextWeek;
		private final String hours;
		private final String pto;
		private final String distribution;

		public StatusDisp(String name, String dateBeg, String dateEnd, String clientName, String accomplishments, String risks, String nextWeek, String hours, String pto, String distribution) {
			this.name = name;
			this.datespan = dateBeg + "-" + dateEnd;
			this.clientName = clientName;
			this.accomplishments = accomplishments;
			this.risks = risks;
			this.nextWeek = nextWeek;
			this.hours = hours;
			this.pto = pto;
			this.distribution = distribution;
		}
	}

	private void popDateSelector(ListBox dateSelector, String[] dates) {
		for (int i = 0; i < dates.length; i++) {
			dateSelector.addItem(dates[i]);
		}
	}
}
