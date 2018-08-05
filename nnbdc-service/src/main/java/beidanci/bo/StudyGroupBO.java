package beidanci.bo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.Global;
import beidanci.dao.BaseDao;
import beidanci.po.*;
import beidanci.util.Util;

@Service("StudyGroupBO")
@Scope("prototype")
public class StudyGroupBO extends BaseBo<StudyGroup> {
	private static final Logger log = LoggerFactory.getLogger(StudyGroup.class);

	public StudyGroupBO() {
		setDao(new BaseDao<StudyGroup>() {
		});
	}

	public List<StudyGroup> findByGroupName(String groupName) {
		StudyGroup exam = new StudyGroup();
		exam.setGroupName(groupName);
		baseDao.setPreciseEntity(exam);
		return queryAll();
	}

	public List<StudyGroup> findAll() {
		return queryAll();
	}

	public String dismissStudyGroup(int groupID, int userId) throws IllegalAccessException {
		// 验证用户是否是小组创建者
		StudyGroupBO groupDAO = Global.getStudyGroupBO();
		StudyGroup group = groupDAO.findById(groupID);

		if (!group.getCreator().getId().equals(userId)) {
			return "只有该组的创建者才能解散小组";
		}

		// 删除组管理员
		List<User> managers = new ArrayList<User>(group.getManagers());
		for (User manager : managers) {
			manager.exitGroup(group.getId());
		}
		group.getManagers().clear();
		groupDAO.updateEntity(group);

		// 删除组员
		List<User> members = new ArrayList<User>(group.getUsers());
		for (User member : members) {
			member.exitGroup(group.getId());
		}
		group.getUsers().clear();
		groupDAO.updateEntity(group);

		// 删除小组的日结记录
		for (StudyGroupSnapshotDaily snapshot : group.getSnapshotDailys()) {
			Global.getStudyGroupSnapshotDailyBO().deleteEntity(snapshot);
		}
		group.getSnapshotDailys().clear();
		groupDAO.updateEntity(group);

		// 删除小组的帖子
		for (StudyGroupPost post : group.getStudyGroupPosts()) {
			for (StudyGroupPostReply reply : post.getStudyGroupPostReplies()) {
				Global.getStudyGroupPostReplyBO().deleteEntity(reply);
			}
			post.getStudyGroupPostReplies().clear();
			Global.getStudyGroupPostBO().updateEntity(post);
			Global.getStudyGroupPostBO().deleteEntity(post);
		}
		group.getStudyGroupPosts().clear();
		groupDAO.updateEntity(group);

		// 删除组
		groupDAO.deleteEntity(group);

		log.info(String.format("用户[%s]解散了小组[%s]", Util.getNickNameOfUser(group.getCreator()), group.getGroupName()));
		return null;
	}
}
