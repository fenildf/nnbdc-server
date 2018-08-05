package beidanci.po;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.*;

import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.store.WordStore;
import beidanci.vo.WordVo;

@Entity
@Table(name = "learning_word", indexes = { @Index(name = "idx_userid", columnList = "userId") })
public class LearningWord extends Po implements java.io.Serializable {

	// Fields

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	private LearningWordId id;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false, updatable = false, insertable = false)
	private User user;

	@Column(name = "AddTime", nullable = false)
	private Date addTime;

	@Column(name = "AddDay", nullable = false)
	private Integer addDay;

	@Column(name = "LifeValue", nullable = false)
	private Integer lifeValue;

	@Column(name = "LastLearningDate")
	private Date lastLearningDate;

	@Column(name = "LearningOrder")
	private Integer learningOrder;

	// Constructors

	/**
	 * default constructor
	 */
	public LearningWord() {
	}

	/**
	 * minimal constructor
	 */
	public LearningWord(LearningWordId id, User user, Timestamp addTime, Integer addDay, Integer lifeValue) {
		this.id = id;
		this.user = user;
		this.addTime = addTime;
		this.addDay = addDay;
		this.lifeValue = lifeValue;
	}

	/**
	 * full constructor
	 */
	public LearningWord(LearningWordId id, User user, Timestamp addTime, Integer addDay, Integer lifeValue,
			Date lastLearningDate, Integer learningOrder) {
		this.id = id;
		this.user = user;
		this.addTime = addTime;
		this.addDay = addDay;
		this.lifeValue = lifeValue;
		this.lastLearningDate = lastLearningDate;
		this.learningOrder = learningOrder;
	}

	// Property accessors

	public LearningWordId getId() {
		return this.id;
	}

	public void setId(LearningWordId id) {
		this.id = id;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getAddTime() {
		return this.addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public Integer getAddDay() {
		return this.addDay;
	}

	public void setAddDay(Integer addDay) {
		this.addDay = addDay;
	}

	public Integer getLifeValue() {
		return this.lifeValue;
	}

	public void setLifeValue(Integer lifeValue) {
		this.lifeValue = lifeValue;
	}

	public Date getLastLearningDate() {
		return this.lastLearningDate;
	}

	public void setLastLearningDate(Date lastLearningDate) {
		this.lastLearningDate = lastLearningDate;
	}

	public Integer getLearningOrder() {
		return this.learningOrder;
	}

	public void setLearningOrder(Integer learningOrder) {
		this.learningOrder = learningOrder;
	}

	public WordVo getWord() throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		return WordStore.getInstance().getWordById(id.getWordId());
	}

}