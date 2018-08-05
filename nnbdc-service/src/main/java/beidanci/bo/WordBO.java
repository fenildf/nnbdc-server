package beidanci.bo;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.hibernate.Query;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.Global;
import beidanci.dao.BaseDao;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.MeaningItem;
import beidanci.po.Word;
import beidanci.po.WordSentence;
import beidanci.store.SentenceStore;
import beidanci.store.WordStore;
import beidanci.util.SysParamUtil;
import beidanci.util.Utils;
import beidanci.vo.MeaningItemVo;
import beidanci.vo.WordVo;

@Service("WordBO")
@Scope("prototype")
public class WordBO extends BaseBo<Word> {
	public WordBO() {
		setDao(new BaseDao<Word>() {
		});
	}

	/**
	 * 获取所有单词，并按照单词拼写升序排列(不区分大小写)
	 *
	 * @return
	 */
	public List<Word> getAllWords() {
		String hql = "from Word order by lower(spell) asc";
		Query query = getSession().createQuery(hql);
		return query.list();
	}

	/**
	 * 获取所有单词，并按照单词拼写升序排列(不区分大小写)
	 *
	 * @return
	 */
	public List<WordVo> getAllWordVos() {
		List<Word> allWords = getAllWords();
		List<WordVo> vos = new ArrayList<>(allWords.size());
		Map<String, List<WordSentence>> sentenceLinksBySpell = WordStore.getSentenceLinksOfAllWords();
		for (Word wordPO : allWords) {
			WordVo vo = WordStore.genWordVO(SentenceStore.getInstance(), sentenceLinksBySpell, wordPO);
			vos.add(vo);
		}
		return vos;
	}

	public WordVo getWordVo(int wordId) {
		Word wordPO = findById(wordId);

		List<WordSentence> wordSentences = WordStore.getSentenceLinksOfAWord(wordId);
		Map<String, List<WordSentence>> sentenceLinksBySpell = new HashMap<>();
		sentenceLinksBySpell.put(wordPO.getSpell(), wordSentences);

		WordVo vo = WordStore.genWordVO(SentenceStore.getInstance(), sentenceLinksBySpell, wordPO);
		return vo;
	}

	private MeaningItemVo getMeaningItemVoFromList(int meaningItemId, List<MeaningItemVo> meaningItems) {
		for (MeaningItemVo itemVo : meaningItems) {
			if (itemVo.getId() != null && itemVo.getId().equals(meaningItemId)) {
				return itemVo;
			}
		}
		return null;
	}

	public String updateWord(WordVo wordVo) throws IllegalAccessException, InvalidMeaningFormatException,
			EmptySpellException, ParseException, IOException {
		if (wordVo.getId() == null) {
			return "单词ID不能为null";
		}

		WordVo existingWord = WordStore.getInstance().getWordBySpell(wordVo.getSpell());
		if (existingWord != null && !existingWord.getId().equals(wordVo.getId())) {
			return String.format("单词%s已存在", wordVo.getSpell());
		}

		Word word = findById(wordVo.getId());

		// 删除被删除的meaningItems
		for (Iterator<MeaningItem> i = word.getMeaningItems().iterator(); i.hasNext();) {
			MeaningItem item = i.next();
			if (getMeaningItemVoFromList(item.getId(), wordVo.getMeaningItems()) == null) {
				i.remove();
				Global.getMeaningItemBO().deleteEntity(item);
			}
		}

		// 更新被修改的meaningItems
		for (MeaningItem item : word.getMeaningItems()) {
			MeaningItemVo itemVo = getMeaningItemVoFromList(item.getId(), wordVo.getMeaningItems());
			if (itemVo != null) {
				item.setCiXing(itemVo.getCiXing());
				item.setMeaning(itemVo.getMeaning());
				Global.getMeaningItemBO().updateEntity(item);
			}
		}

		// 添加新增的meaningItems
		for (MeaningItemVo itemVo : wordVo.getMeaningItems()) {
			if (itemVo.getId() == null) {
				MeaningItem item = new MeaningItem();
				item.setCiXing(itemVo.getCiXing());
				item.setMeaning(itemVo.getMeaning());
				item.setWord(word);
				Global.getMeaningItemBO().createEntity(item);
				word.getMeaningItems().add(item);
			}
		}

		// 更新单词的拼写
		String oldSpell = word.getSpell();
		word.setSpell(wordVo.getSpell());

		Global.getWordBO().updateEntity(word);

		// 更新声音文件（重命名）
		if (!oldSpell.equalsIgnoreCase(wordVo.getSpell())) {
			File oldSoundFile = new File(
					SysParamUtil.getSoundPath() + "/" + Utils.getFileNameOfWordSound(oldSpell) + ".mp3");
			File newSoundFile = new File(
					SysParamUtil.getSoundPath() + "/" + Utils.getFileNameOfWordSound(wordVo.getSpell()) + ".mp3");
			oldSoundFile.renameTo(newSoundFile);

			oldSoundFile = new File(
					SysParamUtil.getSoundPath() + "/" + Utils.getFileNameOfWordSound(oldSpell) + ".oga");
			newSoundFile = new File(
					SysParamUtil.getSoundPath() + "/" + Utils.getFileNameOfWordSound(wordVo.getSpell()) + ".oga");
			if (oldSoundFile.exists()) {
				oldSoundFile.renameTo(newSoundFile);
			}
		}

		// 刷新缓存
		WordStore.getInstance().reloadWord(word.getId(), oldSpell);
		return null;
	}

}
