package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.Daka;

@Service("DakaBO")
@Scope("prototype")
public class DakaBO extends BaseBo<Daka> {
	public DakaBO() {
		setDao(new BaseDao<Daka>() {
		});
	}
}
