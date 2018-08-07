package beidanci.po;

import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "word_additional_info")
@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class WordAdditionalInfo extends Po implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "wordId", nullable = false)
	private Word word;

	@Column(name = "content", length = 1024, nullable = false)
	private String content;

	@Column(name = "handCount", nullable = false)
	private Integer handCount;

	@Column(name = "footCount", nullable = false)
	private Integer footCount;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE,
			CascadeType.MERGE }, mappedBy = "wordAdditionalInfo", fetch = FetchType.LAZY)
	private Set<InfoVoteLog> voteLogs;

	// Constructors

	/**
	 * default constructor
	 */
	public WordAdditionalInfo() {
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

	public Word getWord() {
		return word;
	}

	public void setWord(Word word) {
		this.word = word;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getHandCount() {
		return this.handCount;
	}

	public void setHandCount(Integer handCount) {
		this.handCount = handCount;
	}

	public Integer getFootCount() {
		return this.footCount;
	}

	public void setFootCount(Integer footCount) {
		this.footCount = footCount;
	}

	public Set<InfoVoteLog> getVoteLogs() {
		return voteLogs;
	}

	public void setVoteLogs(Set<InfoVoteLog> voteLogs) {
		this.voteLogs = voteLogs;
	}

}