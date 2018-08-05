package beidanci.controller;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.po.LearningDict;
import beidanci.po.User;
import beidanci.util.BeanUtils;
import beidanci.util.UserSorter;
import beidanci.util.Util;
import beidanci.vo.LearningDictVo;
import beidanci.vo.LevelVo;

@Controller
public class MainPageController {
	private static Logger log = LoggerFactory.getLogger(MainPageController.class);

	@RequestMapping("/switchEyeMode.do")
	public void switchEyeMode(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Boolean eyeMode = (Boolean) request.getSession().getAttribute("eyeMode");
		eyeMode = eyeMode == null ? false : eyeMode;
		eyeMode = !eyeMode;
		request.getSession().setAttribute("eyeMode", eyeMode);
		Util.sendBooleanResponse(true, null, null, response);
	}

	@RequestMapping("/getEyeMode.do")
	public void getEyeMode(HttpServletRequest request, HttpServletResponse response) throws Exception {
		boolean eyeMode = Util.getEyeMode(request);
		Util.sendJson(eyeMode, response);
	}

	@RequestMapping("/testPage.do")
	public String testPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "test";
	}

	@RequestMapping("getStudyProgress.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getStudyProgress(HttpServletResponse response) throws IOException {
		// 基本进度信息
		User user = Util.getLoggedInUser();
		Map<String, Object> items = new HashMap<String, Object>();
		items.put("existDays", user.getExistDays());
		items.put("dakaDayCount", user.getDakaDayCount());
		items.put("dakaRatio", user.getDakaRatio());
		items.put("totalScore", user.getTotalScore());
		items.put("userOrder", UserSorter.getInstance().getOrderOfUser(user.getUserName())); // 排名
		int rawWordCount = Global.getRawWordBO().findByUser(user).size();
		items.put("rawWordCount", rawWordCount); // 生词本中的单词数
		items.put("cowDung", user.getCowDung());
		items.put("level", BeanUtils.makeVO(user.getLevel(), LevelVo.class, null));
		items.put("masteredWordsCount", user.getMasteredWordsCount());
		items.put("learningWordsCount", user.getLearningWords().size());
		items.put("wordsPerDay", user.getWordsPerDay());
		items.put("continuousDakaDayCount", user.getContinuousDakaDayCount());
		items.put("throwDiceChance", user.getThrowDiceChance());

		// 用户是否没有选择单词书或者所有单词书都已背完
		items.put("allDictsFinished", Util.needSelectDictBeforeStudy(user));

		// 判断用户是否已完成本日单词学习
		boolean todayLearningFinished = user.getLearningFinished()
				&& Util.isSameDay(user.getLastLearningDate(), new Date());
		items.put("todayLearningFinished", todayLearningFinished);

		// 正在学习的单词书
		List<LearningDict> dicts = Util.getSelectedLearningDicts(user);
		List<LearningDictVo> dictVOs = new ArrayList<LearningDictVo>();
		for (LearningDict dict : dicts) {
			LearningDictVo vo = BeanUtils.makeVO(dict, LearningDictVo.class,
					new String[] { "createTime", "lastUpdateTime", "UserVo.invitedBy", "owner", "dictWords" });
			dictVOs.add(vo);
		}
		items.put("selectedLearningDicts", dictVOs);

		// 生词本是否设置了优先取词
		items.put("isRawWordBookPrivileged", user.getIsRawWordBookPrivileged());

		Util.sendJson(items, response);
	}
}
