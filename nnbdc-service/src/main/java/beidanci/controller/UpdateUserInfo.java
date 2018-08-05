package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.bo.UserBO;
import beidanci.po.User;
import beidanci.util.EmojiFilter;
import beidanci.util.UserSorter;
import beidanci.util.Util;

@Controller
@RequestMapping("/updateUserInfo.do")
public class UpdateUserInfo {
	private static Logger log = LoggerFactory.getLogger(UpdateUserInfo.class);

	@RequestMapping
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException, InterruptedException,
			IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);
		UserBO userDAO = Global.getUserBO();
		User user = Util.getLoggedInUser();

		// Get input.
		Map<String, String[]> paramMap = request.getParameterMap();
		String password = paramMap.get("password")[0];
		String password2 = paramMap.get("password2")[0];
		String nickName = paramMap.get("nickName")[0].trim();
		String email = paramMap.get("email")[0].trim();

		// 验证密码
		String errorMsg = "";
		if (!password.equals(password2)) {
			errorMsg += "两次输入的密码不一致\n";
		}
		if (password.length() < 6) {
			errorMsg += "密码长度必须大于6个字符\n";
		}

		// 验证电子邮箱
		if (!Util.isValidEmail(email)) {
			errorMsg += "Email地址格式不正确\n";
		}
		User userWithTheEmail = null;
		if (userDAO.findByEmail(email).size() > 0) {
			userWithTheEmail = userDAO.findByEmail(email).get(0);
		}
		if (userWithTheEmail != null && (!userWithTheEmail.getUserName().equals(user.getUserName()))) {
			errorMsg += "Email地址已被其他人使用";
		}

		if (errorMsg.equals("")) {

			// 保存用户信息
			user.setNickName(EmojiFilter.filterEmoji(nickName));
			user.setPassword(password);
			user.setEmail(email);
			userDAO.updateEntity(user);

			List<User> changedUsers = new ArrayList<User>();
			changedUsers.add(user);
			UserSorter.getInstance().onUserChanged(changedUsers);

			log.info(String.format("用户[%s] 修改个人信息成功", user.getUserName()));
		} else {
			log.info(String.format("用户修改个人信息失败，详情：[%s] ", errorMsg));
		}

		// Send result back to client.
		Util.sendBooleanResponse(errorMsg.equals(""), errorMsg, null, response);

	}
}
