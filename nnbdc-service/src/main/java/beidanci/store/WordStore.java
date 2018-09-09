package beidanci.store;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import beidanci.Global;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.Sentence;
import beidanci.po.Word;
import beidanci.po.WordSentence;
import beidanci.util.BeanUtils;
import beidanci.util.Util;
import beidanci.vo.MeaningItemVo;
import beidanci.vo.SentenceVo;
import beidanci.vo.SynonymVo;
import beidanci.vo.WordVo;

public class WordStore {
	private static Logger log = LoggerFactory.getLogger(WordStore.class);

	private static WordStore instance;

	public static WordStore getInstance()
			throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		if (instance == null) {
			synchronized (WordStore.class) {
				if (instance == null) {
					instance = new WordStore();

				}
			}
		}
		return instance;
	}

	/**
	 * 词库中所有单词（按拼写索引）
	 */
	private Map<String, WordVo> words = new HashMap<>();

	/**
	 * 词库中所有单词（按ID索引)
	 */
	private Map<Integer, WordVo> wordsById = new HashMap<>();

	private WordStore() throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		loadFromDB();
	}

	public boolean addWord(WordVo word) throws IOException {
		if (word.getId() == null) {
			throw new RuntimeException("word ID为null");
		}
		words.put(word.getSpell(), word);
		wordsById.put(word.getId(), word);
		return true;
	}

	private static int invalidCount = 0;

	/**
	 * 验证单词是否合乎规格，如果不合规格则抛出异常
	 */
	private static boolean isWordValid(WordVo word) {
		return word.getMeaningItems().size() > 0;
	}

	public WordVo getWordBySpell(String spell) {
		return words.get(spell);
	}

	public WordVo getWordById(Integer id) {
		return wordsById.get(id);
	}

	public int getWordCount() {
		return words.size();
	}

	public void reloadWord(int wordId, String oldSpell) {
		words.remove(oldSpell);
		WordVo wordVo = Global.getWordBO().getWordVo(wordId);
		words.put(wordVo.getSpell(), wordVo);
		wordsById.put(wordVo.getId(), wordVo);

		// 挂接近义词
		for (MeaningItemVo meaningItem : wordVo.getMeaningItems()) {
			for (SynonymVo synonymVo : meaningItem.getSynonyms()) {
				synonymVo.setSpell(getWordById(synonymVo.getWordId()).getSpell());
			}
		}

		log.info(String.format("重新刷新了单词的缓存：%s", wordVo.getSpell()));
	}

	/**
	 * 从词库文件读取单词到内存，在读取过程中进行必要的验证
	 */
	private void loadFromDB() throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		Date beginTime = new Date();

		log.info("开始加载单词 ...");
		List<WordVo> wordVos = Global.getWordBO().getAllWordVos();
		for (WordVo wordVo : wordVos) {
			words.put(wordVo.getSpell(), wordVo);
			wordsById.put(wordVo.getId(), wordVo);
			if (words.size() % 1000 == 0) {
				log.info(words.size() + " words loaded.");
			}
		}

		// 挂接近义词
		log.info("开始挂接近义词 ...");
		for (WordVo wordVo : words.values()) {
			for (MeaningItemVo meaningItem : wordVo.getMeaningItems()) {
				for (SynonymVo synonymVo : meaningItem.getSynonyms()) {
					synonymVo.setSpell(getWordById(synonymVo.getWordId()).getSpell());
				}
			}
		}

		Date endTime = new Date();
		log.info(String.format("%d words loaded. use time:%s seconds", words.size(),
				(endTime.getTime() - beginTime.getTime()) / 1000));
	}

	/**
	 * 获取所有单词和例句的连接，并按照单词拼写进行组织
	 */
	public static Map<String, List<WordSentence>> getSentenceLinksOfAllWords() {
		Map<String, List<WordSentence>> linksBySpell = new HashMap<>();
		List<WordSentence> wordSentenceLinks = Global.getWordSentenceBO().queryAll();
		for (WordSentence link : wordSentenceLinks) {
			String spell = link.getWord().getSpell();
			List<WordSentence> linksOfASpell = linksBySpell.get(spell);
			if (linksOfASpell == null) {
				linksOfASpell = new ArrayList<>();
				linksBySpell.put(spell, linksOfASpell);
			}
			linksOfASpell.add(link);
		}
		return linksBySpell;
	}

	public static List<WordSentence> getSentenceLinksOfAWord(int wordId) {
		List<WordSentence> wordSentenceLinks = Global.getWordSentenceBO().getWordSentencesOfWord(wordId);
		return wordSentenceLinks;
	}

	public static WordVo genWordVO(SentenceStore sentenceStore, Map<String, List<WordSentence>> sentenceLinksBySpell,
			Word wordPo) {
		WordVo wordVo = BeanUtils.makeVO(wordPo, WordVo.class, new String[] { "sentenceDiyItems", "sentences",
				"SynonymVo.meaningItem", "SynonymVo.word", "createTime", "updateTime", "similarWords" });

		// 验证单词是否合乎规格
		if (!isWordValid(wordVo)) {
			invalidCount++;
			Util.println("invalid " + invalidCount + " " + wordVo.getSpell());
		}

		// 添加形近词
		List<WordVo> similarWords = new ArrayList<>(wordPo.getSimilarWords().size());
		for (Word similarWord : wordPo.getSimilarWords()) {
			WordVo vo = new WordVo();
			vo.setSpell(similarWord.getSpell());
			vo.setMeaningStr(similarWord.getMeaningStr());
			similarWords.add(vo);
		}
		wordVo.setSimilarWords(similarWords);

		// 在例句库中搜索该单词的例句
		if (wordVo.isPhrase()) {
			List<SentenceVo> sentenceVos = new ArrayList<>();
			List<WordSentence> links = sentenceLinksBySpell.get(wordVo.getSpell());
			if (links != null) {
				for (WordSentence link : links) {
					SentenceVo sentenceVo = sentenceStore.getSentenceById(link.getId().getSentenceId());
					sentenceVos.add(sentenceVo);
				}
			}
			wordVo.setSentences(sentenceVos);
		} else {
			List<SentenceVo> sentencesFromStore = sentenceStore.getSentencesOfWord(wordVo.getSpell());
			wordVo.setSentences(sentencesFromStore);
		}

		// 每个单词最多只保留6个例句
		List<SentenceVo> sentences = wordVo.getSentences();
		if (wordVo.getSentences().size() > 6) {
			// 把所有例句进行分类
			List<SentenceVo> humanAudioSentences = new ArrayList<>();
			List<SentenceVo> bilingualSentences = new ArrayList<>();
			List<SentenceVo> humanVideoSentences = new ArrayList<>();
			List<SentenceVo> authoritySentences = new ArrayList<>();
			List<SentenceVo> ttsSentences = new ArrayList<>();
			for (SentenceVo sentence : sentences) {
				if (sentence.getTheType().equalsIgnoreCase(Sentence.HUMAN_AUDIO)) {
					humanAudioSentences.add(sentence);
				} else if (sentence.getTheType().equalsIgnoreCase(Sentence.BILINGUAL)) {
					bilingualSentences.add(sentence);
				} else if (sentence.getTheType().equalsIgnoreCase(Sentence.HUMAN_VIDEO)) {
					humanVideoSentences.add(sentence);
				} else if (sentence.getTheType().equalsIgnoreCase(Sentence.AUTHORITY)) {
					authoritySentences.add(sentence);
				} else if (sentence.getTheType().equalsIgnoreCase(Sentence.TTS)) {
					ttsSentences.add(sentence);
				} else {
					log.warn(String.format("发现未知的Sentence Type[%s]", sentence.getTheType()));
				}
			}

			// 对每种例句按照句子长短排序
			sortHumanAudioSentences(humanAudioSentences);
			sortSentencesByLength(humanVideoSentences);
			sortSentencesByLength(bilingualSentences);
			sortSentencesByLength(authoritySentences);
			sortHumanAudioSentences(ttsSentences); //TTS例句采用和真人发音例句一样的排序规则

			// 从每种类型中取出若干例句
			List<SentenceVo> selectedSentences = new ArrayList<>();
			int needCount = 0; // 该类型的例句需要的个数
			needCount += 2;
			for (SentenceVo sentence : humanAudioSentences) {// 原声例句
				if (needCount == 0) {
					break;
				}
				selectedSentences.add(sentence);
				needCount--;
			}
			needCount += 2;
			for (SentenceVo sentence : ttsSentences) {// TTS例句
				if (needCount == 0) {
					break;
				}
				selectedSentences.add(sentence);
				needCount--;
			}
			needCount += 2;
			for (SentenceVo sentence : bilingualSentences) {// 双语例句
				if (needCount == 0) {
					break;
				}
				selectedSentences.add(sentence);
				needCount--;
			}
			needCount += 2;
			for (SentenceVo sentence : humanVideoSentences) {// 视频例句
				if (needCount == 0) {
					break;
				}
				selectedSentences.add(sentence);
				needCount--;
			}
			for (SentenceVo sentence : authoritySentences) {// 权威例句(候补)
				if (needCount == 0) {
					break;
				}
				selectedSentences.add(sentence);
				needCount--;
			}
			sentences.clear();
			sentences.addAll(selectedSentences);

			/**
			 * 如果当前单词没有足够的真人发音例句，则对该单词选中的例句（最多6个）进行TTS
			 */
			if (humanAudioSentences.size() < 2) {
				for (SentenceVo sentence : selectedSentences) {
					if (!sentence.getTheType().equals(Sentence.HUMAN_AUDIO)) {
						SentenceTtsThread.getInstance().schedule(sentence.getId());
					}
				}
			}
		}

		// 对例句进行排序（真人发音->双语->视频->权威）
		Collections.sort(sentences, new Comparator<SentenceVo>() {
			@Override
			public int compare(SentenceVo o1, SentenceVo o2) {
				int priority1 = 0;
				if (o1.getTheType().equalsIgnoreCase(Sentence.HUMAN_AUDIO)) {
					priority1 = 1;
				} else if (o1.getTheType().equalsIgnoreCase(Sentence.BILINGUAL)) {
					priority1 = 2;
				} else if (o1.getTheType().equalsIgnoreCase(Sentence.HUMAN_VIDEO)) {
					priority1 = 3;
				} else if (o1.getTheType().equalsIgnoreCase(Sentence.AUTHORITY)) {
					priority1 = 4;
				}

				int priority2 = 0;
				if (o2.getTheType().equalsIgnoreCase(Sentence.HUMAN_AUDIO)) {
					priority2 = 1;
				} else if (o2.getTheType().equalsIgnoreCase(Sentence.BILINGUAL)) {
					priority2 = 2;
				} else if (o2.getTheType().equalsIgnoreCase(Sentence.HUMAN_VIDEO)) {
					priority2 = 3;
				} else if (o2.getTheType().equalsIgnoreCase(Sentence.AUTHORITY)) {
					priority2 = 2;
				}
				return priority1 - priority2;
			}
		});

		// 检查例句的声音文件是否存在
		for (SentenceVo sentence : sentences) {
			sentence.setSoundFileExists(isSentenceSoundFileExists(sentence));
		}
		return wordVo;
	}

	private static void sortSentencesByLength(List<SentenceVo> humanAudioSentences) {
		Collections.sort(humanAudioSentences, new Comparator<SentenceVo>() {
			@Override
			public int compare(SentenceVo o1, SentenceVo o2) {
				return o1.getEnglish().length() - o2.getEnglish().length();
			}
		});
	}

	/**
	 * 对真人发音例句进行排序，其他例句是按长短进行排序，短句排在前面，但真人发音例句有很多短语例句，太短了，所以要把长度适中的例句排在前面
	 */
	private static void sortHumanAudioSentences(List<SentenceVo> humanAudioSentences) {
		Collections.sort(humanAudioSentences, new Comparator<SentenceVo>() {
			@Override
			public int compare(SentenceVo o1, SentenceVo o2) {
				int length1 = o1.getEnglish().length();
				int length2 = o2.getEnglish().length();
				int priority1;
				if ((length1 >= 25) && (length1 <= 100)) { // 最偏爱中等长度的句子
					priority1 = 3;
				} else if (length1 < 25) { // 其次偏爱短句
					priority1 = 2;
				} else { // 最不喜欢长句
					priority1 = 1;
				}
				int priority2;
				if (length2 >= 25 && length2 <= 100) { // 最偏爱中等长度的句子
					priority2 = 3;
				} else if (length2 < 25) { // 其次偏爱短句
					priority2 = 2;
				} else { // 最不喜欢长句
					priority2 = 1;
				}

				if (priority1 == priority2) {
					if (priority1 == 2) {// 短句，优先选择长度较大的
						return o2.getEnglish().length() - o1.getEnglish().length();
					} else {// 中、长句，优先选择长度较小的
						return o1.getEnglish().length() - o2.getEnglish().length();
					}
				} else {
					return priority2 - priority1;
				}
			}
		});
	}

	private static boolean isSentenceSoundFileExists(SentenceVo sentence) {
		return !sentence.getTheType().equals("human_video"); // 声音文件太多了，检查文件是否存在有些影响性能
		/*
		 * String soundFileName = sentence.getEnglishDigest() +
		 * (sentence.getType().equals(Sentence.HUMAN_VIDEO) ? ".flv" : ".mp3"); File
		 * soundFile = new File(SysParamUtil.getSoundPath() + "/sentence/" +
		 * soundFileName); return soundFile.exists();
		 */
	}
}
