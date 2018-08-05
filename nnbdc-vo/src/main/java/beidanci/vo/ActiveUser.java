package beidanci.vo;

public class ActiveUser {
	private String userName;
	private String nickName;

	public ActiveUser(String userName, String nickName) {
		this.userName = userName;
		this.nickName = nickName;
	}

	public String getUserName() {
		return userName;
	}

	public String getNickName() {
		return nickName;
	}
}
