package beidanci;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import beidanci.po.User;
import beidanci.util.Util;
import beidanci.vo.UserVo;

public class
SessionListener implements HttpSessionListener, HttpSessionAttributeListener {
	private static Logger log = LoggerFactory.getLogger(SessionListener.class);
	private static int OnlineUsercount = 0;

	public void sessionCreated(HttpSessionEvent httpSessionEvent) {
	}

	public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
	}

	public void attributeAdded(HttpSessionBindingEvent event) {
		if (event.getName().equals("sessionData")) {
			OnlineUsercount++;
			UserVo user = Util.getLoggedInUserVO();
			log.info(String.format("用户【%s】登录成功，在线用户数【%d】", user.getUserName(), OnlineUsercount));
		}

	}

	public void attributeRemoved(HttpSessionBindingEvent event) {
		if (event.getName().equals("sessionData")) {
			User user = Util.getLoggedInUser();
			OnlineUsercount--;
			log.info(String.format("用户【%s】登出，在线用户数【%d】", Util.getNickNameOfUser(user), OnlineUsercount));
		}
	}

	public void attributeReplaced(HttpSessionBindingEvent event) {

	}

}
