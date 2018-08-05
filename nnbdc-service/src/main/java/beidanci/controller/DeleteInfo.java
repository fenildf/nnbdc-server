package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.util.Util;

@Controller
@RequestMapping("/deleteInfo.do")
public class DeleteInfo {
	private static final Logger log = LoggerFactory.getLogger(SendMsg.class);

	@RequestMapping
	public void handle(int infoId, HttpServletResponse response) throws SQLException, NamingException,
			ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException {
		Global.getWordAdditionalInfoBO().deleteById(infoId);
		Util.sendBooleanResponse(true, null, null, response);
	}
}
