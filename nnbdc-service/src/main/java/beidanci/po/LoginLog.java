package beidanci.po;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "login_log")
public class LoginLog extends Po implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	public LoginLog() {
	}

	public LoginLog(User user, Date loginTime) {
		this.user = user;
		this.loginTime = loginTime;
	}

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	@Column(name = "loginTime", nullable = false)
	private Date loginTime;
}
