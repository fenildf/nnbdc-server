package beidanci.po;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "user_cow_dung_log")
public class UserCowDungLog extends Po implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	@Column(name = "delta", nullable = false)
	private Integer delta;

	@Column(name = "cowDung", nullable = false)
	private Integer cowDung;

	@Column(name = "theTime", nullable = false)
	private Date theTime;

	@Column(name = "reason", nullable = false)
	private String reason;

	// Constructors

	/**
	 * default constructor
	 */
	public UserCowDungLog() {
	}

	/**
	 * full constructor
	 */
	public UserCowDungLog(User user, Integer delta, Integer cowDung, Timestamp theTime, String reason) {
		this.user = user;
		this.delta = delta;
		this.cowDung = cowDung;
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

	public Integer getCowDung() {
		return this.cowDung;
	}

	public void setCowDung(Integer cowDung) {
		this.cowDung = cowDung;
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

	public Date getTheTime() {
		return theTime;
	}

	public void setTheTime(Date theTime) {
		this.theTime = theTime;
	}

}