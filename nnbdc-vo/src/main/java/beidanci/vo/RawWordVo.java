package beidanci.vo;

import beidanci.util.Utils;

import java.sql.Timestamp;

public class RawWordVo extends Vo{
	private Integer id;
	private Timestamp createTime;
	private String createManner;
	private WordVo word;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getCreateManner() {
		return createManner;
	}

	public void setCreateManner(String createManner) {
		this.createManner = createManner;
	}

	public WordVo getWord() {
		return word;
	}

	public void setWord(WordVo word) {
		this.word = word;
	}

	public String getSoundUrl() {
		return Utils.getFileNameOfWordSound(word.getSpell());
	}

}
