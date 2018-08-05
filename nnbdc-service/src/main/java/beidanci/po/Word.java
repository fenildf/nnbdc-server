package beidanci.po;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

@Entity
@Table(name = "word", indexes = { @Index(name = "idx_wordspell", columnList = "spell", unique = true) })
@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Word extends Po {

	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "spell", length = 100)
	private String spell;

	@Column(name = "britishPronounce", length = 100)
	private String britishPronounce;

	@Column(name = "americaPronounce", length = 100)
	private String americaPronounce;

	@Column(name = "pronounce", length = 100)
	private String pronounce;

	@Column(name = "popularity", nullable = false)
	private Integer popularity;

	@Column(name = "groupInfo", length = 200)
	private String groupInfo;

	/**
	 * 单词的简要描述
	 */
	@Column(name = "shortDesc", length = 500)
	private String shortDesc;

	/**
	 * 单词的详细描述
	 */
	@Column(name = "longDesc", length = 1000)
	private String longDesc;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE }, mappedBy = "word")
	@Fetch(FetchMode.SUBSELECT)
	@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<MeaningItem> meaningItems;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "word_sentence", joinColumns = { @JoinColumn(name = "wordId") }, inverseJoinColumns = {
			@JoinColumn(name = "sentenceId") })
	// @Fetch(FetchMode.SUBSELECT)
	@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<Sentence> sentences;

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true, mappedBy = "word")
	@Fetch(FetchMode.SUBSELECT)
	@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<WordImage> images;

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true, mappedBy = "word")
	@Fetch(FetchMode.SUBSELECT)
	@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<WordShortDescChinese> wordShortDescChineses;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE }, mappedBy = "word")
	@Fetch(FetchMode.SUBSELECT)
	@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<VerbTense> verbTences;

	@ManyToMany
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "similar_word", joinColumns = { @JoinColumn(name = "word") }, inverseJoinColumns = {
			@JoinColumn(name = "similarWord") })
	@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<Word> similarWords;

	public List<VerbTense> getVerbTences() {
		return verbTences;
	}

	public void setVerbTences(List<VerbTense> verbTences) {
		this.verbTences = verbTences;
	}

	public Word() {

	}

	public Word(String spell) {
		this.spell = spell;

		meaningItems = new LinkedList<MeaningItem>();
	}

	public List<WordImage> getImages() {
		return images;
	}

	public void setImages(List<WordImage> images) {
		this.images = images;
	}

	public void addMeaningItem(MeaningItem meaningItem) {
		meaningItems.add(meaningItem);
	}

	public String makeJSonForStore() {
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setExcludes(new String[] { "meaningStr", "phrase", "chinese", "english", "soundFileExists" });
		JSONObject jo = JSONObject.fromObject(this, jsonConfig);

		return jo.toString();
	}

	public String getMeaningStr() {
		StringBuilder sb = new StringBuilder();
		for (MeaningItem item : meaningItems) {
			sb.append(item.toString());
		}
		String str = sb.toString();
		if (str.length() > 0) {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	public boolean isPhrase() {
		return spell.trim().indexOf(" ") != -1;
	}

	public boolean wordHasMeaning() {
		return getMeaningItems().size() > 0;
	}

	public String getSpell() {
		return spell;
	}

	public void setSpell(String spell) {
		this.spell = spell;
	}

	public String getBritishPronounce() {
		return britishPronounce;
	}

	public void setBritishPronounce(String britishPronounce) {
		this.britishPronounce = britishPronounce;
	}

	public String getAmericaPronounce() {
		return americaPronounce;
	}

	public void setAmericaPronounce(String americaPronounce) {
		this.americaPronounce = americaPronounce;
	}

	public String getPronounce() {
		return pronounce;
	}

	public void setPronounce(String pronounce) {
		this.pronounce = pronounce;
	}

	public Integer getPopularity() {
		return popularity;
	}

	public void setPopularity(Integer popularity) {
		this.popularity = popularity;
	}

	public String getGroupInfo() {
		return groupInfo;
	}

	public void setGroupInfo(String groupInfo) {
		this.groupInfo = groupInfo;
	}

	public List<MeaningItem> getMeaningItems() {
		return meaningItems;
	}

	public void setMeaningItems(List<MeaningItem> meaningItems) {
		this.meaningItems = meaningItems;
	}

	public List<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}

	public String getLongDesc() {
		return longDesc;
	}

	public void setLongDesc(String longDesc) {
		this.longDesc = longDesc;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Word)) {
			return false;
		}
		return this.getSpell().equals(((Word) obj).getSpell());
	}

	public List<Word> getSimilarWords() {
		return similarWords;
	}

	public void setSimilarWords(List<Word> similarWords) {
		this.similarWords = similarWords;
	}

	public List<WordShortDescChinese> getWordShortDescChineses() {
		return wordShortDescChineses;
	}

	public void setWordShortDescChineses(List<WordShortDescChinese> wordShortDescChineses) {
		this.wordShortDescChineses = wordShortDescChineses;
	}

	public Word(String spell, String britishPronounce, String americaPronounce, String pronounce, Integer popularity,
			String groupInfo, String shortDesc, String longDesc, List<MeaningItem> meaningItems,
			List<Sentence> sentences, List<WordImage> images, List<WordShortDescChinese> wordShortDescChineses,
			List<VerbTense> verbTences, List<Word> similarWords) {
		super();
		this.spell = spell;
		this.britishPronounce = britishPronounce;
		this.americaPronounce = americaPronounce;
		this.pronounce = pronounce;
		this.popularity = popularity;
		this.groupInfo = groupInfo;
		this.shortDesc = shortDesc;
		this.longDesc = longDesc;
		this.meaningItems = meaningItems;
		this.sentences = sentences;
		this.images = images;
		this.wordShortDescChineses = wordShortDescChineses;
		this.verbTences = verbTences;
		this.similarWords = similarWords;
	}
}
