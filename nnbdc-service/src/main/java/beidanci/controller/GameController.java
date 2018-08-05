package beidanci.controller;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.po.HallGroup;
import beidanci.po.UserGame;
import beidanci.socket.game.russia.Hall;
import beidanci.socket.game.russia.RussiaService;
import beidanci.util.BeanUtils;
import beidanci.util.Util;
import beidanci.vo.HallGroupVo;
import beidanci.vo.UserGameVo;

@Controller
public class GameController {
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@RequestMapping("getGameHallData.do")
	public void getAllHallGroups(HttpServletResponse response) throws IOException {
		Map<String, Object> result = new HashMap<>();

		// 获取所有游戏大厅分组
		List<HallGroup> groups = Global.getHallGroupBO().queryAll();
		Collections.sort(groups, new Comparator<HallGroup>() {
			@Override
			public int compare(HallGroup o1, HallGroup o2) {
				return o1.getDisplayOrder() - o2.getDisplayOrder();
			}
		});
		List<HallGroupVo> groupVos = BeanUtils.makeVos(groups, HallGroupVo.class,
				new String[] { "hallGroup", "dicts", "allDicts" });
		result.put("hallGroups", groupVos);

		// 获取所有游戏大厅
		Map<String, Hall> halls = RussiaService.getInstance().getGameHalls();
		result.put("halls", halls);

		// 获取游戏积分榜
		List<UserGame> userGames = Global.getUserGameBO().getUserGamesWithTopScore(15);
		List<UserGameVo> userGameVos = BeanUtils.makeVos(userGames, UserGameVo.class,
				new String[] { "studyGroups", "userGames" });
		result.put("topUserGames", userGameVos);

		Util.sendJson(result, response);
	}
}
