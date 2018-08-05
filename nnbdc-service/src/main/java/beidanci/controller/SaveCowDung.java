package beidanci.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.SessionData;
import beidanci.bo.UserBO;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
public class SaveCowDung {

	@RequestMapping("/saveCowDung.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public String handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		Map<String, String[]> params = request.getParameterMap();
		String reason = params.get("reason")[0];

		UserBO userBO = Global.getUserBO();
		SessionData sessionData = Util.getSessionData(request);
		User user = Util.getLoggedInUser();

		// 获取奖励用户的牛粪数（此数由服务端产生，并保存在会话中）
		int delta = sessionData.getCowDung();

		userBO.saveCowDungOfThrowingDice(delta, reason, user);

		// Send result back to client.
		Util.sendBooleanResponse(true, null, null, response);
		return null;
	}

}
