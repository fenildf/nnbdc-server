package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.bo.UserBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
public class SwitchAutoPlaySentenceFlag {
	@RequestMapping("/switchAutoPlaySentenceFlag.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public String handle(HttpServletRequest request, HttpServletResponse response)
			throws ClassNotFoundException, SQLException, NamingException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();
		UserBO userDAO = Global.getUserBO();

		// 将“自动播放句子发音”标志取反
		user.setAutoPlaySentence(!user.getAutoPlaySentence());
		userDAO.updateEntity(user);

		// Send result back to client.
		Util.sendBooleanResponse(true, null, null, response);

		return null;
	}
}
