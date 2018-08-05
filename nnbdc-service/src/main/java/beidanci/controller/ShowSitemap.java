package beidanci.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.bo.ForumBO;
import beidanci.bo.StudyGroupBO;
import beidanci.po.Article;
import beidanci.po.Forum;
import beidanci.po.ForumPost;
import beidanci.po.StudyGroup;
import beidanci.util.Util;

@Controller
@RequestMapping("/showSitemap.do")
public class ShowSitemap {
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) {
		Util.setPageNoCache(response);

		Util.setCommonAttributesForShowingJSP(request);

		// 获取所有文章并按时间排序
		List<Article> articles = Global.getArticleBO().queryAll();
		Collections.sort(articles, new Comparator<Article>() {
			@Override
			public int compare(Article o1, Article o2) {
				return o2.getCreateTime().compareTo(o1.getCreateTime());
			}
		});
		request.setAttribute("articles", articles);

		// 获取总论坛（目前阶段只显示总论坛）
		ForumBO forumDAO = Global.getForumBO();
		Forum forum = (Forum) forumDAO.findByName("总论坛");

		// 对论坛帖子按更新时间排序
		List<ForumPost> posts = new LinkedList<ForumPost>(forum.getForumPosts());
		Collections.sort(posts, new Comparator<ForumPost>() {
			@Override
			public int compare(ForumPost o1, ForumPost o2) {
				return o2.getUpdateTime().compareTo(o1.getUpdateTime());
			}
		});
		request.setAttribute("sortedPosts", posts);

		// 获取系统中所有学习小组
		StudyGroupBO studyGroupDAO = Global.getStudyGroupBO();
		List<StudyGroup> studyGroups = studyGroupDAO.findAll();
		request.setAttribute("studyGroups", studyGroups);

		return new ModelAndView("sitemap");

	}
}
