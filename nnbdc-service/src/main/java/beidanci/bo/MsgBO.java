package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.Msg;

@Service("MsgBO")
@Scope("prototype")
public class MsgBO extends BaseBo<Msg> {
	public MsgBO() {
		setDao(new BaseDao<Msg>() {
		});
	}
}
