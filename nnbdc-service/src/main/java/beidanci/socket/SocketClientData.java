package beidanci.socket;

import java.util.Date;

import com.corundumstudio.socketio.SocketIOClient;

/**
 * 保存SocketClient相关信息
 *
 * @author Administrator
 */
public class SocketClientData {
	private SocketIOClient socketIOClient;

	public SocketClientData(SocketIOClient socketIOClient, Date lastHeartBeatTime) {
		super();
		this.socketIOClient = socketIOClient;
		this.lastHeartBeatTime = lastHeartBeatTime;
	}

	Date lastHeartBeatTime;

	public SocketIOClient getSocketIOClient() {
		return socketIOClient;
	}

	public void setSocketIOClient(SocketIOClient socketIOClient) {
		this.socketIOClient = socketIOClient;

	}

	public Date getLastHeartBeatTime() {
		return lastHeartBeatTime;
	}

	public void setLastHeartBeatTime(Date lastHeartBeatTime) {
		this.lastHeartBeatTime = lastHeartBeatTime;
	}
}
