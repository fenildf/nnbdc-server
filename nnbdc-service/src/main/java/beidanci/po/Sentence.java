package beidanci.po;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "sentence")
@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Sentence extends Po implements java.io.Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 双语例句
	 */
	public static final String BILINGUAL = "bilingual";

	/**
	 * 权威例句
	 */
	public static final String AUTHORITY = "authority";

	/**
	 * 原声例句(音频)
	 */
	public static final String HUMAN_AUDIO = "human_audio";

	/**
	 * 原声例句(视频)
	 */
	public static final String HUMAN_VIDEO = "human_video";

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@Column(name = "English", length = 2048)
	private String english;

	@Column(name = "Chinese", length = 2048)
	private String chinese;

	@Column(name = "TheType", length = 45)
	private String theType;

	@Column(name = "englishDigest", length = 32)
	private String englishDigest;

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true, mappedBy = "sentence", fetch = FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
	@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<SentenceDiyItem> sentenceDiyItems = new ArrayList<SentenceDiyItem>();

	@Column(name = "LastDiyUpdateTime")
	private Date lastDiyUpdateTime;

	// Constructors

	/**
	 * default constructor
	 */
	public Sentence() {
	}

	/**
	 * minimal constructor
	 */
	public Sentence(String english) {
		this.english = english;
	}

	/**
	 * full constructor
	 */
	public Sentence(String english, String chinese, String type) {
		this.english = english;
		this.chinese = chinese;
		this.theType = type;
	}

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEnglish() {
		return this.english;
	}

	public void setEnglish(String english) {
		this.english = english;
	}

	public String getChinese() {
		return this.chinese;
	}

	public void setChinese(String chinese) {
		this.chinese = chinese;
	}

	public String getTheType() {
		return this.theType;
	}

	public void setTheType(String type) {
		this.theType = type;
	}

	public String getEnglishDigest() {
		return englishDigest;
	}

	public void setEnglishDigest(String digest) {
		this.englishDigest = digest;
	}

	public List<SentenceDiyItem> getSentenceDiyItems() {
		return sentenceDiyItems;
	}

	public void setSentenceDiyItems(List<SentenceDiyItem> sentenceDiyItems) {
		this.sentenceDiyItems = sentenceDiyItems;
	}

	public Date getLastDiyUpdateTime() {
		return lastDiyUpdateTime;
	}

	public void setLastDiyUpdateTime(Date lastDiyUpdateTime) {
		this.lastDiyUpdateTime = lastDiyUpdateTime;
	}

}