package beidanci.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import beidanci.util.SysParamUtil;
import beidanci.util.Util;

@Controller
@RequestMapping("/showCommunityPage.do")
public class ShowChatPage {
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) {

		Util.setPageNoCache(response);

		// 是否是英文聊天室
		boolean isPureEn = Boolean.parseBoolean(request.getParameter("pureEn"));

		Util.setCommonAttributesForShowingJSP(request);
		request.setAttribute("isPureEn", isPureEn);
		request.setAttribute("socketServerAddr", SysParamUtil.getSocketServerAddr());
		request.setAttribute("socketServerPort", SysParamUtil.getSocketServerPort());
		return new ModelAndView("chat");

	}
}
