package beidanci.bo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import beidanci.Global;
import beidanci.dao.BaseDao;
import beidanci.po.*;
import beidanci.util.BeanUtils;
import beidanci.util.Util;
import beidanci.vo.DictVo;
import beidanci.vo.Result;

@Service("DictBO")
@Scope("prototype")
public class DictBO extends BaseBo<Dict> {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(DictBO.class);

	public DictBO() {
		setDao(new BaseDao<Dict>() {
		});
	}

	public void selectDicts(Integer[] selectedDicts) throws IllegalAccessException {

		HashSet<Integer> selectedDictIds = new HashSet<>();
		for (Integer dictId : selectedDicts) {
			selectedDictIds.add(dictId);
		}

		// 删除用户取消选择的单词书
		User user = Util.getLoggedInUser();
		for (Iterator<SelectedDict> i = user.getSelectedDicts().iterator(); i.hasNext();) {
			SelectedDict selectedDict = i.next();
			if (!selectedDictIds.contains(selectedDict.getDict().getId())) {
				Global.getSelectedDictBO().deleteEntity(selectedDict);
				i.remove();
				log.info(String.format("用户[%s]取消选择了单词书[%s]", Util.getNickNameOfUser(user),
						selectedDict.getDict().getName()));
			}
		}

		// 添加用户新选择的单词书
		for (Integer dictId : selectedDicts) {
			SelectedDictId id = new SelectedDictId(user.getId(), dictId);
			SelectedDict selectedDict = Global.getSelectedDictBO().findById(id);
			if (selectedDict == null) {
				Dict dict = Global.getDictBO().findById(dictId);
				assert (dict.getIsReady());
				selectedDict = new SelectedDict(id, dict, user, false);
				Global.getSelectedDictBO().createEntity(selectedDict);
				user.getSelectedDicts().add(selectedDict);
				log.info(String.format("用户[%s]选择了单词书[%s]", Util.getNickNameOfUser(user), dictId));
			}

		}

		// 删除用户的那些尚未开始学习的 leaning dict.
		Global.getUserBO().deleteUnStartedDicts(user, selectedDictIds);

		// 将用户新选择的单词书列为 learning dict.
		for (Integer dictId : selectedDicts) {
			LearningDictId id = new LearningDictId(user.getId(), dictId);
			if (Global.getLearningDictBO().findById(id) == null) {
				Dict dict = Global.getDictBO().findById(dictId);
				LearningDict learningDict = new LearningDict(id, dict, user);
				Global.getLearningDictBO().createEntity(learningDict);
				user.getLearningDicts().add(learningDict);
			}
		}

		Global.getUserBO().updateEntity(user);
	}

	// 获取所有指定用户的单词书
	@SuppressWarnings("unchecked")
	public List<Dict> getOwnDicts(User owner) {
		Session session = getSession();
		String hql = "from Dict where owner=:owner";
		Query query = session.createQuery(hql);
		query.setCacheable(true);
		query.setParameter("owner", owner);
		List<Dict> result = query.list();
		return result;
	}

	// 获取所有系统单词书
	public List<Dict> getAllSysDicts() {
		User user = Global.getUserBO().getByUserName("sys");
		return getOwnDicts(user);
	}

	/**
	 * 完成对指定单词书的编辑
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public Result<Object> finishEditingDict(int dictId)
			throws IOException, IllegalArgumentException, IllegalAccessException {
		Dict dict = Global.getDictBO().findById(dictId);
		if (dict.getIsReady()) {
			return new Result<Object>(false, "单词书已处于就绪状态，不可重复操作", null);
		}
		if (dict.getWordCount() < 10) {
			return new Result<Object>(false, "单词书中的单词数量不能小于10个", null);
		}

		// 对书中的单词进行乱序
		Collections.sort(dict.getDictWords(), new Comparator<DictWord>() {
			@Override
			public int compare(DictWord o1, DictWord o2) {
				return DigestUtils.md5DigestAsHex(o1.getWord().getSpell().getBytes())
						.compareTo(DigestUtils.md5DigestAsHex(o2.getWord().getSpell().getBytes()));
			}
		});
		int md5IndexNo = 1; // 单词按照md5排序的顺序号
		for (DictWord dictWord : dict.getDictWords()) {
			dictWord.setMd5IndexNo(md5IndexNo);
			Global.getDictWordBO().updateEntity(dictWord);
			md5IndexNo++;
		}

		dict.setWordCount(dict.getDictWords().size());
		dict.setIsReady(true);
		updateEntity(dict);

		return Result.SUCCESS;
	}

	/**
	 * 创建新单词书
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public Result<DictVo> createNewDict(String dictName, User user)
			throws IOException, IllegalArgumentException, IllegalAccessException {
		// 检查同名单词书是否已经存在
		List<Dict> allMyDicts = getOwnDicts(user);
		for (Dict dict : allMyDicts) {
			if (dict.getShortName().equalsIgnoreCase(dictName)) {
				return new Result<>(false, "同名单词书已经存在", null);
			}
		}

		Dict dict = new Dict();
		dict.setWordCount(0);
		dict.setIsReady(false); // 新单词书处于待编辑状态
		dict.setIsShared(false);
		dict.setName(dictName + "." + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		dict.setOwner(user);
		createEntity(dict);

		DictVo vo = BeanUtils.makeVO(dict, DictVo.class,
				new String[] { "invitedBy", "studyGroups", "userGames", "dictWords" });

		return new Result<>(true, null, vo);
	}

	public Dict findByName(String dictName) {
		Dict exam = new Dict();
		exam.setName(dictName);
		getDAO().setPreciseEntity(exam);
		return queryUnique();
	}
}
