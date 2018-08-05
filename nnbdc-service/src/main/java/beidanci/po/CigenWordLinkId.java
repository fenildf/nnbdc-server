package beidanci.po;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@SuppressWarnings("serial")
public class CigenWordLinkId implements java.io.Serializable {
	@Column(name = "cigenId", nullable = false)
	private Integer cigenId;

	@Column(name = "wordId", nullable = false)
	private Integer wordId;

	// Constructors

	/**
	 * default constructor
	 */
	public CigenWordLinkId() {
	}

	// Property accessors

	public Integer getCigenId() {
		return this.cigenId;
	}

	public void setCigenId(Integer cigenId) {
		this.cigenId = cigenId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof CigenWordLinkId))
			return false;
		CigenWordLinkId castOther = (CigenWordLinkId) other;

		return cigenId.equals(castOther.cigenId) && wordId.equals(castOther.wordId);
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + cigenId.hashCode();
		result = 37 * result + wordId.hashCode();
		return result;
	}

}