package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.Level;

@Service("LevelBO")
@Scope("prototype")
public class LevelBO extends BaseBo<Level> {
	public LevelBO() {
		setDao(new BaseDao<Level>() {
		});
	}
}
