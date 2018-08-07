package beidanci.po;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "learning_dict")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LearningDict extends Po implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private LearningDictId id;

	@ManyToOne
	@JoinColumn(name = "dictId", nullable = false, updatable = false, insertable = false)
	private Dict dict;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false, updatable = false, insertable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "currentWord", updatable = false, insertable = false)
	private Word currentWord;

	@Column(name = "CurrentWordOrder")
	private Integer currentWordOrder;

	// Constructors

	/**
	 * default constructor
	 */
	public LearningDict() {
	}

	/**
	 * minimal constructor
	 */
	public LearningDict(LearningDictId id, Dict dict, User user) {
		this.id = id;
		this.dict = dict;
		this.user = user;
	}

	// Property accessors

	public LearningDictId getId() {
		return this.id;
	}

	public void setId(LearningDictId id) {
		this.id = id;
	}

	public Dict getDict() {
		return this.dict;
	}

	public void setDict(Dict dict) {
		this.dict = dict;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Word getCurrentWord() {
		return this.currentWord;
	}

	public void setCurrentWord(Word currentWord) {
		this.currentWord = currentWord;
	}

	public Integer getCurrentWordOrder() {
		return this.currentWordOrder;
	}

	public void setCurrentWordOrder(Integer currentWordOrder) {
		this.currentWordOrder = currentWordOrder;
	}

	private boolean isPrivileged;

	public boolean getIsPrivileged() {
		return isPrivileged;
	}

	public void setIsPrivileged(boolean isPrivileged) {
		this.isPrivileged = isPrivileged;
	}
}