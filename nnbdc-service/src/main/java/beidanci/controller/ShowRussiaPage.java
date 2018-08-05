package beidanci.controller;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import beidanci.util.SysParamUtil;
import beidanci.util.Util;

@Controller
@RequestMapping("/showRussiaPage.do")
public class ShowRussiaPage {
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		Util.setPageNoCache(response);

		Util.setCommonAttributesForShowingJSP(request);
		request.setAttribute("date", new Date());
		request.setAttribute("soundBaseUrl", Util.getSoundBaseUrl());
		request.setAttribute("hall", request.getParameter("hall"));
		request.setAttribute("socketServerAddr", SysParamUtil.getSocketServerAddr());
		request.setAttribute("socketServerPort", SysParamUtil.getSocketServerPort());

		// leaveRoom参数是因为用户在房间中点击了【离开】按钮，所以要避免用户再次进入该房间
		request.setAttribute("exceptRoom", request.getParameter("exceptRoom"));

		return new ModelAndView("russia");

	}
}
