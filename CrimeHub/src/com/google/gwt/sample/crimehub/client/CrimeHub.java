package com.google.gwt.sample.crimehub.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.ajaxloader.client.ArrayHelper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.ui.HeatMapLayerWidget;
import com.google.gwt.core.client.ui.PsTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.ColumnChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;



/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CrimeHub implements EntryPoint{

	private List<Crime> parsedCrimes = new ArrayList<Crime>();
	private List<Crime> filteredCrimes = new ArrayList<Crime>();
	private List<Crime> mapCrimesToDisplay = new ArrayList<Crime>();

	private VerticalPanel mainPanel = new VerticalPanel();
	private VerticalPanel filterPanel = new VerticalPanel();
	private HorizontalPanel searchPanel = new HorizontalPanel();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private HorizontalPanel linkPanel = new HorizontalPanel();
	private VerticalPanel loadPanel = new VerticalPanel();
	private HorizontalPanel mapPanel = new HorizontalPanel();
	private VerticalPanel welcomePanel = new VerticalPanel();

	private TabBar tab_bar = new TabBar();
	private PsTable crimesFlexTable = new PsTable();

	private TextBox startYearBox = new TextBox();
	private TextBox startMonthBox = new TextBox();
	private TextBox endYearBox = new TextBox();
	private TextBox endMonthBox = new TextBox();
	private Button goButton = new Button("Go");
	private Label dateErrorLabel = new Label("");
	private Button resetButton = new Button("Reset ALL filters");
	private TextBox searchBox = new TextBox();
	private Button goSearch = new Button("Go");
	
	private String startYear;
	private String startMonth;
	private String endYear;
	private String endMonth;
	private String typeFilter;
	private String streetFilter = "";

	private boolean firstLoad = true;
	private boolean dateFilter = false;
	private boolean tableNeverLoaded = true;

	private Label loadingLabel = new Label("Currently loading data, please wait...");

	final Button graph = new Button("Graph");
	final Button table = new Button("Table");
	final Button map = new Button("Map");
	final Button upload = new Button("Upload");

	private final CrimeServiceAsync crimeService = GWT.create(CrimeService.class);
	private final LoginServiceAsync loginServlet = GWT.create(LoginService.class);

	private LatLng[] points;

	private LoginInfo loginInfo = new LoginInfo();
	private VerticalPanel loginPanel = new VerticalPanel();
	private VerticalPanel logoutPanel = new VerticalPanel();

	
// <<<<<<<<<<<<< Graph
	// Create a graph to display the amount of different crimes by specific year
	private Options createOptions() {
		Options options = Options.create();
		options.setWidth(850);
		options.setHeight(600);
		options.setTitle("Crime Column Graph");
		return options;
	}

	private AbstractDataTable createTable(String year) {
		DataTable data = DataTable.create();
		data.addColumn(ColumnType.STRING, "CrimeType");
		data.addColumn(ColumnType.NUMBER, "Amount");
		data.addRows(7);
		data.setValue(0, 0, "Mischief Under $5000");
		data.setValue(0, 1, countCrimebyCrimeType(year, "Mischief Under $5000"));
		data.setValue(1, 0, "Mischief Over $5000");
		data.setValue(1, 1, countCrimebyCrimeType(year, "Mischief Over $5000"));
		data.setValue(2, 0, "Theft From Auto Under $5000");
		data.setValue(2, 1, countCrimebyCrimeType(year, "Theft From Auto Under $5000"));
		data.setValue(3, 0, "Theft From Auto Over $5000");
		data.setValue(3, 1, countCrimebyCrimeType(year, "Theft From Auto Over $5000"));
		data.setValue(4, 0, "Commercial Break And Enter");
		data.setValue(4, 1, countCrimebyCrimeType(year, "Commercial Break And Enter"));
		data.setValue(5, 0, "Theft Of Auto Under $5000");
		data.setValue(5, 1, countCrimebyCrimeType(year, "Theft Of Auto Under $5000"));
		data.setValue(6, 0, "Theft Of Auto Over $5000");
		data.setValue(6, 1, countCrimebyCrimeType(year, "Theft Of Auto Over $5000"));
		return data;
	}

	private int countCrimebyCrimeType(String year, String CrimeType){
		int count = 0;
		for (Crime c: parsedCrimes){
			if (c.getType().contains(CrimeType) && c.getYear().contains(year)){
				filteredCrimes.add(c);
				count++;
			}
		}
		return count;
	}
	
	// Graph >>>>>>>>>>>>>>

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// Check login status using login service.
		loginServlet.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
			public void onFailure(Throwable error) {
				System.out.println("Login Unsuccessful.");
			}
			public void onSuccess(LoginInfo result) {
				loginInfo = result;
				if(loginInfo.isLoggedIn()) {
					System.out.println("User has logged in.");
					loadCrimeHub();
				} else {
					System.out.println("User is not logged in.");
					loadLogin();
				}
				System.out.println("Login Successful.");
			}
		});
	}

	private void loadLogin() {
		// Assemble login panel.
		HTML login = new HTML("<a href='"
				+ loginInfo.getLoginUrl()
				+ "'>Login</a><br>");
		loginPanel.add(login);
		RootPanel.get("signin").add(loginPanel);
	}

	private void loadCrimeHub() {
		retrieveCrimes();
		
		HTML logout = new HTML("<a href='"
				+ loginInfo.getLogoutUrl()
				+ "' style='color:white'>Logout</a><br>");
		logoutPanel.add(logout);
		RootPanel.get("signin").add(logoutPanel);
		
		crimesFlexTable.setStyleName("crimesFlexTable");
		
		// Buttons for choosing a data representation
		graph.addStyleName("Button");
		table.addStyleName("Button");
		map.addStyleName("Button");
		upload.addStyleName("Button");

		// Label for informing user when data is being loaded
		loadingLabel.setStyleName("loadingLabel");
		loadPanel.setWidth("100%");
		loadPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		loadPanel.add(loadingLabel);

		linkPanel.setStyleName("linkPanel");
		linkPanel.add(graph);
		linkPanel.add(table);
		linkPanel.add(map);
		// Upload button only available for admin users
		if (loginInfo.isAdmin() == true) {
			linkPanel.add(upload);
		}

		// Panel that holds the buttons and label defined above
		loadPanel.add(linkPanel);

		// Panel for heatMap display
		mapPanel.setWidth("100%");
		mapPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		// Date range search bar setup
		startYearBox.setText("Start Year");
		startMonthBox.setText("Start Month (numeric)");
		endYearBox.setText("End Year");
		endMonthBox.setText("End Month (numeric)");
		final HTML daterange = new HTML("<p style='color:white'>Date Range: </p>");
		final HTML to = new HTML("<p style='color:white'>To </p>");
		final HTML streetSearch = new HTML ("<p style='color:white'>Street Name: </p>");
		daterange.setStyleName("daterange");
		to.setStyleName("daterange");
		streetSearch.setStyleName("daterange");
		goButton.setStyleName("goButton");
		goSearch.setStyleName("goButton");
		dateErrorLabel.setStyleName("dateErrorLabel");
		dateErrorLabel.addStyleName("whiteText");
		resetButton.setStyleName("resetButton");

		addPanel.addStyleName("addPanel");
		addPanel.add(daterange);
		addPanel.add(startYearBox);
		addPanel.add(startMonthBox);
		addPanel.add(to);
		addPanel.add(endYearBox);
		addPanel.add(endMonthBox);
		addPanel.add(goButton);
		addPanel.add(resetButton);
		addPanel.add(dateErrorLabel);
		
		searchPanel.setStyleName("searchPanel");
		searchPanel.add(streetSearch);
		searchPanel.add(searchBox);
		searchPanel.add(goSearch);
		
		filterPanel.add(searchPanel);
		filterPanel.add(addPanel);

		// Tab bar set up
		tab_bar.addTab("All Crimes");
		tab_bar.addTab("Mischief Under $5000");
		tab_bar.addTab("Mischief Over $5000");
		tab_bar.addTab("Theft From Auto Under $5000");
		tab_bar.addTab("Theft From Auto Over $5000");
		tab_bar.addTab("Commercial Break And Enter");
		tab_bar.addTab("Theft Of Auto Under $5000");
		tab_bar.addTab("Theft Of Auto Over $5000");

		mainPanel.setWidth("100%");
		mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		mainPanel.setHeight("100%");

		welcomePanel.setWidth("100%");
		welcomePanel.setHeight("100%");
		welcomePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		welcomePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		final HTML message = new HTML("<h1 style='color:white'>Welcome to CrimeHub! Your number one service in finding small crimes in Vancouver</h1>");
		message.addStyleName("cover-container");
		message.addStyleName("jumbotron");
		welcomePanel.add(message);

		RootPanel.get().add(loadPanel);
		RootPanel.get().add(mainPanel);
		RootPanel.get().add(mapPanel);
		RootPanel.get().add(welcomePanel);

		table.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				mainPanel.clear();
				mapPanel.setVisible(false);
				welcomePanel.setVisible(false);
				CrimeTable();

				table.setEnabled(false);
				graph.setEnabled(true);
				map.setEnabled(true);
				upload.setEnabled(true);
			}
		});

		graph.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				mainPanel.clear();
				welcomePanel.setVisible(false);
				mapPanel.setVisible(false);
				getGraph();
				graph.setEnabled(false);
				table.setEnabled(true);
				map.setEnabled(true);
				upload.setEnabled(true);
			}
		});

		map.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				mainPanel.clear();
				mapPanel.clear();
				welcomePanel.setVisible(false);
				mapPanel.setVisible(true);
				loadMapApi();
				graph.setEnabled(true);
				table.setEnabled(true);
				map.setEnabled(false);
				upload.setEnabled(true);
			}			
		});

		upload.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				mainPanel.clear();
				welcomePanel.setVisible(false);
				mapPanel.setVisible(false);
				buttonSetter(false);
				loadingLabel.setText("Currently loading data, please wait...");
				AsyncCallback<Void> ac = new AsyncCallback<Void>() {
					public void onFailure(Throwable error) {
						buttonSetter(true);
						System.out.println("storeCrimes(): Crimes not stored. Show stack trace.");
					}
					public void onSuccess(Void v) {
						retrieveCrimes();
						System.out.println("storeCrimes(): Crimes successfully stored in datastore.");
					}
				};
				crimeService.storeCrimes(ac);
			}			
		});

		DOM.removeChild(RootPanel.getBodyElement(), DOM.getElementById("loading"));
	}
	
	private void retrieveCrimes() {
		loadingLabel.setText("Currently loading data, please wait...");
		buttonSetter(false);
		crimeService.getCrimes(new AsyncCallback<List<Crime>>() {
			public void onFailure(Throwable error) {
				System.out.println("retrieveCrimes(): Crimes not retrieved.");	
				buttonSetter(true);
				loadingLabel.setText("Select any of the options below to view crime data!");
			}
			public void onSuccess(List<Crime> crimes) { 
				parsedCrimes = crimes;
				buttonSetter(true);
				loadingLabel.setText("Select any of the options below to view crime data!");
				System.out.println("retrieveCrimes(): Parsed crimes successfully retrieved.");
			}
		});
	}

	private void CrimeTable() {
		tableNeverLoaded = false;
		
		tab_bar.addSelectionHandler(new SelectionHandler<Integer>() {
			public void onSelection(SelectionEvent<Integer> event) {
				if (event.getSelectedItem() == 0) {
					setTypeFilter(" ");
				}
				if (event.getSelectedItem() == 1) {
					setTypeFilter("Mischief Under");
				}
				if (event.getSelectedItem() == 2) {
					setTypeFilter("Mischief Over");
				}
				if (event.getSelectedItem() == 3) {
					setTypeFilter("Theft From Auto Under");
				}
				if (event.getSelectedItem() == 4) {
					setTypeFilter("Theft From Auto Over");
				}
				if (event.getSelectedItem() == 5) {
					setTypeFilter("Commercial Break and Enter");
				}
				if (event.getSelectedItem() == 6) {
					setTypeFilter("Theft Of Auto Under");
				}
				if (event.getSelectedItem() == 7) {
					setTypeFilter("Theft Of Auto Over");
				}
			}
		});

		// Add handlers to text boxes
		startYearBox.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (startYearBox.getText().equals("Start Year")) {
					startYearBox.setText("");
				}
			}
		});

		startMonthBox.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (startMonthBox.getText().equals("Start Month (numeric)")) {
					startMonthBox.setText("");
				}
			}
		});

		endYearBox.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (endYearBox.getText().equals("End Year")) {
					endYearBox.setText("");
				}
			}
		});

		endMonthBox.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (endMonthBox.getText().equals("End Month (numeric)")) {
					endMonthBox.setText("");
				}
			}
		});
		// Filter crimes by date range and or street
		goButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (dateIsValid(startYearBox.getText(), startMonthBox.getText(), 
						endYearBox.getText(), endMonthBox.getText())) {
					startYear = startYearBox.getText();
					startMonth = startMonthBox.getText();
					endYear = endYearBox.getText();
					endMonth = endMonthBox.getText();
					dateFilter = true;
					filterCrimes();
					dateErrorLabel.setText("Crimes successfully filtered. If none are being displayed, the date range may be invalid.");
				}
				else {
					dateErrorLabel.setText("Error! Text boxes cannot be empty. No whitespace, years: 4 digits, months: between 1 to 12.");
				}
			}
		});

		resetButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				typeFilter = "";
				streetFilter = "";
				dateFilter = false;
				tab_bar.selectTab(0); 
				startYearBox.setText("Start Year");
				startMonthBox.setText("Start Month (numeric)");
				endYearBox.setText("End Year");
				endMonthBox.setText("End Month (numeric)");
				searchBox.setText("");
				dateErrorLabel.setText("");
			}
		});

		goSearch.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
					streetFilter = searchBox.getText();
					filterCrimes();
			}
		});
		
		// Show all crimes the first time the table is loaded 
		if (firstLoad) {
			tab_bar.selectTab(0); 
			firstLoad = false;
		}

		mainPanel.add(filterPanel);
		mainPanel.add(tab_bar);
		mainPanel.add(crimesFlexTable);
	}

	// Returns true if the strings contain only numerics, year is 4 digits long, and months are between 1-12
	private boolean dateIsValid(String startYear, String startMonth, String endYear, String endMonth) {
		if (startYear.matches("[0-9]+") && startYear.length() == 4 && startMonth.matches("[0-9]+") && 
				Integer.parseInt(startMonth) > 0  && Integer.parseInt(startMonth) < 13 &&
				endYear.matches("[0-9]+") && endYear.length() == 4 && endMonth.matches("[0-9]+") && 
				Integer.parseInt(endMonth) > 0 && Integer.parseInt(endMonth) < 13) 	{
			return true;
		}
		else return false;
	}

	// Filters parsedCrimes by type, date, or street, and displays it in table
	private void filterCrimes() {
		System.out.println("entered filter crimes");
		filteredCrimes.clear();
		if (!typeFilter.isEmpty() && dateFilter == true && !streetFilter.isEmpty()) {
			System.out.println("entered filter ");
			filterByAll();
		}
		else if (!typeFilter.isEmpty() && dateFilter == true && streetFilter.isEmpty()) {
			System.out.println("entered filter by type and date");
			filterByTypeAndDate();
		}
		else if (!typeFilter.isEmpty() && dateFilter == false && !streetFilter.isEmpty()) {
			System.out.println("entered filter by type and street");
			filterByTypeAndStreet();
		}	
		else if  (typeFilter.isEmpty() && dateFilter == true && !streetFilter.isEmpty()) {
			System.out.println("entered filter by date and street");
			filterByDateAndStreet();
		}
		else if (!typeFilter.isEmpty()) {
			System.out.println("entered filter type");
			filterByType();
		}
		else if (dateFilter) {
			System.out.println("entered filter by date");
			filterByDate();
		}
		else {
			System.out.println("entered filter by street");
			filterByStreet();
		}
		System.out.println("exit filter crimes");
		crimesFlexTable.showCrimes(filteredCrimes);
		setCrimesToMap(filteredCrimes);
	}
	
	private void filterByDateAndStreet() {
		List<Crime> tempCrimes = new ArrayList<Crime>();
		filterByDate();
		for (Crime c : filteredCrimes) {
			tempCrimes.add(c);
		}
		filteredCrimes.clear();
		for (Crime c : tempCrimes) {
			if  (c.getAddress().contains(streetFilter.toUpperCase())) {
				filteredCrimes.add(c);
			}
		}
	}
	
	private void filterByTypeAndStreet () {
		List<Crime> tempCrimes = new ArrayList<Crime>();
		filterByType();
		for (Crime c : filteredCrimes) {
			tempCrimes.add(c);
		}
		filteredCrimes.clear();
		for (Crime c : tempCrimes) {
			if  (c.getAddress().contains(streetFilter.toUpperCase())) {
				filteredCrimes.add(c);
			}
		}
	}
	
	private void filterByAll () {
		List<Crime> tempCrimes = new ArrayList<Crime>();
		filterByDate();
		for (Crime c : filteredCrimes) {
			tempCrimes.add(c);
		}
		filteredCrimes.clear();
		for (Crime c : tempCrimes) {
			if  (c.getAddress().contains(streetFilter.toUpperCase())) {
				filteredCrimes.add(c);
			}
		}
		tempCrimes.clear();
		for (Crime c : filteredCrimes) {
			tempCrimes.add(c);
		}
		filteredCrimes.clear();
		for (Crime c : tempCrimes) {
			if (c.getType().contains(typeFilter)) {
				filteredCrimes.add(c);
			}
		}
	}

	private void filterByTypeAndDate() {
		List<Crime> tempCrimes = new ArrayList<Crime>();
		filterByDate();
		for (Crime c : filteredCrimes) {
			tempCrimes.add(c);
		}
		filteredCrimes.clear();
		for (Crime c : tempCrimes) {
			if (c.getType().contains(typeFilter)) {
				filteredCrimes.add(c);
			}
		}
	}

	// Filter crimes by date 
	private void filterByDate() {		
		DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM");
		String sMonth, eMonth;
		// Convert month numerics to double digit format, store in temp string
		if (startMonth.length() == 1) {
			sMonth = "0"+startMonth;
		} else {
			sMonth = startMonth;
		}
		if (endMonth.length() == 1) {
			eMonth = "0"+endMonth;
		} else {
			eMonth = endMonth;
		}

		Date minDate = dtf.parse(startYear+"-"+sMonth);
		Date maxDate = dtf.parse(endYear+"-"+eMonth);

		String crimeMonth;
		for (Crime c: parsedCrimes) {
			// Convert crime month numeric to double digit format, store in crimeMonth
			if (c.getMonth().length() == 1) {
				crimeMonth = "0"+c.getMonth();
			}
			else {
				crimeMonth = c.getMonth();
			}
			// Add crime to filteredCrimes if the crime's date is between the specified range
			Date crimeDate = dtf.parse(c.getYear()+"-"+crimeMonth);
			if ((DateUtils.after(minDate, crimeDate) || DateUtils.equals(minDate, crimeDate)) &&
					(DateUtils.before(maxDate, crimeDate) || DateUtils.equals(maxDate, crimeDate))) {
				filteredCrimes.add(c);
			}
		}
	}

	private void setTypeFilter(String type) {
		typeFilter = type;
		filterCrimes();
	}
	
	private void filterByStreet() {
		for (Crime c : parsedCrimes) {
			if (c.getAddress().contains(streetFilter.toUpperCase())) {
				filteredCrimes.add(c);
			}
		}
	}

	private void filterByType() {
		for (Crime c : parsedCrimes) {
			if (c.getType().contains(typeFilter)) {
				filteredCrimes.add(c);
			}
		}
	}

	private void setCrimesToMap(List<Crime> crimes) {
		mapCrimesToDisplay.clear();
		for (Crime c : crimes) {
			mapCrimesToDisplay.add(c);
		}
	}

	private void buttonSetter(Boolean bool) {
		if (bool) {
			table.setEnabled(true);
			graph.setEnabled(true);
			map.setEnabled(true);
			upload.setEnabled(true);
		} else {
			table.setEnabled(false);
			graph.setEnabled(false);
			map.setEnabled(false);
			upload.setEnabled(false);
		}
	}
	
	// Display the graph
	private void getGraph() {
		Runnable onLoadCallback = new Runnable() {
			public void run() {
				Button year1Button = new Button("Crime Data Graph 2012");
				Button year2Button = new Button("Crime Data Graph 2013");
				Button year3Button = new Button("Crime Data Graph 2014");

				mainPanel.add(year1Button);
				mainPanel.add(year2Button);
				mainPanel.add(year3Button);
				final VerticalPanel chartpanel = new VerticalPanel();
				mainPanel.add(chartpanel);

				year1Button.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						chartpanel.clear();
						// Create a column chart visualization.
						ColumnChart chart = new ColumnChart(
								createTable("2012"), createOptions());
						chartpanel.add(chart);
					}
				});

				year2Button.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						chartpanel.clear();
						// Create a column chart visualization.
						ColumnChart chart = new ColumnChart(
								createTable("2013"), createOptions());
						chartpanel.add(chart);
					}
				});

				year3Button.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						chartpanel.clear();
						// Create a column chart visualization.
						ColumnChart chart = new ColumnChart(
								createTable("2014"), createOptions());
						chartpanel.add(chart);
					}
				});

			}
		};

		VisualizationUtils.loadVisualizationApi(onLoadCallback,
				ColumnChart.PACKAGE);
	}

	// <<<<<<<<<<<<<<<<<<<< Map
	// Draw the map 
	private void loadMapApi() {
		boolean sensor = true;
		
		if (tableNeverLoaded) {
			setCrimesToMap(parsedCrimes);
		}

		// load all the libs for use in the maps
		ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
		loadLibraries.add(LoadLibrary.ADSENSE);
		loadLibraries.add(LoadLibrary.DRAWING);
		loadLibraries.add(LoadLibrary.GEOMETRY);
		loadLibraries.add(LoadLibrary.PANORAMIO);
		loadLibraries.add(LoadLibrary.PLACES);
		loadLibraries.add(LoadLibrary.WEATHER);
		loadLibraries.add(LoadLibrary.VISUALIZATION);

		Runnable onLoad = new Runnable() {
			@Override
			public void run() {
				drawHeatMap();
			}
		};

		LoadApi.go(onLoad, loadLibraries, sensor);
	}


	private void drawHeatMap() {
			HeatMapLayerWidget wMap = new HeatMapLayerWidget(plotlatlon(mapCrimesToDisplay));
			wMap.setStyleName("wMap");
			mapPanel.add(wMap);
	}

	// plot the lat lon on the map 
	private JsArray<LatLng> plotlatlon(List<Crime> crimes){
		int i = 0;
		points = new LatLng[crimes.size()];
		if (crimes.isEmpty())
			System.out.println("There is no crimes in the list.");
		else
			for (Crime c: crimes){
				LatLng newpos = LatLng.newInstance(Double.parseDouble(c.getLat()), Double.parseDouble(c.getLon()));
				points[i] = newpos;
				i++;
			}
		JsArray<LatLng> jspoints = ArrayHelper.toJsArray(points);
		return jspoints;
	}
	
	// Map >>>>>>>>>>>>>>>>>>>>

}
