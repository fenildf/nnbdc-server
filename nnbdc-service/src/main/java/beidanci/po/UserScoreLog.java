package beidanci.po;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "user_score_log")
public class UserScoreLog extends Po implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false, updatable = false, insertable = false)
	private User user;

	@Column(name = "delta")
	private Integer delta;

	@Column(name = "score")
	private Integer score;

	@Column(name = "theTime")
	private Date theTime;

	@Column(name = "reason", length = 1024)
	private String reason;

	// Constructors

	/** default constructor */
	public UserScoreLog() {
	}

	/** full constructor */
	public UserScoreLog(User user, Integer delta, Integer score, Timestamp theTime, String reason) {
		this.user = user;
		this.delta = delta;
		this.score = score;
		this.theTime = theTime;
		this.reason = reason;
	}

	// Property accessors

	public Long getId() {
		return this.id;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getDelta() {
		return this.delta;
	}

	public void setDelta(Integer delta) {
		this.delta = delta;
	}

	public Integer getScore() {
		return this.score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public Date getTheTime() {
		return this.theTime;
	}

	public void setTheTime(Date theTime) {
		this.theTime = theTime;
	}

	public String getReason() {
		return this.reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setId(Long id) {
		this.id = id;
	}

}