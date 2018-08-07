package beidanci.po;

import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * 单词的释义
 *
 * @author MaYubing
 */
@Entity
@Table(name = "meaning_item")
@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class MeaningItem extends Po {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	/**
	 * 释义所属单词
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "word")
	private Word word;

	/**
	 * 单词词性
	 */
	@Column(name = "ciXing", length = 10)
	private String ciXing;

	/**
	 * 释义
	 */
	@Column(name = "meaning", length = 500)
	private String meaning;

	/**
	 * 近义词
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE }, mappedBy = "meaningItem")
	@Fetch(FetchMode.SUBSELECT)
	@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<Synonym> synonyms;

	public MeaningItem() {

	}

	public MeaningItem(String ciXing, String meaning) {
		this.ciXing = ciXing;
		this.meaning = meaning;

	}

	@Override
	public String toString() {
		String meaningStr = meaning;
		if (!meaningStr.endsWith(";") && !meaningStr.endsWith("；")) {
			meaningStr += "；";
		}

		return String.format("%s %s", ciXing, meaningStr);
	}

	public String getCiXing() {
		return ciXing;
	}

	public void setCiXing(String ciXing) {
		this.ciXing = ciXing;
	}

	public String getMeaning() {
		return meaning;
	}

	public void setMeaning(String meaning) {
		this.meaning = meaning;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Word getWord() {
		return word;
	}

	public void setWord(Word word) {
		this.word = word;
	}

	public List<Synonym> getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(List<Synonym> synonyms) {
		this.synonyms = synonyms;
	}
}
