package beidanci.bo;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.Global;
import beidanci.dao.BaseDao;
import beidanci.po.WordSentence;

@Service("WordSentenceBO")
@Scope("prototype")
public class WordSentenceBO extends BaseBo<WordSentence> {
	public WordSentenceBO() {
		setDao(new BaseDao<WordSentence>() {
		});
	}

	public List<WordSentence> getWordSentencesOfWord(int wordId) {
		WordSentence exam = new WordSentence();
		exam.setWord(Global.getWordBO().findById(wordId));
		getDAO().setPreciseEntity(exam);
		return queryAll();
	}
}
