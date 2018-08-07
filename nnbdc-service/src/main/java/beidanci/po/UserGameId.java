package beidanci.po;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class UserGameId implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "userId", nullable = false)
	private Integer userId;

	@Column(name = "game", length = 100)
	private String game;

	// Constructors

	/**
	 * default constructor
	 */
	public UserGameId() {
	}

	public UserGameId(Integer userId, String game) {
		this.userId = userId;
		this.game = game;
	}

	public String getGame() {
		return this.game;
	}

	public void setGame(String game) {
		this.game = game;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof UserGameId))
			return false;
		UserGameId castOther = (UserGameId) other;

		return userId.equals(castOther.userId) && game.equals(castOther.game);
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + userId.hashCode();
		result = 37 * result + game.hashCode();
		return result;
	}

}