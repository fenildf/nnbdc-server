package beidanci.socket.system.game.russia;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

import beidanci.Global;
import beidanci.socket.SocketServer;
import beidanci.socket.UserCmd;
import beidanci.socket.system.System;
import beidanci.socket.system.chat.Chat;
import beidanci.socket.system.chat.ChatObject;
import beidanci.util.Util;
import beidanci.vo.UserVo;

public class SocketService {
	private static Logger log = LoggerFactory.getLogger(SocketService.class);
	private static SocketService instance;

	private Map<String, System> systems;

	public static SocketService getInstance() {
		return instance;
	}

	public SocketService(SocketIONamespace namespace, SocketServer socketServer) {
		this.namespace = namespace;
		this.socketServer = socketServer;

		initListeners();

		if (instance != null) {
			throw new RuntimeException("SocketService has been created more than once.");
		}
		instance = this;

		systems = new HashMap<>();
		systems.put(System.SYSTEM_RUSSIA, Russia.getInstance());
		systems.put(System.SYSTEM_CHAT, Chat.getInstance());
	}

	protected void onUserLogout(UserVo user) throws IllegalAccessException {
		for (System sys : systems.values()) {
			sys.onUserLogout(user);
		}
	}

	protected List<UserVo> getIdleUsers(UserVo except, int count) {
		List<UserVo> idleUsers = new ArrayList<>();
		for (System sys : systems.values()) {
			List<UserVo> users = sys.getIdleUsers(except, count);
			for (UserVo user : users) {
				idleUsers.add(user);
				if (idleUsers.size() >= count) {
					return idleUsers;
				}
			}
		}
		return idleUsers;
	}

	protected void onUserLogin(UserVo user) {

	}

	protected SocketIONamespace namespace;

	/**
	 * 本服务的所有在线用户Session，key为user name
	 */
	protected Map<Integer, UUID> sessionsByUser = new ConcurrentHashMap<>();

	/**
	 * 本服务的所有在线用户，key为Session ID
	 */
	protected Map<UUID, UserVo> usersBySession = new ConcurrentHashMap<>();

	/**
	 * 本服务的所有用户socket clients, key为session ID
	 */
	private Map<UUID, SocketIOClient> clientsBySession = new ConcurrentHashMap<UUID, SocketIOClient>();

	private SocketServer socketServer;

	/**
	 * 广播有用户上线了
	 *
	 * @param user
	 */
	public void broadcastUserOnline(UserVo user) {
		namespace.getBroadcastOperations().sendEvent("userOnline", Util.getNickNameOfUser(user));
		broadcastOnelineCount();
	}

	/**
	 * 广播有用户下线了
	 *
	 * @param user
	 */
	public void broadcastUserOffline(UserVo user) {
		namespace.getBroadcastOperations().sendEvent("userOffline", Util.getNickNameOfUser(user));
		broadcastOnelineCount();
	}

	/**
	 * 广播在线用户数量
	 */
	public void broadcastOnelineCount() {
		namespace.getBroadcastOperations().sendEvent("onlineCount", String.valueOf(sessionsByUser.size()));
	}

	/**
	 * 关闭用户的现有连接
	 *
	 * @param user
	 * @return true 表示真正删除发现用户存在现有连接并已将之关闭，false表示用户并没有现有连接
	 */
	public boolean disconnectExistingConnectionOfUser(UserVo user, SocketIOClient newClient, String reason) {
		UUID sessionId = sessionsByUser.get(user.getId());
		if (sessionId != null) {
			SocketIOClient existingClient = clientsBySession.get(sessionId);
			Assert.notNull(existingClient);
			if (existingClient != newClient) {
				log.info(String.format("关闭了用户[%s]的现有连接（%s|%s）, 原因: %s", user.getDisplayNickName(),
						existingClient.getRemoteAddress(), existingClient.getSessionId(), reason));
				existingClient.sendEvent("forceClose", reason);
				existingClient.sendEvent("msg", new ChatObject(Global.getUserBO().getSysUser().getId(), "系统",
						String.format("连接被关闭, 原因: %s", reason)));
				existingClient.disconnect();
				clearUserCache(user.getId(), sessionId);
				return true;
			}
		}
		return false;
	}

	public void addUserCache(UserVo user, SocketIOClient client) {
		UUID sessionId = client.getSessionId();
		sessionsByUser.put(user.getId(), sessionId);
		usersBySession.put(sessionId, user);
		clientsBySession.put(sessionId, client);
		checkCache();
	}

	private void clearUserCache(Integer userId, UUID sessionId) {
		UUID session = (sessionsByUser.remove(userId));
		Assert.isTrue(sessionId.equals(session));

		UserVo user = usersBySession.remove(sessionId);
		Assert.isTrue(userId.equals(user.getId()));

		SocketIOClient client = clientsBySession.remove(sessionId);
		Assert.isTrue(sessionId.equals(client.getSessionId()));
		checkCache();
	}

	private void checkCache() {
		boolean ok = usersBySession.size() == sessionsByUser.size() && usersBySession.size() == clientsBySession.size();
		if (!ok) {
			log.warn(String.format(
					"Cache is not in good staus. usersBySession[%d] sessionsByUser[%d] clientsBySession[%d]",
					usersBySession.size(), sessionsByUser.size(), clientsBySession.size()));
		}
	}

	private void initListeners() {

		namespace.addConnectListener(new ConnectListener() {
			@Override
			public void onConnect(SocketIOClient client) {
				log.info(String.format("Accepted a new connection: %s", client.getRemoteAddress()));

			}
		});

		namespace.addDisconnectListener(new DisconnectListener() {

			@Override
			public void onDisconnect(SocketIOClient client) {
				try {
					UUID sessionId = client.getSessionId();
					UserVo userVo = usersBySession.get(sessionId);
					if (userVo != null) {
						log.info(String.format("与用户[%s]的连接中断！", userVo.getDisplayNickName()));
						clearUserCache(userVo.getId(), sessionId);

						broadcastUserOffline(userVo);
						onUserLogout(userVo);
					}
				} catch (Exception e) {
					log.error("", e);

				}
			}
		});

		namespace.addEventListener("heartBeat", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackSender) {
				try {
					socketServer.onHeartBeatReceived(client);
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});

		namespace.addEventListener("reportUser", Integer.class, new DataListener<Integer>() {
			@Override
			public void onData(SocketIOClient client, Integer theUserId, AckRequest ackSender) {
				try {
					final Integer userId = theUserId;

					// 根据ID查找相应用户
					UserVo user = null;
					try {
						user = Global.getUserBO().getUserVoById(userId);

						if (user == null) {
							log.error(String.format("在数据库中找不到用户【%s】", theUserId));
							return;
						}

					} catch (Exception e) {
						log.error("", e);
					}

					// 关闭用户其他的连接，因为只允许一个用户有一个连接
					if (disconnectExistingConnectionOfUser(user, client, "一个用户不允许多个连接")) {
						broadcastUserOffline(user);
						onUserLogout(user);
					}

					// 保存用户相关信息到缓存
					final UUID sessionId = client.getSessionId();
					if (usersBySession.containsKey(sessionId)) {// 客户端到socket
																// server的连接是长连接，即使客户端切换了登录用户，连接也是一直存在的，所以存在多个用户通过同一个连接上报的情况，此时应将之前登录用户的信息清除
						UserVo oldUser = usersBySession.get(sessionId);
						clearUserCache(oldUser.getId(), sessionId);
					}
					addUserCache(user, client);

					// 向所有玩家广播新用户上线信息
					broadcastUserOnline(user);
					log.info(String.format("用户[%s]上线，在线用户数[%d]", Util.getNickNameOfUser(user), sessionsByUser.size()));
					onUserLogin(user);
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});

		namespace.addEventListener("getIdleUsers", Integer.class, new DataListener<Integer>() {
			@Override
			public void onData(SocketIOClient client, Integer count, AckRequest ackSender) {
				try {
					UserVo user = usersBySession.get(client.getSessionId());
					sendEventToUser(user, "idleUsers", getIdleUsers(user, count));
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});

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

	public void sendEventToUser(UserVo user, String event, Object data) {
		final UUID sessionId = sessionsByUser.get(user.getId());
		if (sessionId != null) {
			SocketIOClient client = clientsBySession.get(sessionId);
			client.sendEvent(event, data);
		}
	}

	/**
	 * Socket Server 通过本函数通知本服务某个session对应的连接被关闭了
	 */
	public void onSessionClosed(UUID sessionId, String reason) {
		UserVo user = usersBySession.get(sessionId);
		if (user != null) {
			disconnectExistingConnectionOfUser(user, null, reason);
		}
	}

	public UserVo getUserById(int userId) {
		for (UserVo user : usersBySession.values()) {
			if (user.getId().equals(userId)) {
				return user;
			}
		}
		return null;
	}

	public List<UserVo> getUsers() {
		return new ArrayList(usersBySession.values());
	}

	public BroadcastOperations getBroadcastOperations() {
		return namespace.getBroadcastOperations();
	}
}
