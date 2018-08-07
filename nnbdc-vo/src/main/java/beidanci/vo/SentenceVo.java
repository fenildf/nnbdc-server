package beidanci.vo;

import java.util.List;

/**
 * For example, this is a example sentence:
 * <p>
 * On the face of it, this story seems unconvincing. <br>
 * 表面上看来，这个故事似乎令人难以置信。
 *
 * @author Administrator
 */
public class SentenceVo extends Vo {

	private Integer id;

	private String english;

	private String englishDigest;

	private String chinese;

	private String theType;

	private boolean soundFileExists;

	private List<SentenceDiyItemVo> sentenceDiyItems;

	public SentenceVo() {

	}

	public SentenceVo(Integer id, String english, String chinese, String type, String englishDigest,
			List<SentenceDiyItemVo> sentenceDiyItems) {
		this.id = id;
		this.english = english;
		this.englishDigest = englishDigest;
		this.chinese = chinese;
		this.theType = type;
		this.sentenceDiyItems = sentenceDiyItems;
	}

	@Override
	public String toString() {
		return String.format("{%s %s}", english, chinese);
	}

	public String getEnglish() {
		return english;
	}

	public void setEnglish(String english) {
		this.english = english;
	}

	public String getChinese() {
		return chinese;
	}

	public void setChinese(String chinese) {
		this.chinese = chinese;
	}

	public String getTheType() {
		return theType;
	}

	public void setTheType(String type) {
		this.theType = type;
	}

	public String getEnglishDigest() {
		return englishDigest;
	}

	public void setEnglishDigest(String englishDigest) {
		this.englishDigest = englishDigest;
	}

	public boolean getSoundFileExists() {
		return soundFileExists;
	}

	public void setSoundFileExists(boolean soundFileExists) {
		this.soundFileExists = soundFileExists;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<SentenceDiyItemVo> getSentenceDiyItems() {
		return sentenceDiyItems;
	}

	public void setSentenceDiyItems(List<SentenceDiyItemVo> sentenceDiyItems) {
		this.sentenceDiyItems = sentenceDiyItems;
	}

}
