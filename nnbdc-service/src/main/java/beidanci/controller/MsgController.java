package beidanci.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.po.Msg;
import beidanci.po.User;
import beidanci.util.Util;
import beidanci.vo.MsgVo;
import beidanci.vo.Msgs;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

@Controller
public class MsgController {
	public static Map<String, List<Msg>> msgs = new ConcurrentHashMap<String, List<Msg>>();

	@RequestMapping("/getMsgs.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getMsgs(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {
		Util.setPageNoCache(response);

		// 更新用户最近访问时间(据此判断是否是在线用户)
		User user = Util.getLoggedInUser();
		GetActiveUsers.updateUserAccessTime(user);

		Map<String, String[]> paramMap = request.getParameterMap();
		Date fromTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(paramMap.get("fromTime")[0]);

		// 获取发给当前用户的所有消息(只返回在指定时间之后的消息)
		List<MsgVo> msgVos = new LinkedList<MsgVo>();
		List<Msg> msgsToUser = msgs.get(user.getUserName());
		if (msgsToUser != null) {
			for (Msg msg : msgsToUser) {
				if (msg.getCreateTime().after(fromTime)) {
					MsgVo msgVo = new MsgVo();
					msgVo.setContent(msg.getContent());
					msgVo.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(msg.getCreateTime()));
					msgVo.setCreateTimeForDisplay(new SimpleDateFormat("HH:mm:ss").format(msg.getCreateTime()));
					msgVo.setFromUserName(msg.getFromUser().getUserName());
					msgVo.setFromUserNickName(Util.getNickNameOfUser(msg.getFromUser()));
					msgVo.setToUserName(msg.getToUser().getUserName());
					msgVo.setToUserNickName(Util.getNickNameOfUser(msg.getToUser()));
					msgVo.setMsgType(msg.getMsgType());
					msgVos.add(msgVo);
				}
			}
		}

		// Send result back to client.
		response.setContentType("text/plain");
		Msgs msgs = new Msgs();
		msgs.setCount(msgVos.size());
		msgs.setMsgVos(msgVos.toArray(new MsgVo[0]));
		PrintWriter out = response.getWriter();
		out.println(((JSONObject) JSONSerializer.toJSON(msgs)).toString());

		return;
	}

}
