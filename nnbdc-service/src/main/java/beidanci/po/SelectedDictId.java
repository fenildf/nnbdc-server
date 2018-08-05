package beidanci.po;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SelectedDictId implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "userId", nullable = false)
	private Integer userId;

	@Column(name = "dictId", nullable = false)
	private Integer dictId;

	// Constructors

	/**
	 * default constructor
	 */
	public SelectedDictId() {
	}

	public SelectedDictId(Integer userId, Integer dictId) {
		this.userId = userId;
		this.dictId = dictId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SelectedDictId))
			return false;
		SelectedDictId castOther = (SelectedDictId) other;

		return userId.equals(castOther.userId) && dictId.equals(castOther.dictId);
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + userId.hashCode();
		result = 37 * result + dictId.hashCode();
		return result;
	}

}