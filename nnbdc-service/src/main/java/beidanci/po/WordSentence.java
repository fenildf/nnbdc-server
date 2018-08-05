package beidanci.po;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "word_sentence")
@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class WordSentence extends Po {
	private static final long serialVersionUID = 1L;
	@Id
	private WordSentenceId id;

	@ManyToOne
	@JoinColumn(name = "wordId", nullable = false, updatable = false, insertable = false)
	private Word word;

	public Word getWord() {
		return word;
	}

	public void setWord(Word word) {
		this.word = word;
	}

	public WordSentenceId getId() {
		return id;
	}

	public void setId(WordSentenceId id) {
		this.id = id;
	}
}
