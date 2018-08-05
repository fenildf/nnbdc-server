package beidanci.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.bo.StudyGroupBO;
import beidanci.po.StudyGroup;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
public class ShowStudyGroupPage {
	private static final Logger log = LoggerFactory.getLogger(ShowStudyGroupPage.class);

	@RequestMapping("/showStudyGroupPage.do")
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException, IllegalAccessException, ParseException {
		Util.setPageNoCache(response);

		// 获取系统中所有学习小组
		long startTime = System.currentTimeMillis();
		StudyGroupBO studyGroupDAO = Global.getStudyGroupBO();
		List<StudyGroup> studyGroups = studyGroupDAO.findAll();
		long endTime = System.currentTimeMillis();
		log.info("查询所有学习小组耗时(ms)：" + (endTime - startTime));

		// 删除那些成立时间大于10天，且打卡率低于80的学习小组
		boolean hasGroupToDelete = false;
		for (StudyGroup studyGroup : studyGroups) {
			if (studyGroup.isBadGroup()) {
				hasGroupToDelete = true;
			}
		}
		if (hasGroupToDelete) {
			synchronized (this) {
				for (Iterator<StudyGroup> i = studyGroups.iterator(); i.hasNext();) {
					StudyGroup studyGroup = i.next();
					if (studyGroup.isBadGroup()) {
						Global.getStudyGroupBO().dismissStudyGroup(studyGroup.getId(), studyGroup.getCreator().getId());
						i.remove();
					}
				}
			}
		}

		User user = Util.getLoggedInUser();
		log.info(String.format("用户[%s]正在访问学习小组总页面, UA[%s]", user == null ? "null" : Util.getNickNameOfUser(user),
				Util.getUserAgent(request)));

		// 按小组名次排序
		startTime = System.currentTimeMillis();
		Collections.sort(studyGroups, new Comparator<StudyGroup>() {

			@Override
			public int compare(StudyGroup o1, StudyGroup o2) {
				try {
					int order1 = o1.getGroupSummary().getGroupOrder();
					int order2 = o2.getGroupSummary().getGroupOrder();
					int ret = order1 - order2;
					return ret;
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}

		});
		endTime = System.currentTimeMillis();
		log.info("对所有学习小组排序耗时(ms)：" + (endTime - startTime));

		request.setAttribute("studyGroups", studyGroups);

		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("studygroup");

	}
}
