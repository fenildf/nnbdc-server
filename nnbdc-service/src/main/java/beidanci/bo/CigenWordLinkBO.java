package beidanci.bo;

import java.util.List;

import javax.persistence.Query;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.CigenWordLink;

@Service("CigenWordLinkBO")
@Scope("prototype")
public class CigenWordLinkBO extends BaseBo<CigenWordLink> {
	public CigenWordLinkBO() {
		setDao(new BaseDao<CigenWordLink>() {
		});
	}

	public List<CigenWordLink> findByWordId(Integer wordId) {
		String hql = "from CigenWordLink where id.wordId = :wordId";
		Query query = getSession().createQuery(hql);
		query.setParameter("wordId", wordId);
		return query.getResultList();
	}
}
