package data;

import java.io.Serializable;

/**
 * Class for sending general messages each methos contains type and data
 */
public class ResponseWrapper implements Serializable {
	private static final long serialVersionUID = 1L;
	private String type;
	private Object data;
	private Object extra;

	/**
	 * @param type type of the msg (to recognize it in the server handle method)
	 * @param data data that will be transfared and used later
	 */
	public ResponseWrapper(String type, Object data) {
		this.type = type;
		this.data = data;
	}

	public ResponseWrapper(String type, Object data, Object extra) {
		this.type = type;
		this.data = data;
		this.extra = extra;
	}

	public Object getExtra() {
		return extra;
	}

	/**
	 * @return type of the general message
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return data of the general message
	 */
	public Object getData() {
		return data;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void setExtra(Object extra) {
		this.extra = extra;
	}

	/**
	 * toString method for printing the message details
	 */
	@Override
	public String toString() {
		return "ResponseWrapper [type=" + type + ", data=" + data + "]";
	}

}
