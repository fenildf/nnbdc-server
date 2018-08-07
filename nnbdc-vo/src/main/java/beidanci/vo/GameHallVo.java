package beidanci.vo;

public class GameHallVo extends Vo {
	private DictGroupVo dictGroup;

	private HallGroupVo hallGroup;

	private Integer basePoint;

	private Integer displayOrder;

	private String gameType;

	private String hallName;

	public DictGroupVo getDictGroup() {
		return dictGroup;
	}

	public void setDictGroup(DictGroupVo dictGroup) {
		this.dictGroup = dictGroup;
	}

	public HallGroupVo getHallGroup() {
		return hallGroup;
	}

	public void setHallGroup(HallGroupVo hallGroup) {
		this.hallGroup = hallGroup;
	}

	public Integer getBasePoint() {
		return basePoint;
	}

	public void setBasePoint(Integer basePoint) {
		this.basePoint = basePoint;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

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
}
