package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.Global;
import beidanci.bo.StudyGroupBO;
import beidanci.po.StudyGroup;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/addStudyGroupManager.do")
public class AddStudyGroupManager {
	@RequestMapping(method = RequestMethod.GET)
	public String handle(HttpServletRequest request, HttpServletResponse response) throws SQLException, NamingException,
			ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		int groupID = Integer.parseInt(params.get("groupID")[0]);
		String userName = params.get("userName")[0];

		// 把用户添加到组管理员
		StudyGroupBO groupDAO = Global.getStudyGroupBO();
		StudyGroup group = groupDAO.findById(groupID);
		User user = Global.getUserBO().findById(userName);
		group.getManagers().add(user);
		groupDAO.updateEntity(group);

		// Send result back to client.
		Util.sendBooleanResponse(true, null, null, response);

		return null;
	}

}
