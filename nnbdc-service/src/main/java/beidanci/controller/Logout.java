package beidanci.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.Global;
import beidanci.util.Util;

@Controller
@RequestMapping("/logout.do")
public class Logout {
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Global.getUserBO().doLogout(request);
		Util.sendBooleanResponse(true, null, null, response);
	}
}
