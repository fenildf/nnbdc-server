package beidanci.po;

import javax.persistence.*;

@Entity
@Table(name = "article")
public class Article extends Po implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "author", nullable = false, updatable = false, insertable = false)
	private User user;

	@Column(name = "title", length = 1024, nullable = false)
	private String title;

	@Column(name = "content", length = 8192, nullable = false)
	private String content;

	@Column(name = "viewedCount", nullable = false)
	private Integer viewedCount;

	@Column(name = "keyWords", length = 1024, nullable = false)
	private String keyWords;

	@Column(name = "description", length = 1024, nullable = false)
	private String description;

	// Constructors

	/**
	 * default constructor
	 */
	public Article() {
	}

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getViewedCount() {
		return this.viewedCount;
	}

	public void setViewedCount(Integer viewedCount) {
		this.viewedCount = viewedCount;
	}

	public String getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}