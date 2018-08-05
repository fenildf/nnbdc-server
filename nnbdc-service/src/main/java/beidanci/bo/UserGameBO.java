package beidanci.bo;

import java.util.List;

import org.hibernate.Query;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.UserGame;

@Service("UserGameBO")
@Scope("prototype")
public class UserGameBO extends BaseBo<UserGame> {
	public UserGameBO() {
		setDao(new BaseDao<UserGame>() {
		});
	}

	@SuppressWarnings("unchecked")
	public List<UserGame> getUserGamesWithTopScore(final int count) {
		Query query = getSession().createQuery(
				" from UserGame where user.userName not like 'guest%' and user.userName not like 'guess%' and user.userName not like '游客%'"
						+ " order by Score desc ");
		query.setCacheable(true);
		query.setFirstResult(0);
		query.setMaxResults(count);
		return query.list();
	}

}
