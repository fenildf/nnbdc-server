package beidanci.po;

import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "sentence_diy_item")
@SuppressWarnings({ "rawtypes", "unchecked" })
@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class SentenceDiyItem extends Po implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public static final String ITEM_TYPE_CHINESE = "ENGLISH";

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "sentenceID", nullable = false)
	private Sentence sentence;

	@ManyToOne
	@JoinColumn(name = "author", nullable = false)
	private User author;

	@Column(name = "itemType", length = 10, nullable = false)
	private String itemType;

	@Column(name = "content", length = 1000, nullable = false)
	private String content;

	@Column(name = "handCount", nullable = false)
	private Integer handCount;

	@Column(name = "footCount", nullable = false)
	private Integer footCount;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE,
			CascadeType.MERGE }, mappedBy = "sentenceDiyItem", fetch = FetchType.LAZY)
	private List<SentenceDiyItemRemark> sentenceDiyItemRemarks;

	// Constructors

	/**
	 * default constructor
	 */
	public SentenceDiyItem() {
	}

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Sentence getSentence() {
		return this.sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}

	public String getItemType() {
		return this.itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
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

	public List<SentenceDiyItemRemark> getSentenceDiyItemRemarks() {
		return this.sentenceDiyItemRemarks;
	}

	public void setSentenceDiyItemRemarks(List sentenceDiyItemRemarks) {
		this.sentenceDiyItemRemarks = sentenceDiyItemRemarks;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

}