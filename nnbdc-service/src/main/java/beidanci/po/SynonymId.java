package beidanci.po;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SynonymId implements Serializable {
	private static final long serialVersionUID = 1L;
	@Column(name = "meaningItemId")
	private Integer meaningItemId;

	public Integer getWordId() {
		return wordId;
	}

	public void setWordId(Integer wordId) {
		this.wordId = wordId;
	}

	@Column(name = "wordId")
	private Integer wordId;

	public long getMeaningItemId() {
		return meaningItemId;
	}

	public void setMeaningItemId(Integer meaningItemId) {
		this.meaningItemId = meaningItemId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SynonymId))
			return false;
		SynonymId castOther = (SynonymId) other;

		return wordId.equals(castOther.wordId) && meaningItemId.equals(castOther.meaningItemId);
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + wordId.hashCode();
		result = 37 * result + meaningItemId.hashCode();
		return result;
	}
}
