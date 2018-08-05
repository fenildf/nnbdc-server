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

import beidanci.Global;
import beidanci.bo.StudyGroupPostBO;
import beidanci.bo.StudyGroupPostReplyBO;
import beidanci.po.StudyGroupPost;
import beidanci.po.StudyGroupPostReply;
import beidanci.util.Util;

@Controller
@RequestMapping("/deleteStudyGroupPost.do")
public class DeleteStudyGroupPost {
	private static Logger log = LoggerFactory.getLogger(DeleteStudyGroupPost.class);

	@RequestMapping
	public String handle(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException {
		Util.setPageNoCache(response);

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		int postID = Integer.parseInt(params.get("postId")[0]);
		StudyGroupPostBO postDAO = Global.getStudyGroupPostBO();
		StudyGroupPost post = postDAO.findById(postID);

		// 删除帖子的所有回复
		StudyGroupPostReplyBO replyDAO = Global.getStudyGroupPostReplyBO();
		for (StudyGroupPostReply reply : post.getStudyGroupPostReplies()) {
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
