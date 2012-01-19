package asi.val;

import java.util.HashMap;

public class ForumPost {

	private HashMap<String, String> hiddenValue;

	public ForumPost() {
		setHiddenValue(new HashMap<String, String>());
	}

	public void setHiddenValue(HashMap<String, String> hiddenValue) {
		this.hiddenValue = hiddenValue;
	}

	public void addHiddenValue(String name, String value) {
		if (name != null && name != "" && value != null)
			this.hiddenValue.put(name, value);
	}

	public HashMap<String, String> getHiddenValue() {
		return hiddenValue;
	}

}
