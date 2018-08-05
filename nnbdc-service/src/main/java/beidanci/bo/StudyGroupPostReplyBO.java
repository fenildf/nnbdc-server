package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.StudyGroupPostReply;

@Service("StudyGroupPostReplyBO")
@Scope("prototype")
public class StudyGroupPostReplyBO extends BaseBo<StudyGroupPostReply> {
	public StudyGroupPostReplyBO() {
		setDao(new BaseDao<StudyGroupPostReply>() {
		});
	}
}
