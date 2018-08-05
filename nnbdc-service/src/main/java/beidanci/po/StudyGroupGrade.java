package beidanci.po;

import java.util.List;

import javax.persistence.*;

@Entity
@Table(name = "study_group_grade")
@SuppressWarnings({ "rawtypes" })
public class StudyGroupGrade extends Po implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "maxUserCount", nullable = false)
	private Integer maxUserCount;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE,
			CascadeType.MERGE }, mappedBy = "studyGroupGrade", fetch = FetchType.LAZY)
	private List<StudyGroup> studyGroups;

	// Constructors

	/** default constructor */
	public StudyGroupGrade() {
	}

	/** minimal constructor */
	public StudyGroupGrade(Integer id, String name, Integer maxUserCount) {
		this.id = id;
		this.name = name;
		this.maxUserCount = maxUserCount;
	}

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getMaxUserCount() {
		return this.maxUserCount;
	}

	public void setMaxUserCount(Integer maxUserCount) {
		this.maxUserCount = maxUserCount;
	}

	public List<StudyGroup> getStudyGroups() {
		return studyGroups;
	}

	public void setStudyGroups(List<StudyGroup> studyGroups) {
		this.studyGroups = studyGroups;
	}
}