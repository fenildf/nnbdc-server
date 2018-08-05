package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.ForumPost;

@Service("ForumPostBO")
@Scope("prototype")
public class ForumPostBO extends BaseBo<ForumPost> {
	public ForumPostBO() {
		setDao(new BaseDao<ForumPost>() {
		});
	}

	public void increaseBrowseCount(ForumPost post) throws IllegalAccessException {
		post.setBrowseCount(post.getBrowseCount() + 1);
		updateEntity(post, false);
	}
}
