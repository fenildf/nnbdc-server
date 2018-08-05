package beidanci.controller;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.po.User;
import beidanci.util.SysParamUtil;
import beidanci.util.Util;
import beidanci.vo.ActiveUser;

@Controller
@RequestMapping("/getActiveUsers.do")
public class GetActiveUsers {
	/**
	 * 活动用户列表（浏览器打开时，JS脚本会每隔一段时间自动调用getMsgs.do获取消息，这时用户即成为活动用户）
	 */
	private static Map<User, Date> activeUsers = new ConcurrentHashMap<User, Date>();

	private static Timer timer;

	private static void initTimeTask() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// 清除一段时间没有活动的用户
				for (Iterator<Date> i = activeUsers.values().iterator(); i.hasNext();) {
					Date lastUpdateTime = i.next();
					if (new Date().getTime() - lastUpdateTime.getTime() > SysParamUtil.getFetchMsgInterval() * 2) {
						i.remove();
					}
				}
			}
		}, 0, SysParamUtil.getFetchMsgInterval() * 2);
	}

	/**
	 * 把用户最近访问时间更新为当前时间
	 * 
	 * @param user
	 */
	public static void updateUserAccessTime(User user) {
		if (timer == null) {
			initTimeTask();
		}
		activeUsers.put(user, new Date());
	}

	/**
	 * 随机获取指定数量的活动用户
	 * 
	 * @return
	 */
	public static List<User> getActiveUsers(int count) {
		if (activeUsers.size() <= count) {
			return new ArrayList<User>(activeUsers.keySet());
		} else {
			List<User> users = new ArrayList<User>(count);
			User[] allActiveUsers = activeUsers.keySet().toArray(new User[0]);
			int index = (int) (Math.random() * allActiveUsers.length);
			for (int i = 0; i < count; i++) {
				if (index >= allActiveUsers.length) {
					index = 0;
				}
				users.add(allActiveUsers[index]);
				index++;
			}
			return users;
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	public String handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Util.setPageNoCache(response);

		Map<String, String[]> paramMap = request.getParameterMap();
		int userCount = Integer.parseInt(paramMap.get("userCount")[0]);

		List<User> users = getActiveUsers(userCount);
		ActiveUser[] userRecords = new ActiveUser[users.size()];
		for (int i = 0; i < users.size(); i++) {
			User user = users.get(i);
			ActiveUser activeUser = new ActiveUser(user.getUserName(), Util.getNickNameOfUser(user));
			userRecords[i] = activeUser;
		}

		// Send result back to client.
		Util.sendBooleanResponse(true, null, userRecords, response);
		return null;
	}

}
