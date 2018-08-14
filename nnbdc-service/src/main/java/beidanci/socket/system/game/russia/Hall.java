package beidanci.socket.system.game.russia;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import beidanci.Global;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.GameHallId;
import beidanci.socket.UserCmd;
import beidanci.socket.system.System;
import beidanci.socket.system.game.russia.state.ReadyState;
import beidanci.socket.system.game.russia.state.WaitState;
import beidanci.store.WordStore;
import beidanci.util.Util;
import beidanci.vo.DictVo;
import beidanci.vo.GameHallVo;
import beidanci.vo.UserVo;
import beidanci.vo.WordVo;

/**
 * 游戏大厅
 *
 * @author Administrator
 */
public class Hall {
	private static Logger logger = LoggerFactory.getLogger(Hall.class);
	private List<RussiaRoom> readyRooms = new ArrayList<RussiaRoom>();
	private List<RussiaRoom> waitingRooms = new ArrayList<RussiaRoom>();
	private SocketService socketService;
	private String name;
	private List<WordVo> wordList;
	private System system;

	/**
	 * 检查游戏室健康状况的定时器
	 */
	private Timer timer = new Timer();

	public Hall(String name, System system, SocketService socketService)
			throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		this.name = name;
		this.socketService = socketService;
		this.system = system;

		logger.info(String.format("正在初始化大厅[%s]", name));

		generateWordList();

		// 启动检查游戏室健康情况的定时任务
		timer.scheduleAtFixedRate(new CheckRussiaRoomTask(readyRooms, waitingRooms, socketService, this), 0, 10000);

		logger.info(String.format("大厅[%s]初始化完成，共有[%d]个单词", name, wordList.size()));
	}

	/**
	 * 为游戏大厅生成相应的单词列表
	 *
	 * @throws EmptySpellException
	 * @throws InvalidMeaningFormatException
	 * @throws ParseException
	 * @throws IOException
	 */
	private void generateWordList()
			throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		// 获取该游戏大厅所包含的所有单词书
		GameHallVo gameHall = Global.getGameHallBO().getGameHallVoById(new GameHallId("russia", name));
		List<DictVo> dicts = gameHall.getDictGroup().getAllDicts();

		// 获取该游戏大厅所包含的单词书中的所有单词
		Map<String, WordVo> words = new HashMap<String, WordVo>();
		for (DictVo dict : dicts) {
			for (String spell : Global.getDictWordBO().getWordSpellsOfDict(dict.getId())) {
				WordVo wordVo = WordStore.getInstance().getWordBySpell(spell);
				words.put(wordVo.getSpell(), wordVo);
			}
		}

		assert (wordList == null);
		wordList = new ArrayList<>(words.values());
	}

	/**
	 * 尝试进入某个已存在且处于等待状态（还缺少一个玩家）的游戏室，如果没有已存在的游戏室或所有游戏室已满，则创建一个新的游戏室并进入其中.
	 *
	 * @return
	 */
	private synchronized RussiaRoom assignRoomForUser(UserVo user, int exceptRoom) throws IllegalAccessException {
		// 尝试进入一个waiting状态的游戏室
		RussiaRoom roomToEnter = null;
		for (RussiaRoom room : waitingRooms) {
			if (room.getId() != exceptRoom) {
				roomToEnter = room;
				break;
			}
		}

		if (roomToEnter == null) {
			roomToEnter = new RussiaRoom(user, this);
		}

		roomToEnter.userEnter(user);

		return roomToEnter;
	}

	/**
	 * 获取指定用户所在的游戏室
	 *
	 * @param user
	 * @return
	 */
	public synchronized RussiaRoom getRoomOfUser(UserVo user) {
		for (Iterator<RussiaRoom> i = waitingRooms.iterator(); i.hasNext();) {
			RussiaRoom room = i.next();
			if (room.hasUser(user)) {
				return room;
			}
		}

		// 尝试从就绪的游戏室中退出,退出后，该游戏室变为等待状态
		for (Iterator<RussiaRoom> i = readyRooms.iterator(); i.hasNext();) {
			RussiaRoom room = i.next();
			if (room.hasUser(user)) {
				return room;
			}
		}

		return null;
	}

	/**
	 * 删除一个游戏室
	 */
	public synchronized void removeRoom(RussiaRoom roomToDel) {
		for (Iterator<RussiaRoom> i = waitingRooms.iterator(); i.hasNext();) {
			RussiaRoom room = i.next();
			if (room.equals(roomToDel)) {
				i.remove();
			}
		}

		for (Iterator<RussiaRoom> i = readyRooms.iterator(); i.hasNext();) {
			RussiaRoom room = i.next();
			if (room.equals(roomToDel)) {
				i.remove();
			}
		}
	}

	public synchronized void onRoomStateChanged(RussiaRoom theRoom) {
		// 首先将room从队列中删除
		removeRoom(theRoom);

		// 更具room的state，将其加入相应队列
		if (theRoom.getState() instanceof WaitState) {
			waitingRooms.add(theRoom);
		} else if (theRoom.getState() instanceof ReadyState) {
			readyRooms.add(theRoom);
		}
	}

	public void sendEvent2User(UserVo user, String event, Object data) {
		socketService.sendEventToUser(user, event, data);
	}

	public void userEnter(UserVo user, int exceptRoom) throws IllegalAccessException {
		RussiaRoom room = assignRoomForUser(user, exceptRoom);
		logger.info(String.format("%s 进入游戏大厅 %s, 房间:[%d,%s]", Util.getNickNameOfUser(user), name, room.getId(),
				room.getState()));
	}

	public void userLeave(UserVo user) throws IllegalAccessException {
		RussiaRoom room = getRoomOfUser(user);
		if (room != null) {
			room.userLeave(user);
		}
		system.onUserLeaveHall(user, this);
		logger.info(String.format("%s 离开游戏大厅 %s", Util.getNickNameOfUser(user), name));
	}

	public void processUserCmd(UserVo user, UserCmd userCmd) throws IllegalAccessException {
		if (userCmd.getCmd().equals("LEAVE_HALL")) {
			userLeave(user);
		} else {
			RussiaRoom room = getRoomOfUser(user);
			if (room != null) {
				room.processUserCmd(user, userCmd);
			}
		}
	}

	public String getName() {
		return name;
	}

	/**
	 * 随机选出一个单词，且与指定单词不同
	 *
	 * @param otherThan
	 * @return
	 */
	public WordVo getWordRandomly(WordVo otherThan) {
		int randomIndex = (int) (wordList.size() * Math.random());
		assert (randomIndex <= wordList.size());
		randomIndex = randomIndex == wordList.size() ? 0 : randomIndex;
		WordVo word = wordList.get(randomIndex);

		if (otherThan != null && otherThan.getSpell().equalsIgnoreCase(word.getSpell())) {
			randomIndex++;
			randomIndex = randomIndex == wordList.size() ? 0 : randomIndex;
			word = wordList.get(randomIndex);
		}

		return word;
	}

	/**
	 * 获取大厅中的人数
	 *
	 * @return
	 */
	public int getUserCount() {
		int count = 0;
		for (RussiaRoom room : readyRooms) {
			count += room.getUsers().size();
		}
		for (RussiaRoom room : waitingRooms) {
			count += room.getUsers().size();
		}
		return count;
	}
}
