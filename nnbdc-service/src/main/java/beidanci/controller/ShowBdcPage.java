package beidanci.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import beidanci.SessionData;
import beidanci.po.LearningWord;
import beidanci.util.Util;

@Controller
public class ShowBdcPage {
	private static final Logger log = LoggerFactory.getLogger(ShowBdcPage.class);

	/**
	 * 接续上次学习的断点
	 *
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/continueAtTheLastBreakPoint.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void continueAtTheLastBreakPoint(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		doContinueAtTheLastBreakPoint(request);
		Util.sendBooleanResponse(true, null, null, response);
	}

	private void doContinueAtTheLastBreakPoint(HttpServletRequest request) {
		SessionData sessionData = Util.getSessionData(request);
		sessionData.setCurrentLearningWordIndex(-1);
		sessionData.setCurrentLearningMode(-1);
	}

	@RequestMapping("/showBdcPage.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ModelAndView showBdcPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Util.setPageNoCache(response);

		// 接续上次学习的断点
		SessionData sessionData = Util.getSessionData(request);
		doContinueAtTheLastBreakPoint(request);

		List<LearningWord> todayWords = sessionData.getTodayWords();
		if (todayWords == null) {// 用户有可能收藏了背单词页面
			log.info("准备进入背单词页面，但今日单词还未准备好，可能是因为用户收藏了背单词页面。正在重定向到首页...");
			response.sendRedirect("showIndexPage.do");
			return null;
		}

		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("bdc");
	}

}
