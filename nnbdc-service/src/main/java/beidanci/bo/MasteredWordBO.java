package beidanci.bo;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.MasteredWord;
import beidanci.po.User;

@Service("MasteredWordBO")
@Scope("prototype")
public class MasteredWordBO extends BaseBo<MasteredWord> {
	public MasteredWordBO() {
		setDao(new BaseDao<MasteredWord>() {
		});
	}

	public List<MasteredWord> findByUser(User user) {
		MasteredWord exam = new MasteredWord();
		exam.setUser(user);
		baseDao.setPreciseEntity(exam);
		return queryAll();
	}
}
