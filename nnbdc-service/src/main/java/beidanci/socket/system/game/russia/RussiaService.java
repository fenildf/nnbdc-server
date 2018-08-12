package beidanci.socket.system.game.russia;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.listener.DataListener;

import beidanci.socket.NamespaceBasedService;
import beidanci.socket.SocketServer;
import beidanci.socket.UserCmd;
import beidanci.vo.UserVo;

public class RussiaService extends NamespaceBasedService {
	private static Logger log = LoggerFactory.getLogger(RussiaService.class);
	private static RussiaService instance;

	public static RussiaService getInstance() {
		return instance;
	}

	public RussiaService(SocketIONamespace namespace, SocketServer socketServer) {
		super(namespace, socketServer);
		initialize();

		if (instance != null) {
			throw new RuntimeException("RussiaService has been created more than once.");
		}
		instance = this;
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

                    Russia.getInstance().processUserCmd(user, userCmd);

				} catch (Exception e) {
					log.error("", e);
				}
			}
		});

	}



	@Override
	protected void onUserLogout(UserVo user) throws IllegalAccessException {
		Russia.getInstance().onUserLogout(user);
	}



	@Override
	protected List<UserVo> getIdleUsers(UserVo except, int count) {
		return Russia.getInstance().getIdleUsers(except, count);
	}

	@Override
	protected void onUserLogin(UserVo user) {

	}



}
