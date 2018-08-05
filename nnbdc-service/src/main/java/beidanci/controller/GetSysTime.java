package beidanci.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.util.Util;

@Controller
@RequestMapping("/getSysTime.do")
public class GetSysTime {
	@RequestMapping(method = RequestMethod.GET)
	public String handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Util.setPageNoCache(response);

		// Send result back to client.
		Util.sendBooleanResponse(true, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), null, response);

		return null;
	}
}
