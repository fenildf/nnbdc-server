package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.SessionData;
import beidanci.bo.StudyGroupBO;
import beidanci.bo.StudyGroupPostBO;
import beidanci.bo.StudyGroupPostReplyBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.StudyGroup;
import beidanci.po.StudyGroupPost;
import beidanci.po.StudyGroupPostReply;
import beidanci.po.User;
import beidanci.util.BeanUtils;
import beidanci.util.Util;
import beidanci.vo.StudyGroupVo;

@Controller
public class StudyGroupController {
	private static Logger log = LoggerFactory.getLogger(StudyGroupController.class);

	@RequestMapping("/showStudyGroupInfo.do")
	public ModelAndView showStudyGroupInfo(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, IOException, ParseException, InvalidMeaningFormatException, EmptySpellException,
			NamingException, ClassNotFoundException {
		Util.setPageNoCache(response);

		// 获取要显示的学习小组，如果小组ID为空，则显示”我的小组“.
		Map<String, String[]> paramMap = request.getParameterMap();
		int groupID;
		if (paramMap.get("groupID") != null) {
			String groupIDStr = paramMap.get("groupID")[0];
			groupIDStr = groupIDStr.split(";")[0]; // 忽略可能存在的”jsessionid"字符串（可能存在这样的一些外部历史链接）
			if (!Util.isNumber(groupIDStr)) {
				log.warn(String.format("groupID[%s]无效", groupIDStr));
				return new ModelAndView("noResource");
			}
			groupID = Integer.parseInt(groupIDStr);
		} else {
			SessionData sessionData = Util.getSessionData(request);
			if (sessionData == null) {// 用户会话超时了
				response.sendRedirect("showStudyGroupPage.do");
				return null;
			}
			User user = Util.getLoggedInUser();

			if (user.getStudyGroups().size() > 0) {
				groupID = ((StudyGroup) user.getStudyGroups().toArray()[0]).getId();
			} else {
				// 用户突然被管理员从小组开除了
				response.sendRedirect("showStudyGroupPage.do");
				return null;
			}
		}

		StudyGroupBO studyGroupDAO = Global.getStudyGroupBO();
		StudyGroup studyGroup = studyGroupDAO.findById(groupID);
		request.setAttribute("studyGroup", studyGroup);

		// 判断小组ID是否有效
		if (studyGroup == null) {
			log.warn(String.format("找不到groupID[%d]对应的小组, UA[%s]", groupID, Util.getUserAgent(request)));
			response.sendRedirect("showStudyGroupPage.do");
			return null;
		}

		User user = Util.getLoggedInUser();
		log.info(String.format("用户[%s]正在访问学习小组[%s], UA[%s]", user == null ? "null" : Util.getNickNameOfUser(user),
				studyGroup.getGroupName(), Util.getUserAgent(request)));

		// 对帖子按最近更新时间的排序
		List<StudyGroupPost> posts = new LinkedList<StudyGroupPost>(studyGroup.getStudyGroupPosts());
		Collections.sort(posts, new Comparator<StudyGroupPost>() {
			@Override
			public int compare(StudyGroupPost o1, StudyGroupPost o2) {
				return o2.getUpdateTime().compareTo(o1.getUpdateTime());
			}
		});
		request.setAttribute("posts", posts);

		// 对组员按贡献度排序
		List<User> members = new LinkedList<User>(studyGroup.getUsers());
		Collections.sort(members, new Comparator<User>() {
			@Override
			public int compare(User o1, User o2) {
				return (int) (o2.getTotalScore() - o1.getTotalScore());
			}
		});
		request.setAttribute("members", members);

		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("studygroupinfo");
	}

	@RequestMapping("/createStudyGroupPost.do")
	public String createStudyGroupPost(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();
		// Get input.
		Map<String, String[]> paramMap = request.getParameterMap();
		String postTitle = paramMap.get("postTitle")[0];
		String postContent = paramMap.get("postContent")[0];
		int groupID = Integer.parseInt(paramMap.get("groupId")[0]);
		StudyGroupBO studyGroupDAO = Global.getStudyGroupBO();
		StudyGroup studyGroup = studyGroupDAO.findById(groupID);

		// 验证帖子标题.
		String errorMsg = "";
		if (postTitle.length() == 0) {
			errorMsg += "帖子标题不能为空\n";
		}

		// 验证帖子内容.
		if (postContent.length() == 0) {
			errorMsg += "帖子内容不能为空\n";
		}
		if (postContent.length() > 1024 * 1000) {
			errorMsg += "帖子内容不能大于1M\n";
		}

		// 验证用户是否是游客
		if (user.getUserName().startsWith("guest")) {
			errorMsg += "您是游客，不能发帖";
		}

		if (errorMsg.equals("")) {

			// 保存帖子信息到数据库.
			StudyGroupPost post = new StudyGroupPost();
			post.setCreateTime(new Timestamp(new Date().getTime()));
			post.setLastReplyTime(null);
			post.setPostContent(postContent);
			post.setPostTitle(postTitle);
			post.setReplyCount(0);
			post.setBrowseCount(0);
			post.setStudyGroup(studyGroup);
			post.setUser(user);
			post.setUpdateTime(new Timestamp(new Date().getTime()));
			StudyGroupPostBO postDAO = Global.getStudyGroupPostBO();
			postDAO.createEntity(post);

			log.info(String.format("用户[%s]创建小组帖子成功：小组[%s] 标题[%s] 内容[%s]", Util.getNickNameOfUser(user),
					studyGroup.getGroupName(), post.getPostTitle(), post.getPostContent()));
		} else {
			log.info(String.format("用户[%s]创建小组帖子失败，详情：[%s] ", Util.getNickNameOfUser(user), errorMsg));
		}

		// Send result back to client.
		Util.sendBooleanResponse(errorMsg.equals(""), errorMsg, null, response);

		return null;
	}

	@RequestMapping("/replyStudyGroupPost.do")
	public String replyStudyGroupPost(HttpServletRequest request, HttpServletResponse response)
			throws ClassNotFoundException, SQLException, NamingException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		// Get input.
		Map<String, String[]> paramMap = request.getParameterMap();
		int postID = Integer.parseInt(paramMap.get("postId")[0]);
		String replyContent = paramMap.get("content")[0];

		// 获取要回复的帖子
		StudyGroupPostBO postDAO = Global.getStudyGroupPostBO();
		StudyGroupPost post = postDAO.findById(postID);

		// 验证帖子标题.
		String errorMsg = "";

		// 验证回复内容.
		if (replyContent.length() == 0) {
			errorMsg += "帖子内容不能为空\n";
		}
		if (replyContent.length() > 1024 * 1000) {
			errorMsg += "帖子内容不能大于1M\n";
		}

		// 验证用户是否是游客
		if (user.getUserName().startsWith("guest")) {
			errorMsg += "您是游客，不能回复帖子";
		}

		if (errorMsg.equals("")) {

			// 保存回复信息到数据库.
			Timestamp now = new Timestamp(new Date().getTime());
			StudyGroupPostReply reply = new StudyGroupPostReply();
			reply.setContent(replyContent);
			reply.setCreateTime(now);
			reply.setStudyGroupPost(post);
			reply.setUser(user);
			StudyGroupPostReplyBO replyDAO = Global.getStudyGroupPostReplyBO();
			replyDAO.createEntity(reply);

			// 更新帖子相关信息
			post.setLastReplyTime(now);
			post.setUpdateTime(now);
			post.setReplyCount(post.getReplyCount() + 1);
			postDAO.updateEntity(post);

			log.info(String.format("用户[%s]回复小组[%s]帖子成功：内容[%s]", Util.getNickNameOfUser(user),
					post.getStudyGroup().getGroupName(), reply.getContent()));
		} else {
			log.info(String.format("用户[%s]回复小组[%s]帖子失败，详情：[%s] ", Util.getNickNameOfUser(user),
					post.getStudyGroup().getGroupName(), errorMsg));
		}

		// Send result back to client.
		Util.sendBooleanResponse(errorMsg.equals(""), errorMsg, null, response);

		return null;
	}

	@RequestMapping("getMyStudyGroups.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getMyStudyGroups(HttpServletResponse response) throws IOException {
		User user = Util.getLoggedInUser();
		List<StudyGroupVo> vos = BeanUtils.makeVos(user.getStudyGroups(), StudyGroupVo.class,
				new String[] { "studyGroupPosts", "UserVo.studyGroups" });
		Util.sendJson(vos, response);
	}

	@RequestMapping("getAllStudyGroups.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getAllStudyGroups(HttpServletResponse response) throws IOException {
		List<StudyGroup> studyGroups = Global.getStudyGroupBO().findAll();
		List<StudyGroupVo> vos = BeanUtils.makeVos(studyGroups, StudyGroupVo.class,
				new String[] { "studyGroupPosts", "UserVo.studyGroups", "userGames" });
		Util.sendJson(vos, response);
	}

	@RequestMapping("getStudyGroupById.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getStudyGroupById(HttpServletResponse response, Integer groupId) throws IOException {
		StudyGroup studyGroup = Global.getStudyGroupBO().findById(groupId);
		StudyGroupVo vo = BeanUtils.makeVO(studyGroup, StudyGroupVo.class,
				new String[] { "UserVo.studyGroups", "studyGroup", "studyGroupPostReplies", "userGames" });
		Util.sendJson(vo, response);
	}
}
