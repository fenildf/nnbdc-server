package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
import beidanci.bo.MsgBO;
import beidanci.bo.UserBO;
import beidanci.po.Msg;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/sendMsg.do")
public class SendMsg {
	private static final Logger log = LoggerFactory.getLogger(SendMsg.class);

	@RequestMapping(method = RequestMethod.GET)
	public String handle(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException {
		Util.setPageNoCache(response);

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		String content = params.get("content")[0];
		String fromUserName = params.get("fromUser")[0];
		String toUserName = params.get("toUser")[0];
		Boolean isTempMsg = Boolean.parseBoolean(params.get("isTemp")[0]);
		String msgType = params.get("msgType")[0];

		// 保存消息
		UserBO userDAO = Global.getUserBO();
		User fromUser = userDAO.findById(fromUserName);
		if (fromUser == null) {
			log.warn(String.format("发送消息的源用户(%s)在系统中不存在!!!, 目标用户:[%s] 消息内容[%s], ", fromUserName, toUserName, content));
			return null;
		}
		MsgBO msgDAO = Global.getMsgBO();
		Msg msg = new Msg();
		msg.setContent(content);
		User toUser = userDAO.findById(toUserName);
		msg.setFromUser(fromUser);
		msg.setToUser(toUser);
		msg.setMsgType(msgType);
		msg.setCreateTime(new Timestamp(new Date().getTime()));
		if (isTempMsg) {
			List<Msg> userMsgs = MsgController.msgs.get(toUserName);
			if (userMsgs == null) {
				userMsgs = new LinkedList<Msg>();
				MsgController.msgs.put(toUserName, userMsgs);
			}
			userMsgs.add(msg);
			log.info(String.format("临时消息队列现有[%d]条消息", MsgController.msgs.size()));
		} else {
			msgDAO.createEntity(msg);
		}

		log.info(String.format("[%s]向[%s]发消息[%s]", Util.getNickNameOfUser(fromUser), Util.getNickNameOfUser(toUser),
				content));

		// Send result back to client.
		Util.sendBooleanResponse(true, null, null, response);

		return null;
	}
}
