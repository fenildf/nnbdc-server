package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.StudyGroupPost;

@Service("StudyGroupPostBO")
@Scope("prototype")
public class StudyGroupPostBO extends BaseBo<StudyGroupPost> {
	public StudyGroupPostBO() {
		setDao(new BaseDao<StudyGroupPost>() {
		});
	}

	public void increaseBrowseCount(StudyGroupPost post) throws IllegalAccessException {
		post.setBrowseCount(post.getBrowseCount() + 1);
		updateEntity(post, false);
	}
}
