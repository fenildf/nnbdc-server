package beidanci.bo;

import java.util.List;

import org.hibernate.Query;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.Sentence;

@Service("SentenceBO")
@Scope("prototype")
public class SentenceBO extends BaseBo<Sentence> {
	public SentenceBO() {
		setDao(new BaseDao<Sentence>() {
		});
	}

	/**
	 * 获取一页待翻译例句(还没有汉语翻译并且也没有DIY翻译的例句)
	 *
	 * @param firstRow
	 * @param pageSize
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Sentence> getSentencesToTranslate(final int firstRow, final int pageSize, final String sentenceType) {

		Query query = getSession().createQuery(" from Sentence as sentence where theType = '" + sentenceType
				+ "' and chinese = null and not exists elements(sentence.sentenceDiyItems)" + " order by ID ");
		query.setFirstResult(firstRow);
		query.setMaxResults(pageSize);
		return query.list();

	}

	/**
	 * 获取待翻译例句(还没有汉语翻译并且也没有DIY翻译的例句)的数量
	 *
	 * @param sentenceType
	 * @return
	 */
	public int getCountOfSentencesToTranslate(final String sentenceType) {
		String hql = "select count(*) from Sentence as sentence where theType = :sentenceType"
				+ " and chinese = null and not exists elements(sentence.sentenceDiyItems)";
		Query countQuery = getSession().createQuery(hql);
		countQuery.setParameter("sentenceType", sentenceType);

		// 查询记录总数
		int totalCount = ((Long) countQuery.uniqueResult()).intValue();

		return totalCount;
	}

	/**
	 * 获取一页待评选例句(还没有汉语翻译但至少1个DIY翻译的例句)
	 *
	 * @param firstRow
	 * @param pageSize
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Sentence> getSentencesToVote(final int firstRow, final int pageSize, final String sentenceType) {
		String hql = " from Sentence as sentence where theType = '" + sentenceType
				+ "' and chinese = null and exists elements(sentence.sentenceDiyItems)"
				+ "  order by lastDiyUpdateTime desc";
		Query query = getSession().createQuery(hql);
		query.setFirstResult(firstRow);
		query.setMaxResults(pageSize);
		return query.list();

	}

	/**
	 * 获取待评选例句(还没有汉语翻译但至少1个DIY翻译的例句)的数量, 并按最近更新（添加了翻译或对翻译进行了评论或投票）时间排序。
	 *
	 * @param sentenceType
	 * @return
	 */
	public int getCountOfSentencesToVote(final String sentenceType) {
		String hql = "select count(*) from Sentence as sentence where theType = :sentenceType "
				+ " and chinese = null and exists elements(sentence.sentenceDiyItems)";
		Query countQuery = getSession().createQuery(hql);
		countQuery.setParameter("sentenceType", sentenceType);

		// 查询记录总数
		int totalCount = ((Long) countQuery.uniqueResult()).intValue();

		return totalCount;
	}

	public List<Sentence> findAll() {
		return queryAll();
	}

	public Sentence getSentenceByDigest(String digest) {
		Query query = getSession().createQuery("from Sentence where englishDigest=:englishDigest");
		query.setParameter("englishDigest", digest);
		return (Sentence) query.uniqueResult();
	}

}
