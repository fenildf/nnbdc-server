package beidanci.socket.system.game.russia;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import beidanci.vo.UserGameInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import beidanci.socket.UserCmd;
import beidanci.socket.system.chat.ChatObject;
import beidanci.socket.system.game.russia.state.EmptyState;
import beidanci.socket.system.game.russia.state.ReadyState;
import beidanci.socket.system.game.russia.state.RoomState;
import beidanci.socket.system.game.russia.state.WaitState;
import beidanci.util.Util;
import beidanci.vo.UserGameVo;
import beidanci.vo.UserVo;

public class RussiaRoom {
	private static Logger log = LoggerFactory.getLogger(RussiaRoom.class);

	/**
	 * 新游戏室编号由此值加1生成
	 */
	private static AtomicInteger roomSerialNo = new AtomicInteger(0);

	/**
	 * 游戏室编号
	 */
	private int roomId;

	/**
	 * 游戏室中的用户（两人）及状态
	 */
	private Map<UserVo, UserGameData> users = new ConcurrentHashMap<>();

	/**
	 * 游戏室的当前状态，如果有一个人，为WaitSate；有两个人，为ReadyState; 没有人，为ExistingState
	 */
	private RoomState state;

	/**
	 * 游戏室所属的大厅
	 */
	private Hall hall;

	public RussiaRoom(UserVo user, Hall hall) {
		this.roomId = roomSerialNo.incrementAndGet();
		this.hall = hall;
	}

	/**
	 * 游戏室中用户数发生变化时，调用此函数切换游戏室状态
	 */
	private void onUserCountChanged(UserVo user) throws IllegalAccessException {
		assert (users.size() <= 2);

		if (state != null) {
			state.exit(user);
		}

		if (users.size() == 1) {
			state = new WaitState(this);
		} else if (users.size() == 2) {
			state = new ReadyState(this);
		} else {
			state = new EmptyState(this);
		}
		state.enter();
		hall.onRoomStateChanged(this);
	}

	public RoomState getState() {
		return state;
	}

	public void broadcastEvent(String event, Object data) {
		for (UserVo user : users.keySet()) {
			hall.sendEvent2User(user, event, data);
		}
	}

	/**
	 * 获取游戏室中的另一个用户
	 *
	 * @param user
	 * @return
	 */
	public UserVo getAnotherUser(UserVo user) {
		for (UserVo aUser : users.keySet()) {
			if (!aUser.equals(user)) {
				return aUser;
			}
		}

		return null;
	}

	public void sendEventToUser(UserVo toUser, String event, Object data) {
		hall.sendEvent2User(toUser, event, data);
	}

	/**
	 * 判断用户是否在该游戏室中
	 *
	 * @param user
	 * @return
	 */
	public boolean hasUser(UserVo user) {
		for (UserVo aUser : users.keySet()) {
			if (aUser.equals(user)) {
				return true;
			}
		}
		return false;
	}

	public void userEnter(final UserVo user) throws IllegalAccessException {
		assert (users.size() < 2);

		// 创建用户的游戏数据
		UserGameData userPlayData = new UserGameData(user.getId());
		userPlayData.setMatchStarted(false);
		userPlayData.setExercise(false);
		users.put(user, userPlayData);

		// 向新进入房间的用户发送房间中现存用户的通知
		UserVo existingUser = getAnotherUser(user);
		if (existingUser != null) {
			hall.sendEvent2User(user, "enterRoom",
					new Object[] { existingUser.getId(), Util.getNickNameOfUser(existingUser) });
		}

		// 广播用户进入消息
		broadcastEvent("enterRoom", new Object[] { user.getId(), Util.getNickNameOfUser(user) });

		// 通知用户进入的房间号
		hall.sendEvent2User(user, "roomId", roomId);

		onUserCountChanged(user);
		broadcastUsersInfo();
	}

	public void userLeave(final UserVo user) throws IllegalAccessException {
		for (Iterator<UserVo> i = users.keySet().iterator(); i.hasNext();) {
			UserVo aUser = i.next();
			if (aUser.equals(user)) {
				// 广播用户离开消息
				broadcastEvent("leaveRoom", new Object[] { user.getId(), Util.getNickNameOfUser(user) });

				i.remove();

				onUserCountChanged(user);
			}
		}
		broadcastUsersInfo();
	}

	public void processUserCmd(UserVo user, UserCmd userCmd) throws IllegalAccessException {
		log.info("Processing cmd: " + userCmd);

		if (userCmd.getCmd().equals("CHAT")) {// 聊天命令，直接处理
			broadcastEvent("Chat", new ChatObject(user.getId(), Util.getNickNameOfUser(user), userCmd.getArgs()[0]));
		} else {// 交给当前的State处理
			state.processUserCmd(user, userCmd);
		}

		// 更新用户的状态数据
		UserGameData userPlayData = users.get(user);
		userPlayData.setLastUserCmd(userCmd);
		userPlayData.setLastOperationTime(System.currentTimeMillis());
	}

	public int getId() {
		return roomId;
	}

	public UserGameData getUserPlayData(UserVo user) {
		return users.get(user);
	}

	public Map<UserVo, UserGameData> getUsers() {
		return users;
	}

	/**
	 * 在本游戏室范围内广播所有用户（其实就是两个玩家）的用户信息
	 */
	public void broadcastUsersInfo() {
		for (UserVo user : users.keySet()) {

			// 用户级信息
			UserGameInfo userGameInfo = new UserGameInfo(user.getId());
			userGameInfo.setCowDung(user.getCowDung());
			userGameInfo.setScore(user.getGameScore());
			userGameInfo.setNickName(Util.getNickNameOfUser(user));

			// 游戏级信息
			userGameInfo.setWinCount(0);
			userGameInfo.setLostCount(0);
			for (UserGameVo userGame : user.getUserGames()) {
				if (userGame.getGame().equals("russia")) {
					userGameInfo.setWinCount(userGame.getWinCount());
					userGameInfo.setLostCount(userGame.getLoseCount());
				}
			}

			broadcastEvent("userGameInfo", userGameInfo);
		}

	}

	public Hall getHall() {
		return hall;
	}

}
