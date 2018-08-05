package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.Global;
import beidanci.bo.StudyGroupBO;
import beidanci.po.StudyGroup;
import beidanci.util.Util;

@Controller
@RequestMapping("/editStudyGroup.do")
public class EditStudyGroup {
	private static Logger log = LoggerFactory.getLogger(CreateStudyGroup.class);

	@RequestMapping(method = RequestMethod.POST)
	public String handle(HttpServletRequest request, HttpServletResponse response) throws SQLException, NamingException,
			ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		// Get input.
		Map<String, String[]> paramMap = request.getParameterMap();
		int groupID = Integer.parseInt(paramMap.get("groupId")[0]);
		String groupName = paramMap.get("groupName")[0].trim();
		String groupTitle = paramMap.get("groupTitle")[0];
		String groupRemark = paramMap.get("groupRemark")[0];

		// 获取要修改的学习小组
		StudyGroupBO studyGroupDAO = Global.getStudyGroupBO();
		StudyGroup studyGroup = studyGroupDAO.findById(groupID);

		// 验证小组名.
		String errorMsg = "";
		if (groupName.length() == 0) {
			errorMsg += "小组名不能为空\n";
		}
		if (!studyGroup.getGroupName().equalsIgnoreCase(groupName)
				&& studyGroupDAO.findByGroupName(groupName).size() != 0) {
			errorMsg += "同名小组已存在\n";
		}

		if (errorMsg.equals("")) {

			// 保存小组信息到数据库.
			studyGroup.setGroupName(groupName);
			studyGroup.setGroupTitle(groupTitle);
			studyGroup.setGroupRemark(groupRemark);
			studyGroupDAO.updateEntity(studyGroup);

			log.info(String.format("学习小组[%s]修改成功", groupName));
		} else {
			log.info(String.format("学习小组[%s]修改失败，详情：[%s] ", groupName, errorMsg));
		}

		// Send result back to client.
		Util.sendBooleanResponse(errorMsg.equals(""), errorMsg, null, response);

		return null;
	}
}
