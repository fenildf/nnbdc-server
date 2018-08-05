package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.Global;
import beidanci.bo.ForumPostBO;
import beidanci.bo.ForumPostReplyBO;
import beidanci.po.ForumPost;
import beidanci.po.ForumPostReply;
import beidanci.util.Util;

@Controller
@RequestMapping("/deleteForumPost.do")
public class DeleteForumPost {
	private static Logger log = LoggerFactory.getLogger(DeleteForumPost.class);

	@RequestMapping(method = RequestMethod.GET)
	public String handle(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException {
		Util.setPageNoCache(response);

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		int postID = Integer.parseInt(params.get("postID")[0]);
		ForumPostBO postDAO = Global.getForumPostBO();
		ForumPost post = postDAO.findById(postID);

		// 删除帖子的所有回复
		ForumPostReplyBO replyDAO = Global.getForumPostReplyBO();
		for (ForumPostReply reply : post.getForumPostReplies()) {
			replyDAO.deleteEntity(reply);
		}

		// 删除帖子
		postDAO.deleteEntity(post);
		log.info(String.format("删除帖子[%s]成功", post.getPostTitle()));

		// Send result back to client.
		Util.sendBooleanResponse(true, null, null, response);

		return null;
	}

}
