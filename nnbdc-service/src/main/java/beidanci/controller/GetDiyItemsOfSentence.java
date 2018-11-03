package beidanci.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.Global;
import beidanci.bo.SentenceBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.Sentence;
import beidanci.po.SentenceDiyItem;
import beidanci.po.SentenceDiyItemRemark;
import beidanci.util.Util;
import beidanci.vo.SentenceDiyItemRemarkVo;
import beidanci.vo.SentenceDiyItemVo;
import beidanci.vo.UserVo;
import net.sf.json.JSONArray;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

/**
 * 获取某个例句的所有DIY条目。
 *
 * @author Administrator
 */
@Controller
@RequestMapping("/getDiyItemsOfSentence.do")
public class GetDiyItemsOfSentence {
    @RequestMapping(method = RequestMethod.GET)
    public String handle(HttpServletRequest request, HttpServletResponse response) throws SQLException, NamingException,
            ClassNotFoundException, IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
        Util.setPageNoCache(response);
        response.setContentType("application/json");

        // 获取输入
        Map<String, String[]> params = request.getParameterMap();
        int sentenceId = Integer.parseInt(params.get("sentenceId")[0]);

        // 获取指定例句的所有DIY条目
        SentenceBO sentenceDAO = Global.getSentenceBO();
        Sentence sentence = sentenceDAO.findById(sentenceId);

        // 获取一页例句
        List<SentenceDiyItemVo> diyItems = new ArrayList<SentenceDiyItemVo>();
        for (SentenceDiyItem diyItem : sentence.getSentenceDiyItems()) {
            SentenceDiyItemVo diyItemVO = new SentenceDiyItemVo();
            diyItemVO.setContent(diyItem.getContent());
            UserVo author = new UserVo();
            author.setId(diyItem.getAuthor().getId());
            author.setUserName(diyItem.getAuthor().getUserName());
            author.setDisplayNickName(diyItem.getAuthor().getDisplayNickName());
            diyItemVO.setAuthor(author);
            diyItemVO.setFootCount(diyItem.getFootCount());
            diyItemVO.setHandCount(diyItem.getHandCount());
            diyItemVO.setId(diyItem.getId());
            diyItemVO.setItemType(diyItem.getItemType());

            // 获取该例句的评论
            List<SentenceDiyItemRemarkVo> diyItemRemarks = new ArrayList<SentenceDiyItemRemarkVo>();
            diyItemVO.setSentenceDiyItemRemarks(diyItemRemarks);
            for (SentenceDiyItemRemark diyItemRemark : diyItem.getSentenceDiyItemRemarks()) {
                SentenceDiyItemRemarkVo diyItemRemarkVO = new SentenceDiyItemRemarkVo();
                diyItemRemarkVO.setContent(diyItemRemark.getContent());
                diyItemRemarkVO.setCreator(diyItemRemark.getUser().getUserName());
                diyItemRemarkVO.setId(diyItemRemark.getId());
                diyItemRemarks.add(diyItemRemarkVO);
            }

            diyItems.add(diyItemVO);
        }

        PrintWriter out = response.getWriter();
        try {
            JsonConfig jsonConfig = new JsonConfig();
            out.println(((JSONArray) JSONSerializer.toJSON(diyItems, jsonConfig)).toString());

        } finally {
            out.close();
        }
        return null;
    }
}
