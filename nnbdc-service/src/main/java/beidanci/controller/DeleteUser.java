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
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/deleteUser.do")
public class DeleteUser {
	@RequestMapping(method = RequestMethod.GET)
	public String handle(HttpServletRequest request, HttpServletResponse response) throws SQLException, NamingException,
			ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		String userToDelete = params.get("userToDelete")[0];

		// 删除用户
		User user = Global.getUserBO().findById(userToDelete);
		boolean deleteSucc;
		String description;
		if (user != null) {
			Global.getUserBO().deleteUser(user);
			deleteSucc = true;
			description = String.format("删除用户[%s]成功", user.getUserName());
		} else {
			deleteSucc = false;
			description = String.format("用户[%s]不存在", userToDelete);
		}

		// Send result back to client.
		Util.sendBooleanResponse(deleteSucc, description, null, response);

		return null;
	}

}
