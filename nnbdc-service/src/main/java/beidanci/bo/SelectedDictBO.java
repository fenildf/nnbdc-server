package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.SelectedDict;

@Service("SelectedDictBO")
@Scope("prototype")
public class SelectedDictBO extends BaseBo<SelectedDict> {
	public SelectedDictBO() {
		setDao(new BaseDao<SelectedDict>() {
		});
	}
}
