package beidanci.po;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LearningWordId implements java.io.Serializable {

	// Fields

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "userId", nullable = false)
	private Integer userId;

	@Column(name = "wordId", nullable = false)
	private Integer wordId;

	// Constructors

	/**
	 * default constructor
	 */
	public LearningWordId() {
	}

	public LearningWordId(Integer userId, Integer wordId) {
		this.userId = userId;
		this.wordId = wordId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getWordId() {
		return wordId;
	}

	public void setWordId(Integer wordId) {
		this.wordId = wordId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof LearningWordId))
			return false;
		LearningWordId castOther = (LearningWordId) other;

		return wordId.equals(castOther.wordId) && userId.equals(castOther.userId);
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + userId.hashCode();
		result = 37 * result + wordId.hashCode();
		return result;
	}

}