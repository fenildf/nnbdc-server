package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.UserCowDungLog;

@Service("UserCowDungLogBO")
@Scope("prototype")
public class UserCowDungLogBO extends BaseBo<UserCowDungLog> {
	public UserCowDungLogBO() {
		setDao(new BaseDao<UserCowDungLog>() {
		});
	}
}
