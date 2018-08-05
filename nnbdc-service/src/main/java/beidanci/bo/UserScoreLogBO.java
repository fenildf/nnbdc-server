package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.UserScoreLog;

@Service("UserScoreLogBO")
@Scope("prototype")
public class UserScoreLogBO extends BaseBo<UserScoreLog> {
	public UserScoreLogBO() {
		setDao(new BaseDao<UserScoreLog>() {
		});
	}
}
