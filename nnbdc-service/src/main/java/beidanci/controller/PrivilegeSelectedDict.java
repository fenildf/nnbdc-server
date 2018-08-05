package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.bo.SelectedDictBO;
import beidanci.bo.UserBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.Dict;
import beidanci.po.SelectedDict;
import beidanci.po.SelectedDictId;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
public class PrivilegeSelectedDict {
	@RequestMapping("/privilegeSelectedDict.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void handle(HttpServletRequest request, HttpServletResponse response, String dictName)
			throws ClassNotFoundException, SQLException, NamingException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException, IllegalArgumentException, IllegalAccessException {
		UserBO userDAO = Global.getUserBO();
		User user = Util.getLoggedInUser();

		// 获取入参
		if (StringUtils.isEmpty(dictName)) {
			Util.sendBooleanResponse(false, "参数错误", null, response);
			return;
		}

		if (dictName.equalsIgnoreCase("rawWordBook")) {
			// 将生词本的“优先取词”标志取反
			user.setIsRawWordBookPrivileged(!user.getIsRawWordBookPrivileged());
			userDAO.updateEntity(user);
		} else {
			// 将单词书的“优先取词”标志取反
			Dict dict = Global.getDictBO().findByName(dictName);
			SelectedDictBO dictDAO = Global.getSelectedDictBO();
			SelectedDictId dictID = new SelectedDictId(user.getId(), dict.getId());
			SelectedDict selectedDict = dictDAO.findById(dictID);
			selectedDict.setIsPrivileged(!selectedDict.getIsPrivileged());
			dictDAO.updateEntity(selectedDict);
		}

		// Send result back to client.
		Util.sendBooleanResponse(true, null, null, response);

		return;
	}
}
