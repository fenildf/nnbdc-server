package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.Article;

@Service("ArticleBO")
@Scope("prototype")
public class ArticleBO extends BaseBo<Article> {
	public ArticleBO() {
		setDao(new BaseDao<Article>() {
		});
	}
}
