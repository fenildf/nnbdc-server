package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.SysParam;

@Service("SysParamBO")
@Scope("prototype")
public class SysParamBO extends BaseBo<SysParam> {
	public SysParamBO() {
		setDao(new BaseDao<SysParam>() {
		});
	}
}
