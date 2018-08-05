package beidanci.po;

import javax.persistence.*;

@Entity
@Table(name = "sentence_diy_item_remark")
public class SentenceDiyItemRemark extends Po implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "itemID", nullable = false, updatable = false, insertable = false)
	private SentenceDiyItem sentenceDiyItem;

	@ManyToOne
	@JoinColumn(name = "author", nullable = false, updatable = false, insertable = false)
	private User user;

	@Column(name = "content", length = 1000, nullable = false)
	private String content;

	// Constructors

	/** default constructor */
	public SentenceDiyItemRemark() {
	}

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public SentenceDiyItem getSentenceDiyItem() {
		return this.sentenceDiyItem;
	}

	public void setSentenceDiyItem(SentenceDiyItem sentenceDiyItem) {
		this.sentenceDiyItem = sentenceDiyItem;
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

}