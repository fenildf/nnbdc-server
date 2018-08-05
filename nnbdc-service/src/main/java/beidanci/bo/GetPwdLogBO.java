package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.GetPwdLog;

@Service("GetPwdLogBO")
@Scope("prototype")
public class GetPwdLogBO extends BaseBo<GetPwdLog> {
	public GetPwdLogBO() {
		setDao(new BaseDao<GetPwdLog>() {
		});
	}
}
