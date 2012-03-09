package Model;

public class Friend {
	String targetName;
	String password;
	
	public Friend(String targetName, String password) {
		this.targetName = targetName;
		this.password = password;
	}
	
	public String getTargetName() {
		return targetName;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
