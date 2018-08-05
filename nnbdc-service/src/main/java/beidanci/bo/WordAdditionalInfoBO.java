package beidanci.bo;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.WordAdditionalInfo;

@Service("WordAdditionalInfoBO")
@Scope("prototype")
public class WordAdditionalInfoBO extends BaseBo<WordAdditionalInfo> {
	public WordAdditionalInfoBO() {
		setDao(new BaseDao<WordAdditionalInfo>() {
		});
	}

	public List<WordAdditionalInfo> findByWordSpell(String wordSpell) {
		Session session = getSession();
		String hql = "  from WordAdditionalInfo where word.spell = :spell ";
		Query query = session.createQuery(hql);
		query.setParameter("spell", wordSpell);
		return query.list();
	}
}
