package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.bo.StudyGroupBO;
import beidanci.po.StudyGroup;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
public class CreateStudyGroup {
	private static Logger log = LoggerFactory.getLogger(CreateStudyGroup.class);

	@RequestMapping("/createStudyGroup.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public String handle(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		// Get input.
		Map<String, String[]> paramMap = request.getParameterMap();
		String groupName = paramMap.get("groupName")[0].trim();
		String groupTitle = paramMap.get("groupTitle")[0];
		String groupRemark = paramMap.get("groupRemark")[0];

		// 验证小组名.
		String errorMsg = "";
		if (groupName.length() == 0) {
			errorMsg += "小组名不能为空\n";
		}
		StudyGroupBO studyGroupDAO = Global.getStudyGroupBO();
		if (studyGroupDAO.findByGroupName(groupName).size() != 0) {
			errorMsg += "同名小组已存在\n";
		}

		// 验证用户是否是游客
		if (user.getUserName().startsWith("guest")) {
			errorMsg += "您是游客，不能创建学习小组";
		}

		// 验证用户是否已经属于某个小组
		if (user.getStudyGroups().size() > 0) {
			errorMsg += "您已经属于某个小组了，不能再创建新的小组";
		}

		if (errorMsg.equals("")) {

			// 保存小组信息到数据库.
			StudyGroup studyGroup = new StudyGroup();
			studyGroup.setCreateTime(new Timestamp(new Date().getTime()));
			studyGroup.setCreator(user);
			studyGroup.setGroupName(groupName);
			studyGroup.setGroupTitle(groupTitle);
			studyGroup.setGroupRemark(groupRemark);
			studyGroup.setStudyGroupGrade(Global.getStudyGroupGradeBO().findById(1));
			studyGroup.setCowDung(0);

			// 将创建者自身添加到小组.
			studyGroup.setUsers(new ArrayList<User>());
			studyGroup.getUsers().add(user);

			// 将创建者设置为小组管理员.
			studyGroup.setManagers(new ArrayList<User>());
			studyGroup.getManagers().add(user);
			studyGroupDAO.createEntity(studyGroup);

			log.info(String.format("学习小组[%s]创建成功, 创建人是[%s] ", groupName, user.getUserName()));
		} else {
			log.info(String.format("学习小组[%s]创建失败，详情：[%s] ", groupName, errorMsg));
		}

		// Send result back to client.
		Util.sendBooleanResponse(errorMsg.equals(""), errorMsg, null, response);

		return null;
	}
}
