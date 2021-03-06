package beidanci.po;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LearningDictId implements java.io.Serializable {

	// Fields

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "userId", nullable = false)
	private Integer userId;

	@Column(name = "dictId", nullable = false)
	private Integer dictId;

	public LearningDictId() {
	}

	public LearningDictId(Integer userId, Integer dictId) {
		this.userId = userId;
		this.dictId = dictId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof LearningDictId))
			return false;
		LearningDictId castOther = (LearningDictId) other;

		return dictId.equals(castOther.dictId) && userId.equals(castOther.userId);
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + userId.hashCode();
		result = 37 * result + dictId.hashCode();
		return result;
	}

}