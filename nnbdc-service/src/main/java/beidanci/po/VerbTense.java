package beidanci.po;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import beidanci.vo.TenseType;

/**
 * 动词的时态
 *
 * @author Administrator
 */
@Entity
@Table(name = "verb_tense")
@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class VerbTense extends Po implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "wordId", nullable = false, updatable = false, insertable = false)
	private Word word;

	/**
	 * 时态的类型 - ORDR_PMTR
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "tenseType", nullable = false, length = 20)
	private TenseType tenseType;

	@Column(name = "tensedSpell")
	private String tensedSpell;

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

	public TenseType getTenseType() {
		return tenseType;
	}

	public void setTenseType(TenseType tenseType) {
		this.tenseType = tenseType;
	}

	public String getTensedSpell() {
		return tensedSpell;
	}

	public void setTensedSpell(String tensedSpell) {
		this.tensedSpell = tensedSpell;
	}
}
