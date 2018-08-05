package beidanci.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import beidanci.Global;
import beidanci.po.Sentence;
import beidanci.util.Util;
import beidanci.vo.SentenceVo;

/**
 * 管理系统中所有例句
 *
 * @author Administrator
 */
public class SentenceStore {
	private static final Logger log = LoggerFactory.getLogger(SentenceStore.class);

	private Map<String, List<SentenceVo>> sentencesByWord = new HashMap<>();

	private Map<Integer, SentenceVo> sentencesById = new HashMap<>();

	private static SentenceStore instance;

	private SentenceStore() {

	}

	public static SentenceStore getInstance() {
		if (instance == null) {
			synchronized (SentenceStore.class) {
				if (instance == null) {
					SentenceStore store = new SentenceStore();
					store.init();
					instance = store;
				}
			}
		}
		return instance;
	}

	/**
	 * 从数据库加载所有例句，并在内存中生成相应数据结构
	 */
	private void init() {
		long startTime = System.currentTimeMillis();
		log.info("正在初始化例句管理器...");
		List<Sentence> sentences = Global.getSentenceBO().findAll();
		long endTime = System.currentTimeMillis();
		log.info(String.format("从数据库中读取例句完成，耗时[%ds] 例句数[%d]", (endTime - startTime) / 1000, sentences.size()));
		int count = 0;
		for (Sentence sentence : sentences) {
			// 为避免占用过多内存，只随机加载大约50%的例句
			/*
			 * if (Math.random() > 0.5 && !sentence.getTheType().equals("human_audio") &&
			 * !sentence.getTheType().equals("human_video")) { continue; }
			 */

			String english = sentence.getEnglish();
			SentenceVo vo = new SentenceVo(sentence.getId(), english, sentence.getChinese(), sentence.getTheType(),
					sentence.getEnglishDigest(), null);

			addSentence(vo);

			count++;
			if (count % 1000 == 0) {
				log.info(count + " sentences loaded.");
			}
		}
		endTime = System.currentTimeMillis();
		log.info(String.format("例句管理器初始化完成，耗时[%ds] 例句数[%d/%d] 单词数[%d]", (endTime - startTime) / 1000, count,
				sentences.size(), sentencesByWord.keySet().size()));
	}

	public void addSentence(SentenceVo sentence) {
		// 把例句加入到Map（按ID）
		sentencesById.put(sentence.getId(), sentence);

		// 把例句拆分成若干单词，并加入到Map(按单词)
		String[] words = sentence.getEnglish().split(" |\\.|,");
		for (String spell : words) {
			if (Util.isStringEnglishWord(spell)) {
				if (!sentencesByWord.containsKey(spell)) {
					List<SentenceVo> sentenceVos = new ArrayList<SentenceVo>();
					sentencesByWord.put(spell, sentenceVos);
				}
				sentencesByWord.get(spell).add(sentence);
			}
		}
	}

	public List<SentenceVo> getSentencesOfWord(String spell) {
		List<SentenceVo> sentences = new ArrayList<SentenceVo>();
		for (String word : Util.getVariantsOfWord(spell)) {
			List<SentenceVo> sents = sentencesByWord.get(word);
			if (sents != null) {
				sentences.addAll(sents);
			}
		}
		return sentences;
	}

	public SentenceVo getSentenceById(int id) {
		return sentencesById.get(id);
	}
}
