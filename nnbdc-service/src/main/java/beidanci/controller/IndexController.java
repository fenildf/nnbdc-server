package beidanci.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.bo.UserBO;
import beidanci.po.UpdateLog;
import beidanci.po.User;
import beidanci.util.IPSeeker;
import beidanci.util.Util;

@Controller
public class IndexController {
	private static Logger log = LoggerFactory.getLogger(IndexController.class);

	@RequestMapping("/getLastUpdateLog.do")
	public void getLastUpdateLog(HttpServletResponse response) throws IOException {
		UpdateLog lastUpdateLog = Global.getUpdateLogBO().getLastestLog();
		Util.sendJson(lastUpdateLog, response);
	}
}
