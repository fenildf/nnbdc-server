package beidanci.socket.system.chat;

import java.util.HashMap;
import java.util.Map;

import beidanci.socket.SocketService;
import beidanci.util.Util;
import beidanci.vo.UserVo;

public class ChatRoom {
	private Map<Integer, UserVo> users = new HashMap<>();

	public void userEnter(UserVo user) {
		users.put(user.getId(), user);

		// 在聊天室内广播用户进入事件
		broadcast("USER_ENTERED", Util.getNickNameOfUser(user));
		broadcastUserCount();
	}

	public void userLeave(UserVo user) {
		users.remove(user.getId());

		// 在聊天室内广播用户离开事件
		broadcast("USER_LEFT", Util.getNickNameOfUser(user));
		broadcastUserCount();
	}

	private void broadcastUserCount(){
		broadcast("USER_COUNT", users.size());
	}

	private void broadcast(String event, Object data) {
		for (UserVo aUser : users.values()) {
			SocketService.getInstance().sendEventToUser(aUser, event, data);
		}
	}

	public void userSpeak(UserVo user, String content) {
		broadcast("USER_SPEAK", new Object[] { user, content });
	}
}
