package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.Forum;

@Service("ForumBO")
@Scope("prototype")
public class ForumBO extends BaseBo<Forum> {
	public ForumBO() {
		setDao(new BaseDao<Forum>() {
		});
	}

	public Forum findByName(String name) {
		Forum exam = new Forum();
		exam.setName(name);
		baseDao.setPreciseEntity(exam);
		return queryUnique();
	}
}
