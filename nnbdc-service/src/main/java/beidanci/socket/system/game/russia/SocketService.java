package beidanci.socket.system.game.russia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.listener.DataListener;

import beidanci.socket.NamespaceBasedService;
import beidanci.socket.SocketServer;
import beidanci.socket.UserCmd;
import beidanci.socket.system.System;
import beidanci.socket.system.chat.Chat;
import beidanci.vo.UserVo;

public class SocketService extends NamespaceBasedService {
	private static Logger log = LoggerFactory.getLogger(SocketService.class);
	private static SocketService instance;

	private Map<String, System> systems;

	public static SocketService getInstance() {
		return instance;
	}

	public SocketService(SocketIONamespace namespace, SocketServer socketServer) {
		super(namespace, socketServer);
		initialize();

		if (instance != null) {
			throw new RuntimeException("SocketService has been created more than once.");
		}
		instance = this;

		systems = new HashMap<>();
		systems.put(System.SYSTEM_RUSSIA, Russia.getInstance());
		systems.put(System.SYSTEM_CHAT, Chat.getInstance());
	}

	private void initialize() {
		namespace.addEventListener("userCmd", UserCmd.class, new DataListener<UserCmd>() {
			@Override
			public void onData(SocketIOClient client, UserCmd userCmd, AckRequest ackSender) {
				try {
					UserVo user = usersBySession.get(client.getSessionId());
					if (user == null) {// 找不到与session ID对应的用户，说明用户尚未上报，这种情况可能出现在服务端重启后
						return;
					}
					assert (userCmd.getUserId().equals(user.getId()));

					System system = systems.get(userCmd.getSystem());
					system.processUserCmd(user, userCmd);
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});

	}

	@Override
	protected void onUserLogout(UserVo user) throws IllegalAccessException {
		for (System sys : systems.values()) {
			sys.onUserLogout(user);
		}
	}

	@Override
	protected List<UserVo> getIdleUsers(UserVo except, int count) {
		List<UserVo> idleUsers = new ArrayList<>();
		for (System sys : systems.values()) {
			idleUsers.addAll(sys.getIdleUsers(except, count));
		}
		return idleUsers;
	}

	@Override
	protected void onUserLogin(UserVo user) {

	}

}
