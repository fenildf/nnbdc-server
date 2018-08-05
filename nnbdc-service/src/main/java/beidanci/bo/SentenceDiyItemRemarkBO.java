package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.SentenceDiyItemRemark;

@Service("SentenceDiyItemRemarkBO")
@Scope("prototype")
public class SentenceDiyItemRemarkBO extends BaseBo<SentenceDiyItemRemark> {
	public SentenceDiyItemRemarkBO() {
		setDao(new BaseDao<SentenceDiyItemRemark>() {
		});
	}
}
