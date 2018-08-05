package beidanci.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.po.StudyGroupPost;
import beidanci.util.BeanUtils;
import beidanci.util.Util;
import beidanci.vo.StudyGroupPostVo;

@Controller
public class StudyGroupPostController {
	@RequestMapping("getStudyGroupPostById.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getStudyGroupById(HttpServletResponse response, Integer postId)
			throws IOException, IllegalAccessException {
		StudyGroupPost studyGroupPost = Global.getStudyGroupPostBO().findById(postId);
		Global.getStudyGroupPostBO().increaseBrowseCount(studyGroupPost);
		StudyGroupPostVo vo = BeanUtils.makeVO(studyGroupPost, StudyGroupPostVo.class,
				new String[] { "studyGroups", "studyGroupPost", "studyGroupPosts", "groupSummary", "userGames" });
		Util.sendJson(vo, response);
	}
}
