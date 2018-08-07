package beidanci.po;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "user_game")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserGame extends Po implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private UserGameId id;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false, updatable = false, insertable = false)
	private User user;

	@Column(name = "winCount")
	private Integer winCount;

	@Column(name = "loseCount")
	private Integer loseCount;

	@Column(name = "score")
	private Integer score;

	// Constructors

	/**
	 * default constructor
	 */
	public UserGame() {
	}

	public UserGame(UserGameId id, User user, Integer winCount, Integer loseCount, Integer score) {
		this.id = id;
		this.user = user;
		this.winCount = winCount;
		this.loseCount = loseCount;
		this.score = score;
	}

	// Property accessors

	public UserGameId getId() {
		return this.id;
	}

	public void setId(UserGameId id) {
		this.id = id;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getWinCount() {
		return this.winCount;
	}

	public void setWinCount(Integer winCount) {
		this.winCount = winCount;
	}

	public Integer getLoseCount() {
		return this.loseCount;
	}

	public void setLoseCount(Integer loseCount) {
		this.loseCount = loseCount;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public String getGame() {
		return id.getGame();
	}

}