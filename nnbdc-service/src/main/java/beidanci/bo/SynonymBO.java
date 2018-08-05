package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.Synonym;

@Service("SynonymBO")
@Scope("prototype")
public class SynonymBO extends BaseBo<Synonym> {
	public SynonymBO() {
		setDao(new BaseDao<Synonym>() {
		});
	}
}
