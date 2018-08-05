package beidanci.vo;


public class WordShortDescChineseVo extends Vo {

	private Integer id;

	private WordVo word;

	private Integer hand;

	private Integer foot;

	private UserVo author;

	private String content;

	/**
	 * 图片是否被当前登录用户评级过？
	 */
	private Boolean hasBeenVoted;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public WordVo getWord() {
		return word;
	}

	public void setWord(WordVo word) {
		this.word = word;
	}

	public Integer getHand() {
		return hand;
	}

	public void setHand(Integer hand) {
		this.hand = hand;
	}

	public Integer getFoot() {
		return foot;
	}

	public void setFoot(Integer foot) {
		this.foot = foot;
	}

	public UserVo getAuthor() {
		return author;
	}

	public void setAuthor(UserVo author) {
		this.author = author;
	}

	public Boolean getHasBeenVoted() {
		return hasBeenVoted;
	}

	public void setHasBeenVoted(Boolean hasBeenVoted) {
		this.hasBeenVoted = hasBeenVoted;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
