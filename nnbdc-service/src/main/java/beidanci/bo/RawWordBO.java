package beidanci.bo;

import java.util.List;

import org.hibernate.Query;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.RawWord;
import beidanci.po.User;

@Service("RawWordBO")
@Scope("prototype")
public class RawWordBO extends BaseBo<RawWord> {
	public RawWordBO() {
		setDao(new BaseDao<RawWord>() {
		});
	}

	@SuppressWarnings("unchecked")
	public List<RawWord> getPage(final User user, final int firstRow, final int pageSize) {

		Query query = getSession().createQuery(" from RawWord where user = ?" + " order by ID desc");
		query.setParameter(0, user);
		query.setFirstResult(firstRow);
		query.setMaxResults(pageSize);
		return (List<RawWord>) query.list();

	}

	public RawWord findRawWordOfUser_ByWordId(User user, Integer wordId) {
		Query query = getSession().createQuery(" from RawWord where user=:user and word.id = :wordId");
		query.setParameter("wordId", wordId);
		query.setParameter("user", user);
		return (RawWord) query.uniqueResult();
	}

	public List<RawWord> findByUser(User user) {
		RawWord exam = new RawWord();
		exam.setUser(user);
		baseDao.setPreciseEntity(exam);
		return queryAll();
	}
}
