package beidanci.po;

import java.util.List;

import javax.persistence.*;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Entity
@Table(name = "hall_group")
public class HallGroup extends Po implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@Column(name = "gameType", length = 100)
	private String gameType;

	@Column(name = "groupName", length = 100)
	private String groupName;

	@Column(name = "displayOrder", nullable = false)
	private Integer displayOrder;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE,
			CascadeType.MERGE }, mappedBy = "hallGroup", fetch = FetchType.LAZY)
	@OrderBy("displayOrder asc")
	private List<GameHall> gameHalls;

	/**
	 * default constructor
	 */
	public HallGroup() {
	}

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
		return this.displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public List getGameHalls() {
		return this.gameHalls;
	}

	public void setGameHalls(List gameHalls) {
		this.gameHalls = gameHalls;
	}

}