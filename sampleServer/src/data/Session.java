package data;

/**
 * class to save the subscriber user along its subscription
 */
public class Session {
	private static Subscriber currentSubscriber;

	/**
	 * methot to set the new subscriber
	 * 
	 * @param subscriber who is the new subscriber
	 */
	public static void setSubscriber(Subscriber subscriber) {
		currentSubscriber = subscriber;
	}

	/**
	 * method to get the current subscriber
	 * 
	 * @return the current subscribed user
	 */
	public static Subscriber getSubscriber() {
		return currentSubscriber;
	}
}
