package beidanci;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import beidanci.socket.SocketServer;
import beidanci.store.WordStore;
import beidanci.util.IPSeeker;

public class BdcServletContextListener implements ServletContextListener {
	private static Logger log = LoggerFactory.getLogger(BdcServletContextListener.class);

	public BdcServletContextListener() {
		log.info("BdcServletContextListener created.");
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		SocketServer.getInstance().stop();
	}

	public void contextInitialized(ServletContextEvent event) {
		IPSeeker.setDataFile(new File(event.getServletContext().getRealPath("/WEB-INF/data/qqwry.dat")));

		event.getServletContext().addListener(SessionListener.class);
		Global.setWebAppCtx(WebApplicationContextUtils.getWebApplicationContext(event.getServletContext()));

		SocketServer.getInstance().start();

		// 触发加载单词
		try {
			WordStore.getInstance();
		} catch (Exception e) {
			log.error("", e);
		}
	}
}
