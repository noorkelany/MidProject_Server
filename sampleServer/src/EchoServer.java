import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import data.Login;
import data.Manager;
import data.MonthlyParkingEntry;
import data.Order;
import data.ParkingSpotReportEntry;
import data.ResponseWrapper;
import data.Subscriber;
import data.SystemStatus;
import data.UpdateOrderDetails;
import data.Worker;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class EchoServer extends AbstractServer {
	/**
	 * The default port to listen on.
	 */
	final public static int DEFAULT_PORT = 5555;
	private mysqlConnection instance = null;
	Connection conn = null;
	private reservationController reservationController;
	private final ServerController controller;
	private LoginController loginController;
	private RegisterController registerController;
	private parkingSpotController parkingSpotController;
	private ReceivingCarController receivingCarController;
	private OrderValidator orderValidator;
	ResponseWrapper responseToClient;
	private parkingSubscriberController carDeliveryController;
	private SubscriberController subscriberController;
	private parkingSubscriberController parkingController;
	private MonthlyReportController monthlyReportController;
	private ParkingSpotReportController parkingSpotReportController;

	public EchoServer(int port, ServerController controller) {
		super(port);
		reservationController = new reservationController();
		registerController = new RegisterController();
		this.loginController = new LoginController();
		this.controller = controller;
		this.parkingSpotController = new parkingSpotController();
		this.receivingCarController = new ReceivingCarController();
		this.orderValidator = new OrderValidator();
		carDeliveryController = new parkingSubscriberController();
		this.parkingSpotController = new parkingSpotController();
		subscriberController = new SubscriberController();
		this.parkingController = new parkingSubscriberController();
		this.monthlyReportController = new MonthlyReportController();
		this.parkingSpotReportController = new ParkingSpotReportController();
		// this.getDBConnection();
	}

	//
	/**
	 * Client connection methods - change status to connected
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
		String ip = client.getInetAddress().getHostAddress();
		String host = client.getInetAddress().getHostName();
		controller.updateClientStatus(ip, host, "Connected");
	}

	/**
	 * Client connection methods - change status to disconnected
	 */
	@Override
	synchronized protected void clientDisconnected(ConnectionToClient client) {
		System.out.println("Client disconnected");
		String ip = client.getInetAddress().getHostAddress();
		String host = client.getInetAddress().getHostName();
		controller.updateClientStatus(ip, host, "Disconnected");
	}

	// Instance methods ************************************************
	public void getDBConnection() {
		try {
			instance = mysqlConnection.getInstance();
			conn = instance.getConnection();
		} catch (Exception e) {
			System.out.println("Error connecting to DB");
		}
	}

	/**
	 * method for handling client message (by contacting with other controllers)
	 */
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		try {
			// Generate Report 1
			if (msg instanceof String && msg.equals("GET_MONTHLY_REPORT")) {
				String currentMonth = LocalDate.now().getYear() + "-"
						+ String.format("%02d", LocalDate.now().getMonthValue());
				ReportFetcher fetcher = new ReportFetcher();
				ArrayList<MonthlyParkingEntry> report = fetcher.getMonthlyReport(currentMonth);
				client.sendToClient(report);
			}
			else if (msg instanceof String strMsg && strMsg.equals("GET_PARKING_SPOT_REPORT")) {
				ArrayList<ParkingSpotReportEntry> reportList = new ArrayList<>();

				try (Connection conn = mysqlConnection.getInstance().getConnection()) {
					String month = java.time.LocalDate.now().withDayOfMonth(1).toString().substring(0, 7); // e.g. "2025-06"
					String sql = "SELECT * FROM parkingspotsreport WHERE month = ?";
					PreparedStatement ps = conn.prepareStatement(sql);
					ps.setString(1, month);
					ResultSet rs = ps.executeQuery();

					while (rs.next()) {
						int code = rs.getInt("parkingCode");
						int regular = rs.getInt("regularDuration");
						int extended = rs.getInt("extendedDuration");
						int late = rs.getInt("lateReturns");

						reportList.add(new ParkingSpotReportEntry(code, regular, extended, late));
					}
					client.sendToClient(reportList);
					System.out.println("✅ Sent parking spot report (" + reportList.size() + " entries) to client.");
				} catch (Exception e) {
					System.err.println("❌ Failed to fetch parking spot report: " + e.getMessage());
					e.printStackTrace();
				}
			}

			// check if order with id exists
			if (msg instanceof String && msg.toString().startsWith("OrderID")) {
				int ID;
				String str;
				str = msg.toString();
				String[] result = str.split("\\s");
				ID = Integer.parseInt(result[1]);
				Order ord = reservationController.getOrderByID(ID);
				if (ord != null) {
					client.sendToClient(ord);
				} else {
					client.sendToClient(null);
				}

			}

			// get all existing orders
			if (msg.equals("showAllOrders")) {
				ArrayList<Order> orders = reservationController.getAllOrders();
				client.sendToClient(orders);
			}
			// update details of order
			if (msg instanceof UpdateOrderDetails) {
				UpdateOrderDetails order = (UpdateOrderDetails) msg;
				client.sendToClient(reservationController.updateOrder(order));
			}
			if (msg.equals("connect")) {
				String ip = client.getInetAddress().getHostAddress();
				String host = client.getInetAddress().getHostName();
				controller.updateClientStatus(ip, host, "Connected");
			}

			if (msg.equals("disconnect")) {
				String ip = client.getInetAddress().getHostAddress();
				String host = client.getInetAddress().getHostName();
				controller.updateClientStatus(ip, host, "Disconnected");
			}
			// send connection info
			if (msg.equals("clientDetails")) {
				String IP = client.getInetAddress().getHostAddress();
				String hostName = client.getInetAddress().getHostName();
				String str = "Client connected:\n" + "IP Address:" + IP + "\nHost Name: " + hostName;

				client.sendToClient(str);
			}

			// login process
			if (msg instanceof ResponseWrapper) {

				ResponseWrapper response = (ResponseWrapper) msg;

				/*
				 * Login details = (Login) response.getData(); String role =
				 * loginController.validateLogin(details.getUsername(), details.getPasswrod());
				 * ResponseWrapper responseToClient = new ResponseWrapper("LOGIN_RESPONSE",
				 * role); // role = "worker" / // "subscriber" / "invalid"
				 * client.sendToClient(responseToClient);
				 */

				switch (response.getType()) {
				case "CHECK_USER_CONFORMATION_CODE":
					Order order1 = mysqlConnection.getOrderByConfirmationCode((String) response.getData());
					if(order1 != null) {
						Subscriber sub =  mysqlConnection.getSubscriberByCode(order1.getSubscriber_id());
						ResponseWrapper respone  = new ResponseWrapper("CORRECT_CONFORMATION_CODE",order1,sub);
						client.sendToClient(respone);
					}else {
						ResponseWrapper respone = new ResponseWrapper("NULL",null);
						client.sendToClient(respone);
					}

					break;
				case "parkingActiveList":
					ArrayList<Order> parkingList = parkingController.getActiveSubscriberParking();
					client.sendToClient(parkingList);
					break;
				case "subscriberList":
					ArrayList<Subscriber> subscribersList = subscriberController.getAllSubscribers();
					client.sendToClient(subscribersList);

					break;
				case "DeliveryButtonClicked":
					Order order = (Order) response.getData();

					SystemStatus status = parkingSubscriberController.handleCarDelivery(order);

					if (status == SystemStatus.SUCCESS_DELIVERY) {

						int parkingCode = order.getParking_space();
						order.setConfirmation_code(parkingCode);

						client.sendToClient(new ResponseWrapper("SUCCESS_DELIVERY", order));
					} else {

						client.sendToClient(status);
					}
					break;
				case "PARKINGCODE":
					// emptyRelevantParkingSpot
					boolean codeExists = parkingSpotController.findParkingCode(response);
					ResponseWrapper reply = new ResponseWrapper("EMPTYPARKINGSPACE", codeExists);
					client.sendToClient(reply);
					break;


				case "LOGIN":
					Login details = (Login) response.getData();
					details = (Login) response.getData();
					String role = loginController.validateLogin(details.getUsername(), details.getPasswrod());

					if (role.equals("subscriber")) {
						Subscriber sub = loginController.getSubscriberByUsername(details.getUsername());
						responseToClient = new ResponseWrapper("LOGIN_RESPONSE", role, sub);
						client.sendToClient(responseToClient);
					} else if(role.equals("manager")) {
						Manager manager = loginController.getManagerByUsername(details.getUsername());
						responseToClient = new ResponseWrapper("LOGIN_RESPONSE", role, manager);
						client.sendToClient(responseToClient);
					}else if(role.equals("worker")) {
						Worker worker = loginController.getWorkerByUsername(details.getUsername());
						responseToClient = new ResponseWrapper("LOGIN_RESPONSE", role, worker);
						client.sendToClient(responseToClient);
					}else {
						responseToClient = new ResponseWrapper("invalid", "invalid");
						client.sendToClient(responseToClient);
					}
					break;
					
				case "SubscriberHistory":
				    try {
				        int subscriberId = (int) response.getData();

				        List<Order> orders = SubscribermainPageController.getOrdersBySubscriber(subscriberId);

				        client.sendToClient(new ResponseWrapper("SubscriberHistoryResult", orders));
				    } catch (Exception e) {
				        System.err.println("Failed to retrieve subscriber history: " + e.getMessage());
				        e.printStackTrace();
				        client.sendToClient(new ResponseWrapper("SubscriberHistoryResult", new ArrayList<>()));
				    }

				    break;
				    
				case "SubscriberParkingStatus":
				    try {
				        int subscriberId = (int) response.getData();
				        List<Order> parkingStatus = SubscribermainPageController.getOrdersFromSubscriberParking(subscriberId);
				        client.sendToClient(new ResponseWrapper("SubscriberParkingStatusResult", parkingStatus));
				    } catch (Exception e) {
				        System.err.println("❌ Failed to retrieve subscriber parking status: " + e.getMessage());
				        e.printStackTrace();
				        client.sendToClient(new ResponseWrapper("SubscriberParkingStatusResult", new ArrayList<>()));
				    }
				    break;
				    
				case "UpdateSubscriber":
				    Subscriber updatedSub = (Subscriber) response.getData();
				    boolean updated = subscriberController.updateSubscriber(updatedSub);
				    client.sendToClient(new ResponseWrapper("UpdateSubscriberResult", updated));
				    break;

					/*
					 * if ("subscriber".equals(role)) { Subscriber sub =
					 * loginController.getSubscriberByUsername(details.getUsername());
					 * responseToClient = new ResponseWrapper("LOGIN_RESPONSE", role, sub);
					 * client.sendToClient(responseToClient); }
					 */

				case "getParkingCodeForSubscriber":
					int parkingCode = receivingCarController.getParkingCodeForSubscriber(response);
					System.out.println("Server " + parkingCode);
					ResponseWrapper rsp = new ResponseWrapper("RetreivedParkingCode", parkingCode);
					client.sendToClient(rsp);
					break;

				case "CHECK_USER_CODE":
					String code = (String) response.getData();
					parkingSubscriberController controller = new parkingSubscriberController();
					controller.getDBConnection();
					boolean correct = controller.checkSubscriptionExists(code);
					if (!correct) {
						client.sendToClient(new ResponseWrapper("CHECK_USER_CODE_RESULT", "Invalid Code"));
					} else {
						int Espot = controller.checkEmptyParkingSpots();
						if (Espot == -1)
							client.sendToClient(new ResponseWrapper("CHECK_USER_CODE_RESULT", "FULL"));
						else {
							client.sendToClient(new ResponseWrapper("CHECK_USER_CODE_RESULT", "valid Code"));
						}
					}
					break;
				case "EXTEND_PARKING_TIME":
					// check parking spot that subscribers occupies
					// check if it's available (according to orders) for the required time
					// if there are less hours it will tell the user to pick another one,otherwise
					// we will just
					// extends the time
					String answer = this.receivingCarController.extendParkingTime(response);
					ResponseWrapper timeRsp = new ResponseWrapper("TIME_REQUEST_ANSWER", answer);
					client.sendToClient(timeRsp);
					break;
				case "DELIVERYCAR":
					Order incomingOrder = (Order) response.getData();
					if(incomingOrder.getConfirmation_code() != 0) {
						SystemStatus result = parkingSubscriberController.handleCarDeliveryWithConfirmationCode(incomingOrder);
						if (result == SystemStatus.SUCCESS_DELIVERY) {
							client.sendToClient(new ResponseWrapper("SUCCESS_DELIVERY", incomingOrder));
						} else {
							client.sendToClient(result);
						}
					}else {
						//
						// find empty spot
						int spot = carDeliveryController.checkEmptyParkingSpots();

						if (spot == -1) {
							client.sendToClient(SystemStatus.NO_PARKING_SPOT);
							break;
						}

						// create confirmation code
						int confirmationCode = generateConfirmationCode(spot);

						// update order
						incomingOrder.setParking_space(spot);
						incomingOrder.setConfirmation_code(confirmationCode);

						// save data to db
						SystemStatus result = parkingSubscriberController.handleCarDelivery(incomingOrder);

						if (result == SystemStatus.SUCCESS_DELIVERY) {
							client.sendToClient(new ResponseWrapper("SUCCESS_DELIVERY", incomingOrder));
						} else {
							client.sendToClient(result);
						}
					}
					break;
				case "REQUEST_ORDER":
					// check if there are 40% free available spots
					// according to the time and date
					/*
					 * ResponseWrapper timeRequest = new ResponseWrapper("TIME", startTime,endTime);
					 * ResponseWrapper request = new ResponseWrapper("REQUEST_ORDER",
					 * date,timeRequest);
					 */
					ResponseWrapper time = (ResponseWrapper) response.getExtra();
					boolean available = this.orderValidator.isOverallAvailabilitySufficient(
							(LocalDate) response.getData(), (LocalTime) time.getData(), (LocalTime) time.getExtra());
					ResponseWrapper parkingList1 = new ResponseWrapper("PARKING_LIST", null);
					if (!available) {
						System.out.println("null");
						parkingList1.setData(null);

					} else {
						Map<Integer, Boolean> parkingSpotsStatus = OrderValidator.getAllSpotsStatus(
								(LocalDate) response.getData(), (LocalTime) time.getData(),
								(LocalTime) time.getExtra());
						System.out.println("parking spot status map");
						parkingList1.setData(parkingSpotsStatus);

					}
					client.sendToClient(parkingList1);
					break;

				case "ORDER_DETAILS":
					client.sendToClient(OrderValidator.insertNewOrder(response));
					break;
				}
			}
			// new subscriber process
			if (msg instanceof Subscriber) {
				Subscriber subscriber = (Subscriber) msg;
				System.out.println("new subscriber");

				String result = registerController.registerNewSubscriber(subscriber);
				client.sendToClient(result); // send result back to client
				client.sendToClient(result); // send result back to client
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * starts listening for connections.
	 */
	protected void serverStarted() {
		System.out.println("Server listening for connections on port " + getPort());
		// ParkingMonitor.startMonitoring();
		//ParkingMonitor.startMonitoringLateOrders();
		System.out.println("Timer activated");
		// report generation
		ParkingMonitor.startMonthlyReportScheduler(monthlyReportController);

		ParkingMonitor.scheduleParkingSpotReport(parkingSpotReportController);

	}

	/**
	 * This method overrides the one in the superclass. Called when the server stops
	 * listening for connections.
	 */
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
	}

	// Class methods ***************************************************

	/**
	 * This method is responsible for the creation of the server instance (there is
	 * no UI in this phase).
	 *
	 * @param args[0] The port number to listen on. Defaults to 5555 if no argument
	 *                is entered.
	 */
	private int generateConfirmationCode(int parkingCode) {
		return parkingCode * 73 + 1234;
	}
	// In handle message from client - take care of the msg according to its type
	// (arrayList of students and so on)
	// 2 functions of require executing query from the DB
	// function for connected Client query IP, host address and connection status
	// Order class with relevant properties

}
