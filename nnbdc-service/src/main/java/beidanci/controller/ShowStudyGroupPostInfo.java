package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.bo.StudyGroupPostBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.StudyGroupPost;
import beidanci.po.StudyGroupPostReply;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/showStudyGroupPostInfo.do")
public class ShowStudyGroupPostInfo {
	private static final Logger log = LoggerFactory.getLogger(ShowStudyGroupPostInfo.class);

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, IOException, ParseException, InvalidMeaningFormatException, EmptySpellException,
			NamingException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		// Get input.
		Map<String, String[]> paramMap = request.getParameterMap();
		String postIDStr = paramMap.get("postID")[0];
		postIDStr = postIDStr.split(";")[0]; // 忽略可能存在的”jsessionid"字符串（可能存在这样的一些外部历史链接）
		if (!Util.isNumber(postIDStr)) {
			log.warn(String.format("postID[%s]无效", postIDStr));
			return new ModelAndView("noResource");
		}
		int postID = Integer.parseInt(postIDStr);
		StudyGroupPostBO postDAO = Global.getStudyGroupPostBO();
		StudyGroupPost post = postDAO.findById(postID);
		request.setAttribute("post", post);

		if (post == null) {
			log.warn(String.format("Can not find post of ID[%d]", postID));
			response.getWriter().println(String.format("帖子[%d]不存在", postID));
			return null;
		}

		User user = Util.getLoggedInUser();
		log.info(
				String.format("用户[%s]正在访问学习小组[%s]的帖子[%s], UA[%s]", user == null ? "null" : Util.getNickNameOfUser(user),
						post.getStudyGroup().getGroupName(), post.getPostTitle(), Util.getUserAgent(request)));

		// 将所有回复按时间排序
		List<StudyGroupPostReply> replys = new LinkedList<StudyGroupPostReply>(post.getStudyGroupPostReplies());
		Collections.sort(replys, new Comparator<StudyGroupPostReply>() {

			@Override
			public int compare(StudyGroupPostReply o1, StudyGroupPostReply o2) {
				return o1.getCreateTime().compareTo(o2.getCreateTime());
			}
		});
		request.setAttribute("replys", replys);

		// 增加浏览计数
		post.setBrowseCount(post.getBrowseCount() + 1);
		postDAO.updateEntity(post);

		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("studyGroupPostInfo");
	}
}
