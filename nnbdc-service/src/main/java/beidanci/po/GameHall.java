package beidanci.po;

import javax.persistence.*;

@Entity
@Table(name = "game_hall")
public class GameHall extends Po implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private GameHallId id;

	@ManyToOne
	@JoinColumn(name = "dictGroup", nullable = false, updatable = false, insertable = false)
	private DictGroup dictGroup;

	@ManyToOne
	@JoinColumn(name = "hallGroup", nullable = false, insertable = false, updatable = false)
	private HallGroup hallGroup;

	@Column(name = "basePoint")
	private Integer basePoint;

	@Column(name = "displayOrder")
	private Integer displayOrder;

	// Constructors

	/**
	 * default constructor
	 */
	public GameHall() {
	}

	public GameHallId getId() {
		return id;
	}

	public void setId(GameHallId id) {
		this.id = id;
	}

	public DictGroup getDictGroup() {
		return this.dictGroup;
	}

	public void setDictGroup(DictGroup dictGroup) {
		this.dictGroup = dictGroup;
	}

	public HallGroup getHallGroup() {
		return this.hallGroup;
	}

	public void setHallGroup(HallGroup hallGroup) {
		this.hallGroup = hallGroup;
	}

	public Integer getBasePoint() {
		return this.basePoint;
	}

	public void setBasePoint(Integer basePoint) {
		this.basePoint = basePoint;
	}

	public Integer getDisplayOrder() {
		return this.displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public String getHallName() {
		return id.getHallName();
	}

	public String getGameType() {
		return id.getGameType();
	}

}