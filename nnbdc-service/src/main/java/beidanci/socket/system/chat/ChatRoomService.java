package beidanci.socket.system.chat;

import java.util.List;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.listener.DataListener;

import beidanci.socket.NamespaceBasedService;
import beidanci.socket.SocketServer;
import beidanci.vo.UserVo;

public class ChatRoomService extends NamespaceBasedService {
	public ChatRoomService(SocketIONamespace namespace, SocketServer socketServer) {
		super(namespace, socketServer);
		initialize();
	}

	private void initialize() {
		namespace.addEventListener("msg", ChatObject.class, new DataListener<ChatObject>() {

			@Override
			public void onData(SocketIOClient client, ChatObject data, AckRequest ackSender) throws Exception {
				// broadcast messages to all clients
				namespace.getBroadcastOperations().sendEvent("msg", data);
			}
		});

	}

	@Override
	protected void onUserLogout(UserVo user) {

	}

	@Override
	protected List<UserVo> getIdleUsers(UserVo except, int count) {
		return null;
	}

	@Override
	protected void onUserLogin(UserVo user) {
	}
}
