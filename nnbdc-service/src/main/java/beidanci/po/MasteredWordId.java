package beidanci.po;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class MasteredWordId implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "userId", nullable = false)
	private Integer userId;

	@Column(name = "wordId", nullable = false)
	private Integer wordId;

	// Constructors

	/** default constructor */
	public MasteredWordId() {
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof MasteredWordId))
			return false;
		MasteredWordId castOther = (MasteredWordId) other;

		return wordId.equals(castOther.wordId) && userId.equals(castOther.userId);
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + userId.hashCode();
		result = 37 * result + wordId.hashCode();
		return result;
	}

}