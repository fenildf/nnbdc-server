package beidanci.vo;

import java.util.LinkedList;
import java.util.List;

import beidanci.util.Utils;

public class WordVo extends Vo {

	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private String spell;

	private String britishPronounce;

	private String americaPronounce;

	private String pronounce;

	private Integer popularity;

	private String groupInfo;

	private String shortDesc;

	private String meaningStr;

	private List<MeaningItemVo> meaningItems;

	private List<SynonymsItem> synonymsItems;

	private List<SentenceVo> sentences;

	private List<WordVo> similarWords;

	public List<WordVo> getSimilarWords() {
		return similarWords;
	}

	public void setSimilarWords(List<WordVo> similarWords) {
		this.similarWords = similarWords;
	}

	public WordVo() {

	}

	public WordVo(String spell) {
		this.spell = spell;

		meaningItems = new LinkedList<MeaningItemVo>();
		sentences = new LinkedList<SentenceVo>();
	}

	public void addMeaningItem(MeaningItemVo meaningItem) {
		meaningItems.add(meaningItem);
	}

	public String getMeaningStr() {
		if (this.meaningStr != null) {
			return meaningStr;
		}

		StringBuilder sb = new StringBuilder();
		if (meaningItems != null) {
			for (MeaningItemVo item : meaningItems) {
				sb.append(item.toString());
			}
		}
		String str = sb.toString();
		if (str.length() > 0) {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	public void setMeaningStr(String meaningStr) {
		this.meaningStr = meaningStr;
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

	public List<MeaningItemVo> getMeaningItems() {
		return meaningItems;
	}

	public void setMeaningItems(List<MeaningItemVo> meaningItems) {
		this.meaningItems = meaningItems;
	}

	public List<SynonymsItem> getSynonymsItems() {
		return synonymsItems;
	}

	public void setSynonymsItems(List<SynonymsItem> synonymsItems) {
		this.synonymsItems = synonymsItems;
	}

	// private boolean sentenceInitialized = false;

	public List<SentenceVo> getSentences() {
		/*
		 * SentenceStore sentenceStore = SentenceStore.getInstance();
		 * 
		 * if (!sentenceInitialized) { synchronized (sentences) { if
		 * (!sentenceInitialized) { // 对于短语，词库中会保留对例句的引用, 这里把引用解开 List<SentenceVo>
		 * parsedSentences = new ArrayList<SentenceVo>(); for (SentenceVo sentenceRef :
		 * sentences) { String englishDigest = sentenceRef.getEnglishDigest();
		 * SentenceVo sentence = sentenceStore.getSentenceByDigest(englishDigest);
		 * parsedSentences.add(sentence); } sentences.clear();
		 * sentences.addAll(parsedSentences);
		 * 
		 * // 在例句库中搜索该单词的例句 List<SentenceVo> sentencesFromStore =
		 * sentenceStore.getSentencesOfWord(spell); if (sentencesFromStore != null) {
		 * sentences.addAll(sentencesFromStore); }
		 * 
		 * // 每个单词最多只保留6个例句 if (sentences.size() > 6) {
		 * 
		 * // 把所有例句进行分类 List<SentenceVo> humanAudioSentences = new
		 * ArrayList<SentenceVo>(); List<SentenceVo> bilingualSentences = new
		 * ArrayList<SentenceVo>(); List<SentenceVo> humanVideoSentences = new
		 * ArrayList<SentenceVo>(); List<SentenceVo> authoritySentences = new
		 * ArrayList<SentenceVo>(); for (SentenceVo sentence : sentences) { if
		 * (sentence.getType().equalsIgnoreCase(Sentence.AUTHORITY)) {
		 * authoritySentences.add(sentence); } else if
		 * (sentence.getType().equalsIgnoreCase(Sentence.BILINGUAL)) {
		 * bilingualSentences.add(sentence); } else if
		 * (sentence.getType().equalsIgnoreCase(Sentence.HUMAN_AUDIO)) {
		 * humanAudioSentences.add(sentence); } else if
		 * (sentence.getType().equalsIgnoreCase(Sentence.HUMAN_VIDEO)) {
		 * humanVideoSentences.add(sentence); } else {
		 * log.warn(String.format("发现未知的Sentence Type[%s]", sentence.getType())); } }
		 * 
		 * // 对每种例句按照句子长短排序 sortSentencesByLength(humanAudioSentences);
		 * sortSentencesByLength(humanVideoSentences);
		 * sortSentencesByLength(bilingualSentences);
		 * sortSentencesByLength(authoritySentences);
		 * 
		 * // 从每种类型中取出若干例句 List<SentenceVo> selectedSentences = new
		 * ArrayList<SentenceVo>(); int needCount = 0; // 该类型的例句需要的个数 needCount += 2;
		 * for (SentenceVo sentence : humanAudioSentences) {// 原声例句 if (needCount == 0)
		 * { break; } selectedSentences.add(sentence); needCount--; } needCount += 2;
		 * for (SentenceVo sentence : bilingualSentences) {// 双语例句 if (needCount == 0) {
		 * break; } selectedSentences.add(sentence); needCount--; } needCount += 2; for
		 * (SentenceVo sentence : humanVideoSentences) {// 视频例句 if (needCount == 0) {
		 * break; } selectedSentences.add(sentence); needCount--; } for (SentenceVo
		 * sentence : authoritySentences) {// 权威例句(候补) if (needCount == 0) { break; }
		 * selectedSentences.add(sentence); needCount--; } sentences.clear();
		 * sentences.addAll(selectedSentences); }
		 * 
		 * // 检查例句的声音文件是否存在 for (SentenceVo sentence : sentences) {
		 * sentence.setSoundFileExists(isSentenceSoundFileExists(sentence)); }
		 * 
		 * sentenceInitialized = true; } } }
		 */
		return sentences;
	}

	public void setSentences(List<SentenceVo> sentences) {
		this.sentences = sentences;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}

	public String getSound() {
		return Utils.getFileNameOfWordSound(spell);
	}
}
