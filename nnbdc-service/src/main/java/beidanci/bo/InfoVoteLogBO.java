package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.InfoVoteLog;

@Service("InfoVoteLogBO")
@Scope("prototype")
public class InfoVoteLogBO extends BaseBo<InfoVoteLog> {
	public InfoVoteLogBO() {
		setDao(new BaseDao<InfoVoteLog>() {
		});
	}
}
