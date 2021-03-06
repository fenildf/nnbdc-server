package beidanci.vo;

/**
 * 保存用户与游戏相关的信息
 *
 * @author Administrator
 */
public class UserGameInfo {
	private Integer userId;

	public UserGameInfo(Integer userId) {
		super();
		this.userId = userId;
	}

	/**
	 * 用户的积分，属于用户级信息，和具体游戏无关
	 */
	private int score;

	/**
	 * 用户的牛粪数
	 */
	private int cowDung;

	private int winCount;

	private int lostCount;

	private String nickName;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getCowDung() {
		return cowDung;
	}

	public void setCowDung(int cowDung) {
		this.cowDung = cowDung;
	}

	public int getWinCount() {
		return winCount;
	}

	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}

	public int getLostCount() {
		return lostCount;
	}

	public void setLostCount(int lostCount) {
		this.lostCount = lostCount;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
}
