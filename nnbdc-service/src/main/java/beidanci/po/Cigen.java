package beidanci.po;

import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name = "cigen")
public class Cigen extends Po {

	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@Column(name = "description", length = 1024, nullable = false)
	private String description;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE,
			CascadeType.MERGE }, mappedBy = "cigen", fetch = FetchType.LAZY)
	private Set<CigenWordLink> cigenWordLinks;

	// Constructors

	/**
	 * default constructor
	 */
	public Cigen() {
	}

	/**
	 * minimal constructor
	 */
	public Cigen(Integer id, String description) {
		this.id = id;
		this.description = description;
	}

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<CigenWordLink> getCigenWordLinks() {
		return cigenWordLinks;
	}

	public void setCigenWordLinks(Set<CigenWordLink> cigenWordLinks) {
		this.cigenWordLinks = cigenWordLinks;
	}

}