package beidanci.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.SessionData;
import beidanci.util.SysParamUtil;
import beidanci.util.Util;

@Controller
public class ThrowDiceCotroller {

	@RequestMapping("/showThrowDicePage.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public String showThrowDicePage(HttpServletRequest request, HttpServletResponse response) {
		Util.setPageNoCache(response);

		Util.setCommonAttributesForShowingJSP(request);

		// 掷骰子，并把掷出牛粪数保存在session中
		throwDice(request);

		request.setAttribute("holidayCowDungRatio", SysParamUtil.getHolidayCowDungRatio());
		request.setAttribute("holidayCowDungDesc", SysParamUtil.getHolidayCowDungDesc());

		return "throwdice";
	}

	/**
	 * 掷骰子，并保存到数据库
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/throwDiceAndSave.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void throwDiceAndSave(HttpServletRequest request, HttpServletResponse response)
			throws IOException, IllegalAccessException {

		// 掷骰子，并把掷出牛粪数保存在数据库中
		int cowdung = throwDice(request);
		Global.getUserBO().saveCowDungOfThrowingDice(cowdung, "throw dice after learning", Util.getLoggedInUser());

		Util.sendBooleanResponse(true, null, cowdung, response);
	}

	/**
	 * 掷骰子，并把掷出牛粪数保存在session中
	 */
	private int throwDice(HttpServletRequest request) {
		int cowDung = Util.genRandomNumber(1, 20);
		SessionData sessionData = Util.getSessionData(request);
		sessionData.setCowDung(cowDung);
		return cowDung;
	}
}
