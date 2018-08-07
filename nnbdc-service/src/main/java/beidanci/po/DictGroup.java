package beidanci.po;

import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Entity
@Table(name = "dict_group")
@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class DictGroup extends Po implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@Column(name = "name", length = 20)
	private String name;

	@ManyToOne
	@JoinColumn(name = "parent")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private DictGroup dictGroup;

	public Integer getDisplayIndex() {
		return displayIndex;
	}

	public void setDisplayIndex(Integer displayIndex) {
		this.displayIndex = displayIndex;
	}

	@Column(name = "displayIndex", nullable = false)
	private Integer displayIndex;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE,
			CascadeType.MERGE }, mappedBy = "dictGroup", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@Fetch(FetchMode.SUBSELECT)
	private List<DictGroup> dictGroups;

	@ManyToMany
	@JoinTable(name = "group_and_dict_link", joinColumns = { @JoinColumn(name = "groupId") }, inverseJoinColumns = {
			@JoinColumn(name = "dictId") })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@Fetch(FetchMode.SUBSELECT)
	private List<Dict> dicts;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE,
			CascadeType.MERGE }, mappedBy = "dictGroup", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@Fetch(FetchMode.SUBSELECT)
	private Set<GameHall> gameHalls;

	// Constructors

	/**
	 * default constructor
	 */
	public DictGroup() {
	}

	/**
	 * minimal constructor
	 */
	public DictGroup(String name, DictGroup dictGroup, Integer displayIndex) {
		this.name = name;
		this.dictGroup = dictGroup;
		this.displayIndex = displayIndex;
	}

	/**
	 * full constructor
	 */
	public DictGroup(String name, DictGroup dictGroup, Integer displayIndex, List dictGroups, List dicts,
			Set gameHalls) {
		this.name = name;
		this.dictGroup = dictGroup;
		this.displayIndex = displayIndex;
		this.dictGroups = dictGroups;
		this.dicts = dicts;
		this.gameHalls = gameHalls;
	}

	// Property accessors

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DictGroup getDictGroup() {
		return this.dictGroup;
	}

	public void setDictGroup(DictGroup dictGroup) {
		this.dictGroup = dictGroup;
	}

	public List<DictGroup> getDictGroups() {
		return this.dictGroups;
	}

	public void setDictGroups(List<DictGroup> dictGroups) {
		this.dictGroups = dictGroups;
	}

	public List<Dict> getDicts() {
		return dicts;
	}

	public void setDicts(List<Dict> dicts) {
		this.dicts = dicts;
	}

	public Set getGameHalls() {
		return this.gameHalls;
	}

	public void setGameHalls(Set gameHalls) {
		this.gameHalls = gameHalls;
	}

	/**
	 * 获取群组之下的所有单词书，包括子孙群组之下的单词书
	 *
	 * @return
	 */
	public List<Dict> getAllDicts() {
		Map<String, Dict> dicts = new HashMap<String, Dict>();

		// 加入子群组包含的单词书
		for (DictGroup dictGroup : this.dictGroups) {
			// root群组的的父亲还是root，要避免无限递归
			if (dictGroup.getName().equalsIgnoreCase("root")) {
				continue;
			}

			for (Dict dict : dictGroup.getAllDicts()) {
				dicts.put(dict.getName(), dict);
			}
		}

		// 加入直接包含的单词书
		for (Dict dict : this.dicts) {
			dicts.put(dict.getName(), dict);
		}

		// 将单词书按字母顺序排序
		List<Dict> dicts2 = new ArrayList<Dict>(dicts.values());
		Collections.sort(dicts2, new Comparator<Dict>() {

			@Override
			public int compare(Dict o1, Dict o2) {
				return o1.getName().compareTo(o2.getName());
			}

		});

		return dicts2;
	}
}