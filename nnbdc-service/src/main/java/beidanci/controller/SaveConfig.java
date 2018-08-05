package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import beidanci.Global;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.util.Util;

@Controller
public class SaveConfig {

	@RequestMapping("/saveConfig.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void handle(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "selectedDicts[]") Integer[] selectedDicts)
			throws ClassNotFoundException, SQLException, NamingException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		Map<String, String[]> params = request.getParameterMap();

		if (selectedDicts == null) {
			Util.sendBooleanResponse(false, "参数错误", null, response);
			return;
		}

		Global.getDictBO().selectDicts(selectedDicts);

		// Send result back to client.
		Util.sendBooleanResponse(true, null, null, response);
	}
}
