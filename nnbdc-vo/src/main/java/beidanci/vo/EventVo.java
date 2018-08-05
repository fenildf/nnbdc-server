package beidanci.vo;

public class EventVo {

	private Integer id;

	private TenseType eventType;

	private UserVo user;

	private WordImageVo wordImage;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TenseType getEventType() {
		return eventType;
	}

	public void setEventType(TenseType eventType) {
		this.eventType = eventType;
	}

	public UserVo getUser() {
		return user;
	}

	public void setUser(UserVo user) {
		this.user = user;
	}

	public WordImageVo getWordImage() {
		return wordImage;
	}

	public void setWordImage(WordImageVo wordImage) {
		this.wordImage = wordImage;
	}
}
