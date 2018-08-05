package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.LoginLog;

@Service("LoginLogBO")
@Scope("prototype")
public class LoginLogBO extends BaseBo<LoginLog> {
	public LoginLogBO() {
		setDao(new BaseDao<LoginLog>() {
		});
	}
}