package beidanci.vo;

public class MsgVo {
	private int id;
	private String fromUserName;
	private String fromUserNickName;
	private String toUserName;
	private String toUserNickName;
	private String content;
	private String createTime;
	private String createTimeForDisplay; // 形如“一分钟前”、“10秒前”之类
	private String msgType;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFromUserName() {
		return fromUserName;
	}

	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}

	public String getFromUserNickName() {
		return fromUserNickName;
	}

	public void setFromUserNickName(String fromUserNickName) {
		this.fromUserNickName = fromUserNickName;
	}

	public String getToUserName() {
		return toUserName;
	}

	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}

	public String getToUserNickName() {
		return toUserNickName;
	}

	public void setToUserNickName(String toUserNickName) {
		this.toUserNickName = toUserNickName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getCreateTimeForDisplay() {
		return createTimeForDisplay;
	}

	public void setCreateTimeForDisplay(String createTimeForDisplay) {
		this.createTimeForDisplay = createTimeForDisplay;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
}
