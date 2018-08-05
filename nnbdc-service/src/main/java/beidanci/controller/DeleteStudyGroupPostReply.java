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
import beidanci.bo.StudyGroupPostBO;
import beidanci.bo.StudyGroupPostReplyBO;
import beidanci.po.StudyGroupPost;
import beidanci.po.StudyGroupPostReply;
import beidanci.util.Util;

@Controller
@RequestMapping("/deleteStudyGroupPostReply.do")
public class DeleteStudyGroupPostReply {
	private static Logger log = LoggerFactory.getLogger(DeleteStudyGroupPostReply.class);

	@RequestMapping(method = RequestMethod.GET)
	public String handle(HttpServletRequest request, HttpServletResponse response) throws SQLException, NamingException,
			ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		int replyID = Integer.parseInt(params.get("replyID")[0]);
		StudyGroupPostReplyBO replyDAO = Global.getStudyGroupPostReplyBO();
		StudyGroupPostReply reply = replyDAO.findById(replyID);
		StudyGroupPost post = reply.getStudyGroupPost();

		// 删除回复
		replyDAO.deleteEntity(reply);
		log.info(String.format("删除回复[%s]成功", reply.getContent()));

		// 更新帖子相关信息
		post.setReplyCount(post.getReplyCount() - 1);
		StudyGroupPostBO postDAO = Global.getStudyGroupPostBO();
		postDAO.updateEntity(post);

		// Send result back to client.
		Util.sendBooleanResponse(true, null, null, response);

		return null;
	}

}
