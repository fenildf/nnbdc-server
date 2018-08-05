package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.mail.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.bo.ForumBO;
import beidanci.bo.ForumPostBO;
import beidanci.bo.ForumPostReplyBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.Forum;
import beidanci.po.ForumPost;
import beidanci.po.ForumPostReply;
import beidanci.po.User;
import beidanci.util.BeanUtils;
import beidanci.util.Util;
import beidanci.vo.ForumPostVo;
import beidanci.vo.ForumVo;
import beidanci.vo.PagedResults;

@Controller
public class ForumController {
	private static final Logger log = LoggerFactory.getLogger(ForumController.class);

	@RequestMapping("/showForumPage.do")
	public ModelAndView showForumPage(HttpServletRequest request, HttpServletResponse response)
			throws ClassNotFoundException, SQLException, NamingException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException {

		Util.setPageNoCache(response);

		// 获取总论坛（目前阶段只显示总论坛）
		ForumBO forumDAO = Global.getForumBO();
		Forum forum = (Forum) forumDAO.findByName("总论坛");
		request.setAttribute("forum", forum);

		User user = Util.getLoggedInUser();
		log.info(String.format("用户[%s]正在访问论坛总页面, UA[%s]", user == null ? "null" : Util.getNickNameOfUser(user),
				Util.getUserAgent(request)));

		// 对论坛帖子按更新时间排序
		List<ForumPost> posts = new LinkedList<ForumPost>(forum.getForumPosts());
		Collections.sort(posts, new Comparator<ForumPost>() {
			@Override
			public int compare(ForumPost o1, ForumPost o2) {
				return o2.getUpdateTime().compareTo(o1.getUpdateTime());
			}
		});
		request.setAttribute("sortedPosts", posts);

		Util.setCommonAttributesForShowingJSP(request);

		return new ModelAndView("forum");

	}

	@RequestMapping("/createForumPost.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void createForumPost(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException, EmailException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		// Get input.
		Map<String, String[]> paramMap = request.getParameterMap();
		String postTitle = paramMap.get("postTitle")[0];
		String postContent = paramMap.get("postContent")[0];
		int forumID = Integer.parseInt(paramMap.get("forumId")[0]);
		ForumBO forumDAO = Global.getForumBO();
		Forum forum = forumDAO.findById(forumID);

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
			ForumPost post = new ForumPost();
			post.setCreateTime(new Timestamp(new Date().getTime()));
			post.setLastReplyTime(null);
			post.setPostContent(postContent);
			post.setPostTitle(postTitle);
			post.setReplyCount(0);
			post.setBrowseCount(0);
			post.setForum(forum);
			post.setUser(user);
			post.setUpdateTime(new Timestamp(new Date().getTime()));
			ForumPostBO postDAO = Global.getForumPostBO();
			postDAO.createEntity(post);

			log.info(String.format("用户[%s]创建论坛帖子成功：标题[%s] 内容[%s]", Util.getNickNameOfUser(user), post.getPostTitle(),
					post.getPostContent()));
		} else {
			log.info(String.format("用户[%s]创建论坛帖子失败，详情：[%s] ", Util.getNickNameOfUser(user), errorMsg));
		}

		// Send result back to client.
		Util.sendBooleanResponse(errorMsg.equals(""), errorMsg, null, response);

		// 向myb发送通知邮件
		Util.sendSimpleEmail("mmyybb3000@hotmail.com", "myb", "牛牛有新帖",
				String.format("发帖人：%s\n 标题：%s\n 内容：%s\n", user.getDisplayNickName(), postTitle, postContent));
	}

	@RequestMapping("/replyForumPost.do")
	public void replyForumPost(HttpServletRequest request, HttpServletResponse response) throws ClassNotFoundException,
			SQLException, NamingException, IOException, ParseException, InvalidMeaningFormatException,
			EmptySpellException, IllegalArgumentException, IllegalAccessException, EmailException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		// Get input.
		Map<String, String[]> paramMap = request.getParameterMap();
		int postID = Integer.parseInt(paramMap.get("postId")[0]);
		String replyContent = paramMap.get("content")[0];

		// 获取要回复的帖子
		ForumPostBO postDAO = Global.getForumPostBO();
		ForumPost post = postDAO.findById(postID);

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
			ForumPostReply reply = new ForumPostReply();
			reply.setContent(replyContent);
			reply.setCreateTime(now);
			reply.setForumPost(post);
			reply.setUser(user);
			ForumPostReplyBO replyDAO = Global.getForumPostReplyBO();
			replyDAO.createEntity(reply);

			// 更新帖子相关信息
			post.setLastReplyTime(now);
			post.setUpdateTime(now);
			post.setReplyCount(post.getReplyCount() + 1);
			postDAO.updateEntity(post);

			log.info(String.format("用户[%s]回复论坛帖子成功：内容[%s]", Util.getNickNameOfUser(user), reply.getContent()));
		} else {
			log.info(String.format("用户[%s]回复论坛帖子失败，详情：[%s] ", Util.getNickNameOfUser(user), errorMsg));
		}

		// Send result back to client.
		Util.sendBooleanResponse(errorMsg.equals(""), errorMsg, null, response);

		// 向myb发送通知邮件
		Util.sendSimpleEmail("mmyybb3000@hotmail.com", "myb", "牛牛有回复帖", String.format("回复人：%s\n 原贴标题：%s\n 回复内容：%s\n",
				user.getDisplayNickName(), post.getPostTitle(), replyContent));
	}

	@RequestMapping("/showForumPostInfo.do")
	public ModelAndView showForumPostInfo(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, IOException, ParseException, InvalidMeaningFormatException, EmptySpellException,
			NamingException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		// 获取参数，并检验是否合法.
		Map<String, String[]> paramMap = request.getParameterMap();
		if (paramMap.get("postID") == null || !Util.isNumber(paramMap.get("postID")[0])) {
			log.warn("postID为null");
			return new ModelAndView("noResource");
		}
		if (!Util.isNumber(paramMap.get("postID")[0])) {
			log.warn(String.format("postID[%s]无效", paramMap.get("postID")[0]));
			return new ModelAndView("noResource");
		}
		int postID = Integer.parseInt(paramMap.get("postID")[0]);
		ForumPostBO postDAO = Global.getForumPostBO();
		ForumPost post = postDAO.findById(postID);

		// 检查帖子是否存在
		if (post == null) {
			log.warn(String.format("找不到相应的论坛帖子(postID[%d])", postID));
			return new ModelAndView("noResource");
		}
		request.setAttribute("post", post);

		User user = Util.getLoggedInUser();
		log.info(String.format("用户[%s]正在访问论坛帖子[%s], UA[%s]", user == null ? "null" : Util.getNickNameOfUser(user),
				post.getPostTitle(), Util.getUserAgent(request)));

		// 将所有回复按时间排序
		List<ForumPostReply> replys = new LinkedList<ForumPostReply>(post.getForumPostReplies());
		Collections.sort(replys, new Comparator<ForumPostReply>() {

			@Override
			public int compare(ForumPostReply o1, ForumPostReply o2) {
				return o1.getCreateTime().compareTo(o2.getCreateTime());
			}
		});
		request.setAttribute("replys", replys);

		// 增加浏览计数
		post.setBrowseCount(post.getBrowseCount() + 1);
		postDAO.updateEntity(post);

		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("forumPostInfo");
	}

	@RequestMapping("getForumPosts.do")
	public void getForumPosts(HttpServletResponse response, int page, int rows, String sort, String order)
			throws IOException {
		PagedResults<ForumPost> posts = Global.getForumPostBO().pagedQuery(page, rows, sort, order);
		PagedResults<ForumPostVo> vos = BeanUtils.makePagedVos(posts, ForumPostVo.class,
				new String[] { "studyGroups", "forumPostReplies", "forum", "userGames" });
		Util.sendJson(vos, response);
	}

	@RequestMapping("getForumByName.do")
	public void getForumPosts(HttpServletResponse response, String name) throws IOException {
		Forum forum = Global.getForumBO().findByName(name);
		ForumVo vo = BeanUtils.makeVO(forum, ForumVo.class,
				new String[] { "studyGroups", "forumPostReplies", "forum", "userGames" });
		Util.sendJson(vo, response);
	}

	@RequestMapping("getForumPostById.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getForumPostById(HttpServletResponse response, Integer postId)
			throws IOException, IllegalAccessException {
		ForumPost post = Global.getForumPostBO().findById(postId);
		Global.getForumPostBO().increaseBrowseCount(post);
		ForumPostVo vo = BeanUtils.makeVO(post, ForumPostVo.class,
				new String[] { "forumPosts", "forumPost", "studyGroups", "userGames" });
		Util.sendJson(vo, response);
	}
}
