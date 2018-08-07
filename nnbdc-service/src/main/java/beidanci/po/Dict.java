package beidanci.po;

import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 单词书
 *
 * @author Administrator
 */
@Entity
@Table(name = "dict", indexes = { @Index(name = "idx_dictname", columnList = "name", unique = true) })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Dict extends Po implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger log = LoggerFactory.getLogger(Dict.class);

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

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@ManyToOne
	@JoinColumn(name = "owner", nullable = false)
	private User owner;

	/**
	 * 对于用户自定义的单词书，该标志指明该单词书是否已经共享给其他用户
	 */
	@Column(name = "isShared", nullable = false)
	private Boolean isShared;

	/**
	 * 该单词书是否已经准备就绪（只有准备就绪的单词书才能供用户使用，并且一旦就绪后就不能再编辑）
	 */
	@Column(name = "isReady", nullable = false)
	private Boolean isReady;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE,
			CascadeType.MERGE }, mappedBy = "dict", fetch = FetchType.LAZY)
	private List<DictWord> dictWords;

	/**
	 * 该单词书的单词数量
	 */
	@Column(name = "wordCount", nullable = false)
	private Integer wordCount;

	// Constructors

	public List<DictWord> getDictWords() {
		return dictWords;
	}

	public void setDictWords(List<DictWord> dictWords) {
		this.dictWords = dictWords;
	}

	/**
	 * default constructor
	 */
	public Dict() {
	}

	/**
	 * minimal constructor
	 */
	public Dict(String name) {
		this.name = name;
	}

	// Property accessors

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getWordCount() {
		return wordCount;
	}

	public void setWordCount(Integer wordCount) {
		this.wordCount = wordCount;
	}

	public String getShortName() {
		return name.substring(0, name.lastIndexOf("."));
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Boolean getIsReady() {
		return isReady;
	}

	public void setIsReady(Boolean isReady) {
		this.isReady = isReady;
	}

	public Boolean getIsShared() {
		return isShared;
	}

	public void setIsShared(Boolean isShared) {
		this.isShared = isShared;
	}
}