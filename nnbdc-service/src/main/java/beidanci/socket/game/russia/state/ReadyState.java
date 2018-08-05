package beidanci.socket.game.russia.state;

import org.apache.commons.lang.NotImplementedException;

import beidanci.Global;
import beidanci.po.SysParam;
import beidanci.po.User;
import beidanci.socket.game.cmd.UserCmd;
import beidanci.socket.game.russia.RussiaRoom;
import beidanci.socket.game.russia.UserGameData;
import beidanci.util.Util;
import beidanci.vo.UserVo;

/**
 * 当游戏室有两人时，即进入Ready State.
 *
 * @author Administrator
 */
public class ReadyState extends RoomState {

	private GetNextWordProcessor getNextWordProcessor;
	private GameOverProcessor gameOverProcessor;
	private StartExerciseProcessor startExerciseProcessor;

	/**
	 * 游戏是否正在进行中
	 */
	boolean isPlaying;

	public ReadyState(RussiaRoom room) {
		super(room);
		getNextWordProcessor = new GetNextWordProcessor(room);
		gameOverProcessor = new GameOverProcessor(room);
		startExerciseProcessor = new StartExerciseProcessor(room);
	}

	@Override
	public void enter() {
		room.broadcastEvent("enterReady", null);
	}

	public void processUserCmd(UserVo user, UserCmd userCmd) throws IllegalAccessException {
		if (userCmd.getCmd().equals("GET_NEXT_WORD")) {
			getNextWordProcessor.processGetNextWordCmd(user, userCmd);
		} else if (userCmd.getCmd().equals("START_EXERCISE")) {// 单人练习命令
			getNextWordProcessor.reset();
			gameOverProcessor.reset();
			startExerciseProcessor.process(user, userCmd);
		} else if (userCmd.getCmd().equals("GAME_OVER")) {
			isPlaying = false;
			gameOverProcessor.processGameOverCmd(user, userCmd);
		} else if (userCmd.getCmd().equals("START_GAME")) {
			processStartGameCmd(user);
		} else if (userCmd.getCmd().equals("USE_PROPS")) {
			processUsePropsCmd(user, Integer.parseInt(userCmd.getArgs()[0]));
		} else {
			throw new NotImplementedException(String.format("Don't support cmd[%s]", userCmd.getCmd()));
		}

	}

	private void processUsePropsCmd(UserVo user, int props) {
		UserGameData playData = room.getUserPlayData(user);
		if (playData.getPropsCounts()[props] > 0) {
			playData.getPropsCounts()[props]--;
			room.broadcastEvent("propsUsed", new Object[] { user.getId(), props, playData.getPropsCounts()[props],
					Util.getNickNameOfUser(user) });
		}
	}

	private void processStartGameCmd(UserVo user) throws IllegalAccessException {
		// 获取系统配置（每局游戏需要支付的牛粪数）
		SysParam sysParam = Global.getSysParamBO().findById(SysParam.COW_DUNG_PER_GAME);
		final int cowDungPerGame = Integer.parseInt(sysParam.getParamValue());

		// 检查用户是否有足够的牛粪
		if (user.getCowDung() < cowDungPerGame) {
			room.sendEventToUser(user, "noEnoughCowDung", cowDungPerGame);
			return;
		}

		// 设置用户的游戏状态为“开始”
		UserGameData userPlayData1 = room.getUserPlayData(user);
		userPlayData1.setMatchStarted(true);
		room.broadcastEvent("userStarted", user.getId());

		// 获取另一位玩家的游戏状态信息
		UserVo anotherUser = room.getAnotherUser(user);
		UserGameData userPlayData2 = room.getUserPlayData(anotherUser);

		// 如果两个用户都点击了【开始】按钮，则开始新游戏
		if (userPlayData1.isMatchStarted() && userPlayData2.isMatchStarted()) {
			// 复位用户的游戏状态信息
			userPlayData1.setMatchStarted(false);
			userPlayData1.setCorrectCount(0);
			userPlayData1.getPropsCounts()[0] = 0;
			userPlayData1.getPropsCounts()[1] = 0;
			userPlayData2.setMatchStarted(false);
			userPlayData2.setCorrectCount(0);
			userPlayData2.getPropsCounts()[0] = 0;
			userPlayData2.getPropsCounts()[1] = 0;

			// 复位游戏状态
			gameOverProcessor.reset();
			getNextWordProcessor.reset();

			room.broadcastEvent("sysCmd", "BEGIN");
			isPlaying = true;

			// 两位玩家各扣除若干牛粪（按照系统配置）
			for (UserVo userVo : room.getUsers().keySet()) {
				User user2 = Global.getUserBO().findById(userVo.getId());
				Global.getUserBO().adjustCowDung(user2, cowDungPerGame * (-1), "游戏开始时扣除的牛粪");
			}

			room.broadcastUsersInfo();
		}
	}

	@Override
	public void exit(UserVo user) throws IllegalAccessException {
		// 游戏正在进行中，用户退出，判为输家，另一方判为赢家
		if (isPlaying) {
			assert (room.getUsers().size() == 1);
			UserVo winer = room.getUsers().keySet().toArray(new UserVo[0])[0];
			UserVo loser = user;
			room.broadcastEvent("loser", loser.getId());
			gameOverProcessor.adjustUserScore(winer, loser);
			isPlaying = false;
			room.broadcastUsersInfo();
		}
	}

}
