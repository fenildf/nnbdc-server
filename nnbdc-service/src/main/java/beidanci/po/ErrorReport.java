package beidanci.po;

import javax.persistence.*;

@Entity
@Table(name = "error_report")
public class ErrorReport extends Po implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	@Column(name = "content", length = 8192, nullable = false)
	private String content;

	@Column(name = "word", length = 100, nullable = false)
	private String word;

	@Column(name = "fixed", nullable = false)
	private Boolean fixed;

	public Boolean getFixed() {
		return fixed;
	}

	public void setFixed(Boolean fixed) {
		this.fixed = fixed;
	}

	// Constructors

	/**
	 * default constructor
	 */
	public ErrorReport() {
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

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getWord() {
		return this.word;
	}

	public void setWord(String word) {
		this.word = word;
	}

}