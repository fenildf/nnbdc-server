package beidanci.controller;

import java.io.IOException;
import java.util.Map;

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
@RequestMapping("/getAwardForInviting.do")
public class GetAwardForInviting {
	private static Logger log = LoggerFactory.getLogger(GetAwardForInviting.class);

	@RequestMapping(method = RequestMethod.GET)
	public String handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		Map<String, String[]> params = request.getParameterMap();
		String invitedUserName = params.get("invitedUser")[0];

		UserBO userDAO = Global.getUserBO();
		User user = Util.getLoggedInUser();
		User invitedUser = userDAO.findById(invitedUserName);

		// 验证是否符合领奖条件(防止恶意领奖)
		String errMsg = "";
		if (invitedUser == null) {
			errMsg = "该用户不存在";
			log.warn(String.format("用户[%s]尝试领取邀请奖励，但用户[%s]不存在。", Util.getNickNameOfUser(user), invitedUserName));
		}
		if (!invitedUser.getInvitedBy().getUserName().equalsIgnoreCase(user.getUserName())) {
			errMsg = "该用户并不是您邀请的";
			log.warn(String.format("用户[%s]尝试领取邀请奖励，但用户[%s]并不是由他邀请的。", Util.getNickNameOfUser(user), invitedUserName));
		} else if (invitedUser.getInviteAwardTaken()) {
			errMsg = "该用户的邀请奖励已经领取过了";
			log.warn(
					String.format("用户[%s]尝试领取邀请奖励，但目标用户[%s]的奖励已经领取过了。", Util.getNickNameOfUser(user), invitedUserName));
		}

		// 领取邀请奖励
		if (errMsg.equals("")) {
			// 获取用户当前牛粪数
			int currCowDung = user.getCowDung();

			// 保存用户的牛粪数
			int delta = 100;
			userDAO.adjustCowDung(user, delta, "被邀请用户打卡天数达到要求");

			// 设置奖励已领取标志
			invitedUser.setInviteAwardTaken(true);
			userDAO.updateEntity(invitedUser);

			// Send result back to client.
			String msg = String.format("您成功领取了[%d]个牛粪的奖励", delta);
			log.warn(String.format("用户[%s]成功领取了邀请奖励，被邀请用户[%s]", Util.getNickNameOfUser(user), invitedUserName));
			Util.sendBooleanResponse(true, msg, null, response);
		} else {
			// Send result back to client.
			Util.sendBooleanResponse(false, errMsg, null, response);
		}

		return null;
	}
}
