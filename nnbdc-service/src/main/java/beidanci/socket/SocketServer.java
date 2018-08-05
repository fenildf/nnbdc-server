package beidanci.socket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

import beidanci.socket.chat.ChatRoomService;
import beidanci.socket.game.russia.RussiaService;
import beidanci.util.SysParamUtil;

public class SocketServer {
	private static Logger log = LoggerFactory.getLogger(SocketServer.class);

	private boolean isStarted = false;
	private SocketIOServer server;
	private List<NamespaceBasedService> services = new ArrayList<NamespaceBasedService>();
	private static SocketServer instance;
	private Map<UUID, SocketClientData> socketIOClients = new ConcurrentHashMap<UUID, SocketClientData>();
	private Timer timer;

	public static SocketServer getInstance() {
		if (instance == null) {
			synchronized (SocketServer.class) {
				if (instance == null) {
					instance = new SocketServer();
				}
			}
		}
		return instance;
	}

	private SocketServer() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new CheckHeartBeatTask(), 0, 5 * 1000);
	}

	private class CheckHeartBeatTask extends TimerTask {

		@Override
		public void run() {

			try {
				for (Iterator<SocketClientData> i = socketIOClients.values().iterator(); i.hasNext();) {
					SocketClientData socketClientData = i.next();
					SocketIOClient socketClient = socketClientData.getSocketIOClient();
					Date lastHeartBeatTime = socketClientData.getLastHeartBeatTime();

					// 15秒没有听到客户端心跳，即杀掉连接
					if (new Date().getTime() - lastHeartBeatTime.getTime() > 15 * 1000) {
						log.info(String.format("心跳超时，关闭连接: %s|%s", socketClient.getRemoteAddress(),
								socketClient.getSessionId()));
						socketClient.disconnect();
						i.remove();

						// 通知上层服务连接已经关闭了
						for (NamespaceBasedService service : services) {
							service.onSessionClosed(socketClient.getSessionId(), "心跳超时");
						}
					}
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}

	}

	public void onHeartBeatReceived(SocketIOClient client) {
		SocketClientData socketClientData = socketIOClients.get(client.getSessionId());
		if (socketClientData != null) {
			socketClientData.setLastHeartBeatTime(new Date());
			socketIOClients.put(client.getSessionId(), socketClientData);
		} else {
			// 这样情况可能是NIO线程被某些操作长时间占用，导致心跳包（可能还不止一个）被堵在TCP层，
			// 在心跳包被程序收到之前，连接就因为心跳超时被关闭了
			log.warn(String.format("收到来自 %s 的心跳连接，但是SocketServer没有该连接的信息", client.getRemoteAddress()));
		}
	}

	public void start() {

		if (isStarted) {
			throw new RuntimeException(this.getClass().getSimpleName() + " already started.");
		}

		Configuration config = new Configuration();
		config.setHostname("127.0.0.1");
		config.setPort(SysParamUtil.getSocketServerPort());

		server = new SocketIOServer(config);

		server.addConnectListener(new ConnectListener() {

			@Override
			public void onConnect(SocketIOClient client) {
				try {
					socketIOClients.put(client.getSessionId(), new SocketClientData(client, new Date()));
					log.info(String.format("新建连接:%s|%s", client.getRemoteAddress(), client.getSessionId()));
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});

		server.addDisconnectListener(new DisconnectListener() {
			@Override
			public void onDisconnect(SocketIOClient client) {
				try {
					log.info(String.format("连接关闭:%s|%s", client.getRemoteAddress(), client.getSessionId()));
					socketIOClients.remove(client.getSessionId());
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});

		final SocketIONamespace commonRoomNamespace = server.addNamespace("/commonRoom");
		services.add(new ChatRoomService(commonRoomNamespace, this));

		final SocketIONamespace englishRoomNamespace = server.addNamespace("/englishRoom");
		services.add(new ChatRoomService(englishRoomNamespace, this));

		final SocketIONamespace russiaNamespace = server.addNamespace("/russia");
		services.add(new RussiaService(russiaNamespace, this));

		server.start();
		isStarted = true;
	}

	public void stop() {
		server.stop();
	}

}
