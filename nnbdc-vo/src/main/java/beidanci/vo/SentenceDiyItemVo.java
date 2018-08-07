package beidanci.vo;

import java.util.List;

public class SentenceDiyItemVo extends Vo {
	private int id;
	private UserVo author;
	private String itemType;
	private String content;
	private int handCount;
	private int footCount;
	private List<SentenceDiyItemRemarkVo> sentenceDiyItemRemarks;

	/**
	 * 是否被当前登录用户评级过？
	 */
	private Boolean hasBeenVoted;

	public SentenceDiyItemVo() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getHandCount() {
		return handCount;
	}

	public void setHandCount(int handCount) {
		this.handCount = handCount;
	}

	public int getFootCount() {
		return footCount;
	}

	public void setFootCount(int footCount) {
		this.footCount = footCount;
	}

	public List<SentenceDiyItemRemarkVo> getSentenceDiyItemRemarks() {
		return sentenceDiyItemRemarks;
	}

	public void setSentenceDiyItemRemarks(List<SentenceDiyItemRemarkVo> sentenceDiyItemRemarks) {
		this.sentenceDiyItemRemarks = sentenceDiyItemRemarks;
	}

	public Boolean getHasBeenVoted() {
		return hasBeenVoted;
	}

	public void setHasBeenVoted(Boolean hasBeenVoted) {
		this.hasBeenVoted = hasBeenVoted;
	}

	public UserVo getAuthor() {
		return author;
	}

	public void setAuthor(UserVo author) {
		this.author = author;
	}
}
