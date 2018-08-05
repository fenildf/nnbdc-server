package beidanci.po;

import java.util.List;

import javax.persistence.*;

@Entity
@Table(name = "forum")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Forum extends Po implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@Column(name = "name", nullable = false)
	private String name;

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE })
	@JoinTable(name = "forum_and_manager_link", joinColumns = @JoinColumn(name = "forumId"), inverseJoinColumns = @JoinColumn(name = "userId"))
	private List<User> managers;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE,
			CascadeType.MERGE }, mappedBy = "forum", fetch = FetchType.LAZY)
	private List<ForumPost> forumPosts;

	// Constructors

	/** default constructor */
	public Forum() {
	}

	/** minimal constructor */
	public Forum(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	/** full constructor */
	public Forum(Integer id, String name, List managers, List forumPosts) {
		this.id = id;
		this.name = name;
		this.managers = managers;
		this.forumPosts = forumPosts;
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

	public List getManagers() {
		return this.managers;
	}

	public void setManagers(List managers) {
		this.managers = managers;
	}

	public List<ForumPost> getForumPosts() {
		return this.forumPosts;
	}

	public void setForumPosts(List forumPosts) {
		this.forumPosts = forumPosts;
	}

}