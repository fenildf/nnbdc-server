package beidanci;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import beidanci.po.User;
import beidanci.util.Util;

@Component
public class GlobalExceptionHandler implements HandlerExceptionResolver {
	private static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception e) {
		// AccessDeniedException异常由spring security框架自行处理
		if (e instanceof AccessDeniedException) {
			try {
				User user = Util.getLoggedInUser();
				log.info(String.format("用户[%s]访问[%s]缺乏权限!", user == null ? "未登录" : user.getUserName(),
						request.getRequestURI()));

				response.setHeader("command", "login");
				PrintWriter out;
				out = response.getWriter();
				response.setContentType("text/plain;charset=UTF-8");
				out.println("login please!");
				return new ModelAndView(); // 这里new一个空的ModelAndView而不是返回null，是为了告诉底层异常已被处理了。

			} catch (Exception ex) {
				log.error("", e);
			}
			return null;
		}

		// 客户端在接收完应答前终止了，这种异常不需要处理，由框架层自行处理
		if (e instanceof ClientAbortException) {
			log.info(String.format("客户端在访问[%s]时终止", request.getRequestURI()));
			return null;
		}

		log.error(String.format("访问[%s]时出现异常", request.getRequestURI()), e);

		try {
			response.setStatus(500);
			Util.sendBooleanResponse(false, "系统异常:" + e.getMessage(), null, response);
		} catch (IOException e1) {
			log.error("", e1);
		}
		return new ModelAndView(); // 这里new一个空的ModelAndView而不是返回null，是为了告诉底层异常已被处理了。

	}

}
