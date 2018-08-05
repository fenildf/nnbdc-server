package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.ForumPostReply;

@Service("ForumPostReplyBO")
@Scope("prototype")
public class ForumPostReplyBO extends BaseBo<ForumPostReply> {
	public ForumPostReplyBO() {
		setDao(new BaseDao<ForumPostReply>() {
		});
	}
}
