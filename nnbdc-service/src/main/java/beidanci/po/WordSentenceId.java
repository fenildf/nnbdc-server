package beidanci.po;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class WordSentenceId implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "wordId", nullable = false)
	private Integer wordId;

	@Column(name = "sentenceId", nullable = false)
	private Integer sentenceId;

	public Integer getWordId() {
		return wordId;
	}

	public void setWordId(Integer wordId) {
		this.wordId = wordId;
	}

	public Integer getSentenceId() {
		return sentenceId;
	}

	public void setSentenceId(Integer sentenceId) {
		this.sentenceId = sentenceId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof WordSentenceId))
			return false;
		WordSentenceId castOther = (WordSentenceId) other;

		return wordId.equals(castOther.wordId) && sentenceId.equals(castOther.sentenceId);
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + wordId.hashCode();
		result = 37 * result + sentenceId.hashCode();
		return result;
	}
}
