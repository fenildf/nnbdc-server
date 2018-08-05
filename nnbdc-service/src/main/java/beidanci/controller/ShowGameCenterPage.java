package beidanci.controller;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.bo.HallGroupBO;
import beidanci.bo.UserGameBO;
import beidanci.po.HallGroup;
import beidanci.po.UserGame;
import beidanci.socket.game.russia.Hall;
import beidanci.socket.game.russia.RussiaService;
import beidanci.util.SysParamUtil;
import beidanci.util.Util;

@Controller
@RequestMapping("/showGameCenterPage.do")
public class ShowGameCenterPage {
	Logger log = LoggerFactory.getLogger(ShowGameCenterPage.class);

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
			throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		Util.setPageNoCache(response);

		// 获取所有游戏大厅分组
		HallGroupBO hallGroupDAO = Global.getHallGroupBO();
		List<HallGroup> hallGroups = hallGroupDAO.queryAll();
		Collections.sort(hallGroups, new Comparator<HallGroup>() {
			@Override
			public int compare(HallGroup o1, HallGroup o2) {
				return o1.getDisplayOrder() - o2.getDisplayOrder();
			}
		});

		// 获取所有游戏大厅
		Map<String, Hall> halls = RussiaService.getInstance().getGameHalls();
		request.setAttribute("halls", halls);

		// 获取游戏积分榜
		UserGameBO userGameDAO = Global.getUserGameBO();
		List<UserGame> userGames = userGameDAO.getUserGamesWithTopScore(15);
		request.setAttribute("topUserGames", userGames);

		Util.setCommonAttributesForShowingJSP(request);
		request.setAttribute("hallGroups", hallGroups);
		request.setAttribute("awardCowDungForShare", SysParamUtil.getAwardCowDungForShare());
		return new ModelAndView("gamecenter");
	}
}
