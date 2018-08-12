package beidanci.socket.system.game.russia;

import beidanci.socket.UserCmd;

/**
 * 保存用户在游戏中的状态和数据
 *
 * @author Administrator
 */
public class UserGameData {
	public UserGameData(Integer userId) {
		this.userId = userId;
		lastOperationTime = System.currentTimeMillis();
	}

	private Integer userId;

	/**
	 * 用户是否按下了【开始】按钮
	 */
	private boolean isMatchStarted;

	/**
	 * 是否是否处于练习状态
	 */
	private boolean isExercise;

	/**
	 * 用户或客户端最后一次下达的命令
	 */
	private UserCmd lastUserCmd;

	/**
	 * 记录用户最后一个操作的时间
	 */
	private long lastOperationTime;

	/**
	 * 用户每种道具的数量
	 */
	private int[] propsCounts = new int[2];

	/**
	 * 连续答对次数
	 */
	private int correctCount;

	public long getLastOperationTime() {
		return lastOperationTime;
	}

	public void setLastOperationTime(long lastOperationTime) {
		this.lastOperationTime = lastOperationTime;
	}

	public Integer getUserId() {
		return userId;
	}

	public UserCmd getLastUserCmd() {
		return lastUserCmd;
	}

	public void setLastUserCmd(UserCmd lastUserCmd) {
		this.lastUserCmd = lastUserCmd;
	}

	public int getCorrectCount() {
		return correctCount;
	}

	public void setCorrectCount(int correctCount) {
		this.correctCount = correctCount;
	}

	public int[] getPropsCounts() {
		return propsCounts;
	}

	public boolean isMatchStarted() {
		return isMatchStarted;
	}

	public void setMatchStarted(boolean isMatchStarted) {
		this.isMatchStarted = isMatchStarted;
		if (isMatchStarted) {
			isExercise = false;
		}
	}

	public boolean isExercise() {
		return isExercise;
	}

	public void setExercise(boolean isExercise) {
		this.isExercise = isExercise;

		if (isExercise) {
			isMatchStarted = false;
		}
	}
}
