package beidanci.socket.system.chat;

public class ChatObject {

	private Integer userId;
	private String nickName;
	private String message;

	public ChatObject() {
	}

	public ChatObject(Integer userId, String nickName, String message) {
		super();
		this.userId = userId;
		this.message = message;
		this.nickName = nickName;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

}
