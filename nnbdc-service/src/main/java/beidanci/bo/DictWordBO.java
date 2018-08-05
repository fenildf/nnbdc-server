package beidanci.bo;

import java.io.IOException;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.Global;
import beidanci.dao.BaseDao;
import beidanci.dao.PaginationResults;
import beidanci.po.Dict;
import beidanci.po.DictWord;
import beidanci.po.DictWordId;
import beidanci.po.Word;
import beidanci.vo.Result;

@Service("DictWordBO")
@Scope("prototype")
public class DictWordBO extends BaseBo<DictWord> {
	public DictWordBO() {
		setDao(new BaseDao<DictWord>() {
		});
	}

	/**
	 * 获取指定的单词在指定词典中的顺序号（基于md5排序）
	 *
	 * @return
	 */
	public int getOrderOfWord(Integer dictId, Integer wordId) {
		DictWordId id = new DictWordId(dictId, wordId);
		DictWord dictWord = findById(id);
		return dictWord == null ? -1 : dictWord.getMd5IndexNo();
	}

	/**
	 * 从指定单词书的指定位置获取单词
	 *
	 * @param dictName
	 * @return
	 */
	public Word getWordOfOrder(Integer dictId, int indexNo) {
		String hql = "from DictWord where dict.id=:dictId and md5IndexNo=:md5IndexNo";
		Query query = getSession().createQuery(hql);
		query.setParameter("dictId", dictId);
		query.setParameter("md5IndexNo", indexNo);
		query.setCacheable(true);
		DictWord dictWord = (DictWord) query.uniqueResult();
		return dictWord.getWord();
	}

	public int getWordCountOfDict(String dictName) {
		// 查询记录总数
		Session session = getSession();
		String hql = "select count(0) from DictWord where dictName=:dictName";
		Query query = session.createQuery(hql);
		query.setParameter("dictName", dictName);
		query.setCacheable(true);
		int total = ((Long) query.uniqueResult()).intValue();
		return total;
	}

	/**
	 * 读取指定单词书中的所有单词
	 *
	 * @param dictName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PaginationResults<DictWord> getDictWords(String dictName, int pageNo, int pageSize, String orderBy) {

		// 查询记录总数
		int total = getWordCountOfDict(dictName);

		// 查询一页数据
		Session session = getSession();
		String sql = "from DictWord where dictName=:dictName " + (orderBy == null ? "" : " order by " + orderBy);
		Query query = session.createQuery(sql);
		query.setParameter("dictName", dictName);
		int pageCount = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
		pageNo = pageNo > pageCount ? pageCount : pageNo;
		int offset = (pageSize * (pageNo - 1));
		query.setFirstResult(offset);
		query.setMaxResults(pageSize);
		List<DictWord> dictWords = query.list();

		PaginationResults<DictWord> result = new PaginationResults<DictWord>();
		result.setTotal((int) total);
		result.setRows(dictWords);
		return result;
	}

	/**
	 * 获取自定义单词书中的所有单词
	 */
	/*
	 * public List<DictWord> getWordsOfDict(String dictName) throws IOException {
	 * DictWord exam = new DictWord();
	 * exam.setDict(Global.getDictBO().findById(dictName));
	 * baseDao.buildPreciseEntity(exam); List<DictWord> dictWords = queryAll();
	 * 
	 * return dictWords; }
	 */
	@SuppressWarnings("unchecked")
	public List<String> getWordSpellsOfDict(Integer dictId) throws IOException {
		Session session = getSession();
		String sql = "select w.spell from dict_word dw left join word w on dw.wordId=w.id where dw.dictId=:dictId ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("dictId", dictId);

		List<String> dictWords = (List<String>) query.list();
		return dictWords;
	}

	/**
	 * 向指定单词书中添加指定的单词
	 */
	public Result<Object> addWord(Integer dictId, Integer wordId, boolean ignoreIfExisted)
			throws IOException, IllegalAccessException {
		// 判断单词书是否可编辑
		Dict dict = Global.getDictBO().findById(dictId);
		if (dict.getIsReady()) {
			return new Result<Object>(false, "单词书处于就绪状态，不可编辑", null);
		}

		// 判断单词是否已经在单词书中了
		DictWordId id = new DictWordId(dictId, wordId);
		DictWord existingWord = findById(id);
		if (existingWord != null) {
			if (ignoreIfExisted) {
				return Result.SUCCESS;
			} else {
				return new Result<Object>(false, String.format("%s已经在该单词书中了，不能再次添加", existingWord.getWord().getSpell()),
						null);
			}
		} else {
			// 把单词添加到单词书
			DictWord dictWord = new DictWord();
			dictWord.setId(id);
			dictWord.setDict(dict);
			dictWord.setWord(Global.getWordBO().findById(wordId));
			createEntity(dictWord);

			dict.setWordCount(dict.getWordCount() + 1);
			Global.getDictBO().updateEntity(dict);

			return Result.SUCCESS;
		}
	}

	/**
	 * 把单词从源单词书导入到目标单词书
	 */
	public Result<Object> importFromDict(int fromDictId, int toDictId) throws IOException, IllegalAccessException {
		// 判断单词书是否可编辑
		Dict toDict = Global.getDictBO().findById(toDictId);
		if (toDict.getIsReady()) {
			return new Result<Object>(false, "单词书处于就绪状态，不可编辑", null);
		}

		int count = 0;
		Dict fromDict = Global.getDictBO().findById(fromDictId);
		for (DictWord dictWord : fromDict.getDictWords()) {
			Result result = addWord(toDictId, dictWord.getWord().getId(), false);
			if (result == Result.SUCCESS) {
				count++;
			}
		}

		return new Result<>(true, "导入了" + count + "个单词", null);
	}

	/**
	 * 从指定单词书中删除指定的单词
	 */
	public Result<Object> deleteWord(Integer dictId, Integer wordId) throws IOException, IllegalAccessException {
		Dict dict = Global.getDictBO().findById(dictId);
		if (dict.getIsReady()) {
			return new Result<Object>(false, "单词书处于就绪状态，不可编辑", null);
		}

		DictWordId id = new DictWordId(dictId, wordId);
		deleteById(id);

		dict.setWordCount(dict.getWordCount() - 1);
		Global.getDictBO().updateEntity(dict);
		return Result.SUCCESS;
	}

	/**
	 * 清空指定单词书中的所有单词
	 */
	public Result<Object> clearWordsOfDict(int dictId) throws IOException, IllegalAccessException {
		Dict dict = Global.getDictBO().findById(dictId);
		if (dict.getIsReady()) {
			return new Result<Object>(false, "单词书处于就绪状态，不可编辑", null);
		}

		DictWord exam = new DictWord();
		exam.setDict(dict);
		baseDao.setPreciseEntity(exam);
		List<DictWord> words = queryAll();
		for (DictWord word : words) {
			deleteEntity(word);
		}

		dict.setWordCount(0);
		Global.getDictBO().updateEntity(dict);

		return Result.SUCCESS;
	}

}