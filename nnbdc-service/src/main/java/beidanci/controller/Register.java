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
import beidanci.bo.UserBO;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/register.do")
public class Register {
	private static Logger log = LoggerFactory.getLogger(Register.class);

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	public void handle(HttpServletRequest request, HttpServletResponse response, Integer invitorId)
			throws SQLException, NamingException, ClassNotFoundException, IOException, InterruptedException {
		Util.setPageNoCache(response);

		// Get input.
		Map<String, String[]> paramMap = request.getParameterMap();
		if (paramMap.get("email") == null || paramMap.get("password") == null) {
			log.warn("告警：注册必填信息缺失!");
			Util.sendBooleanResponse(false, null, null, response);
			return;
		}

		String password = paramMap.get("password")[0];
		String password2 = paramMap.get("password2")[0];
		String nickName = paramMap.get("nickName")[0].trim();
		String email = paramMap.get("email")[0].trim();
		User invitor = invitorId == null ? null : Global.getUserBO().findById(invitorId);

		if (invitor != null) {
			log.info(String.format("[%s]邀请的朋友提交了注册请求", invitor.getDisplayNickName()));
		} else {
			invitorId = null;
		}

		// 验证电子邮箱
		String errorMsg = "";
		if (!Util.isValidEmail(email)) {
			errorMsg += "Email地址格式不正确\n";
		}
		UserBO userDAO = Global.getUserBO();
		boolean emailExists = userDAO.findByEmail(email).size() > 0;
		if (emailExists) {
			errorMsg += "Email已被其他用户使用\n";
		}

		// 验证密码
		if (!password.equals(password2)) {
			errorMsg += "两次输入的密码不一致\n";
		}
		if (password.length() < 6) {
			errorMsg += "密码长度必须大于6个字符\n";
		}

		if (errorMsg.equals("")) {

			// 如果没有填写昵称，则用用户名作为昵称
			if (nickName.trim().equals("")) {
				nickName = email.split("@")[0];
			}

			// Create user and save to database.
			User user = Util.genUser(email, password, nickName, email, invitor);
			userDAO.createEntity(user);

			log.info(String.format("新用户[%s] 注册成功, 邀请人是[%s] ", user.getUserName(),
					user.getInvitedBy() == null ? null : user.getInvitedBy().getUserName()));
		} else {
			log.info(String.format("用户注册信息填写错误，详情：[%s] ", errorMsg));
		}

		// Send result back to client.
		Util.sendBooleanResponse(errorMsg.equals(""), errorMsg, null, response);

	}
}
