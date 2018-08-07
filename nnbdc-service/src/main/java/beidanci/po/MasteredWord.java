package beidanci.po;

import java.sql.Timestamp;

import javax.persistence.*;

@Entity
@Table(name = "mastered_word")
public class MasteredWord extends Po {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	private MasteredWordId id;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false, updatable = false, insertable = false)
	private User user;

	@Column(name = "masterAtTime", nullable = false)
	private Timestamp masterAtTime;

	/**
	 * default constructor
	 */
	public MasteredWord() {
	}

	/**
	 * full constructor
	 */
	public MasteredWord(MasteredWordId id, User user, Timestamp masterAtTime) {
		this.id = id;
		this.user = user;
		this.masterAtTime = masterAtTime;
	}

	// Property accessors

	public MasteredWordId getId() {
		return this.id;
	}

	public void setId(MasteredWordId id) {
		this.id = id;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Timestamp getMasterAtTime() {
		return this.masterAtTime;
	}

	public void setMasterAtTime(Timestamp masterAtTime) {
		this.masterAtTime = masterAtTime;
	}

}