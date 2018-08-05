package beidanci.po;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class GameHallId implements java.io.Serializable {
	@Column(name = "gameType", nullable = false)
	private String gameType;

	@Column(name = "hallName", nullable = false)
	private String hallName;

	public String getGameType() {
		return gameType;
	}

	public void setGameType(String gameType) {
		this.gameType = gameType;
	}

	public String getHallName() {
		return hallName;
	}

	public void setHallName(String hallName) {
		this.hallName = hallName;
	}

	public GameHallId() {
	}

	public GameHallId(String gameType, String hallName) {
		this.gameType = gameType;
		this.hallName = hallName;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof GameHallId))
			return false;
		GameHallId castOther = (GameHallId) other;

		return gameType.equals(castOther.gameType) && hallName.equals(castOther.hallName);
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + gameType.hashCode();
		result = 37 * result + hallName.hashCode();
		return result;
	}
}
