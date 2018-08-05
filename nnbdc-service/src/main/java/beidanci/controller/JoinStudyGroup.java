package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.bo.StudyGroupBO;
import beidanci.po.StudyGroup;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/joinStudyGroup.do")
public class JoinStudyGroup {
	private static Logger log = LoggerFactory.getLogger(JoinStudyGroup.class);

	@RequestMapping
	public String handle(HttpServletRequest request, HttpServletResponse response, int groupId) throws SQLException,
			NamingException, ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		// Get input.
		StudyGroupBO studyGroupDAO = Global.getStudyGroupBO();
		StudyGroup studyGroup = studyGroupDAO.findById(groupId);

		String errMsg = "";

		// 验证用户是否是游客
		if (user.getUserName().startsWith("guest")) {
			errMsg = "您是游客，不能加入学习小组";
		}
		// 验证小组人数是否已满
		else if (studyGroup.getUsers().size() >= studyGroup.getStudyGroupGrade().getMaxUserCount()) {
			errMsg = "该小组人数已满";
		}
		// 判断和用户是否已经在某个小组中
		else if (user.getStudyGroups().size() > 0) {
			StudyGroup myStudyGroup = new LinkedList<StudyGroup>(user.getStudyGroups()).get(0);
			if (myStudyGroup.getId().equals(groupId)) {
				errMsg = String.format("您已经是学习小组【%s】中的成员", myStudyGroup.getGroupName());
			} else {
				errMsg = String.format("您已经是学习小组【%s】中的成员，不能再加入其他小组", myStudyGroup.getGroupName());
			}
		}

		if (errMsg.equals("")) {
			// 将用户添加到小组.
			studyGroup.getUsers().add(user);
			studyGroupDAO.updateEntity(studyGroup);

			log.info(String.format("用户[%s]成功加入小组[%s] ", Util.getNickNameOfUser(user), studyGroup.getGroupName()));
		} else {
			log.info(String.format("用户[%s]加入小组失败，详情：[%s] ", Util.getNickNameOfUser(user), errMsg));
		}

		// Send result back to client.
		Util.sendBooleanResponse(errMsg.equals(""), errMsg, null, response);

		return null;
	}
}
