package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.MeaningItem;

@Service("MeaningItemBO")
@Scope("prototype")
public class MeaningItemBO extends BaseBo<MeaningItem> {
	public MeaningItemBO() {
		setDao(new BaseDao<MeaningItem>() {
		});
	}
}
