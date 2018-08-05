package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.HallGroup;

@Service("HallGroupBO")
@Scope("prototype")
public class HallGroupBO extends BaseBo<HallGroup> {
	public HallGroupBO() {
		setDao(new BaseDao<HallGroup>() {
		});
	}
}
