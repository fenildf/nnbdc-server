package beidanci;

import java.util.HashSet;
import java.util.List;

import beidanci.po.LearningWord;

public class SessionData {
	public static final String SESSION_DATA = "sessionData";

	public SessionData() {
	}

	private List<LearningWord> todayWords;

	private int currentLearningWordIndex = -1;

	private int currentLearningMode = -1;

	/**
	 * 服务端随机生成的奖励用户的牛粪数
	 */
	private int cowDung;

	/**
	 * 本次会话期间用户评价过的单词图片(用于防止用户多次评价同一个图片，当然， 这样并不能防止用户通过重新登录的方式评价同一图片，程序不刻意阻止用户这样做） )
	 */
	private HashSet<Integer> votedWordImages = new HashSet<Integer>();

	/**
	 * 本次会话期间用户评价过的用户生成例句翻译(用于防止用户多次评价同一个翻译，当然，
	 * 这样并不能防止用户通过重新登录的方式评价同一翻译，程序不刻意阻止用户这样做） )
	 */
	private HashSet<Integer> votedSentenceDiyItems = new HashSet<Integer>();

	/**
	 * 本次会话期间用户评价过的单词英文短描述的中文翻译(用于防止用户多次评价同一个翻译，当然，
	 * 这样并不能防止用户通过重新登录的方式评价同一翻译，程序不刻意阻止用户这样做） )
	 */
	private HashSet<Integer> votedWordShortDescChineses = new HashSet<Integer>();

	public Integer getLastDakaScore() {
		return lastDakaScore;
	}

	public void setLastDakaScore(Integer lastDakaScore) {
		this.lastDakaScore = lastDakaScore;
	}

	/**
	 * 最近一次打卡所获得的积分
	 */
	private Integer lastDakaScore;

	public HashSet<Integer> getVotedWordImages() {
		return votedWordImages;
	}

	public List<LearningWord> getTodayWords() {
		return todayWords;
	}

	public void setTodayWords(List<LearningWord> todayWords) {
		this.todayWords = todayWords;
	}

	public int getCurrentLearningWordIndex() {
		return currentLearningWordIndex;
	}

	public void setCurrentLearningWordIndex(int currentLearningWordIndex) {
		this.currentLearningWordIndex = currentLearningWordIndex;
	}

	public int getCurrentLearningMode() {
		return currentLearningMode;
	}

	public void setCurrentLearningMode(int currentLearningMode) {
		this.currentLearningMode = currentLearningMode;
	}

	public int getCowDung() {
		return cowDung;
	}

	public void setCowDung(int cowDung) {
		this.cowDung = cowDung;
	}

	public HashSet<Integer> getVotedSentenceDiyItems() {
		return votedSentenceDiyItems;
	}

	public HashSet<Integer> getVotedWordShortDescChineses() {
		return votedWordShortDescChineses;
	}
}
