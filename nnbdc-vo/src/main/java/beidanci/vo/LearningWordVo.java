package beidanci.vo;

import java.util.Date;

import beidanci.util.Utils;

/**
 * Created by Administrator on 2015/12/5.
 */
public class LearningWordVo extends Vo {

	private UserVo user;

	private Date addTime;

	private Integer addDay;

	private Integer lifeValue;

	private Date lastLearningDate;

	private Integer learningOrder;

	private WordVo word;

	public WordVo getWord() {
		return word;
	}

	public void setWord(WordVo word) {
		this.word = word;
	}

	public UserVo getUser() {
		return user;
	}

	public void setUser(UserVo user) {
		this.user = user;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public Integer getAddDay() {
		return addDay;
	}

	public void setAddDay(Integer addDay) {
		this.addDay = addDay;
	}

	public Integer getLifeValue() {
		return lifeValue;
	}

	public void setLifeValue(Integer lifeValue) {
		this.lifeValue = lifeValue;
	}

	public Date getLastLearningDate() {
		return lastLearningDate;
	}

	public void setLastLearningDate(Date lastLearningDate) {
		this.lastLearningDate = lastLearningDate;
	}

	public Integer getLearningOrder() {
		return learningOrder;
	}

	public void setLearningOrder(Integer learningOrder) {
		this.learningOrder = learningOrder;
	}

	public String getSoundPath() {
		return Utils.getFileNameOfWordSound(word.getSpell());
	}
}
