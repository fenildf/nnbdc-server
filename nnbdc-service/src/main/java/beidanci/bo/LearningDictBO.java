package beidanci.bo;

import java.util.List;

import javax.persistence.Query;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.LearningDict;
import beidanci.po.User;

@Service("LearningDictBO")
@Scope("prototype")
public class LearningDictBO extends BaseBo<LearningDict> {
	public LearningDictBO() {
		setDao(new BaseDao<LearningDict>() {
		});
	}

	public List<LearningDict> getLearningDictsOfUser(User user) {
		String hql = "from LearningDict where user=:user order by dict.name asc";
		Query query = getSession().createQuery(hql);
		query.setParameter("user", user);
		return query.getResultList();
	}
}
