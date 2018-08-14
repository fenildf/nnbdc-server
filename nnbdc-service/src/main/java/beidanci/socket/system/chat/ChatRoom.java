package beidanci.socket.system.chat;

import java.util.ArrayList;
import java.util.List;

import beidanci.socket.system.game.russia.SocketService;
import beidanci.util.Util;
import beidanci.vo.UserVo;

public class ChatRoom {
	private List<UserVo> users = new ArrayList<>();

	public void userEnter(UserVo user) {
		users.add(user);

		// 在聊天室内广播用户进入事件
		broadcast("USER_ENTERED", Util.getNickNameOfUser(user));
	}

	private void broadcast(String event, Object data) {
		for (UserVo aUser : users) {
			SocketService.getInstance().sendEventToUser(aUser, event, data);
		}
	}

	public void userSpeak(UserVo user, String content) {
		broadcast("USER_SPEAK", new Object[] { user, content });
	}
}
