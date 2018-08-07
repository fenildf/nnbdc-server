package beidanci.vo;

import java.util.List;

public class HallGroupVo extends Vo {
	private Integer id;

	private String gameType;

	private String groupName;

	private Integer displayOrder;

	private List<GameHallVo> gameHalls;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getGameType() {
		return gameType;
	}

	public void setGameType(String gameType) {
		this.gameType = gameType;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public List<GameHallVo> getGameHalls() {
		return gameHalls;
	}

	public void setGameHalls(List<GameHallVo> gameHalls) {
		this.gameHalls = gameHalls;
	}
}
