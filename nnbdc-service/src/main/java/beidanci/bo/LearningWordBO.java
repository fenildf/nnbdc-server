package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.LearningWord;

@Service("LearningWordBO")
@Scope("prototype")
public class LearningWordBO extends BaseBo<LearningWord> {
	public LearningWordBO() {
		setDao(new BaseDao<LearningWord>() {
		});
	}
}
