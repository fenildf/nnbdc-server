package beidanci.bo;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.Global;
import beidanci.SessionData;
import beidanci.dao.BaseDao;
import beidanci.po.Event;
import beidanci.po.User;
import beidanci.po.Word;
import beidanci.po.WordImage;
import beidanci.util.BeanUtils;
import beidanci.util.SysParamUtil;
import beidanci.vo.EventType;
import beidanci.vo.Result;
import beidanci.vo.UserVo;
import beidanci.vo.WordImageVo;

@Service("WordImageBO")
@Scope("prototype")
public class WordImageBO extends BaseBo<WordImage> {
	public WordImageBO() {
		setDao(new BaseDao<WordImage>() {
		});
	}

	public Result<Integer> handImage(Integer imageId, User user)
			throws IllegalArgumentException, IllegalAccessException {
		WordImage image = findById(imageId);
		image.setHand(image.getHand() + 1);
		updateEntity(image);

		// 对作者进行奖励
		Global.getUserBO().adjustCowDung(image.getAuthor(), 1, "单词配图UGC得到了赞");

		Event event = new Event(EventType.HandWordImage, user, image);
		Global.getEventBO().createEntity(event);

		return new Result<Integer>(true, null, image.getHand());
	}

	public Result<Integer> footImage(Integer imageId, User user)
			throws IllegalArgumentException, IllegalAccessException {
		WordImage image = findById(imageId);
		image.setFoot(image.getFoot() + 1);
		updateEntity(image);

		// 如果该图片被踩的次数比被赞的次数多3（或以上），删除该图片
		if (image.getFoot() - image.getHand() >= 3) {
			deleteWordImage(imageId, user, false);
		}

		Event event = new Event(EventType.FootWordImage, user, image);
		Global.getEventBO().createEntity(event);

		return new Result<Integer>(true, null, image.getFoot());
	}

	private static void sortWordImages(List<WordImage> wordImages) {
		Collections.sort(wordImages, new Comparator<WordImage>() {
			@Override
			public int compare(WordImage o1, WordImage o2) {
				int score1 = o1.getHand() - o1.getFoot();
				int score2 = o2.getHand() - o2.getFoot();
				if (score1 == score2) {
					return (int) (o2.getCreateTime().getTime() - o1.getCreateTime().getTime());
				} else {
					return score2 - score1;
				}
			}
		});
	}

	/**
	 * 获取一个单词对应的前10个图片
	 *
	 * @return
	 */
	public WordImageVo[] getImagesOfWord(Integer wordId, SessionData sessionData) {

		// 对图片进行排序，被点赞绝对次数多的排在前面
		Word word = Global.getWordBO().findById(wordId);
		List<WordImage> wordImages = word.getImages();
		sortWordImages(wordImages);

		int total = wordImages.size();
		WordImageVo[] images = new WordImageVo[total <= 10 ? total : 10];
		for (int i = 0; i < images.length; i++) {
			WordImage po = wordImages.get(i);
			WordImageVo vo = BeanUtils.makeVO(po, WordImageVo.class, new String[] { "author" });
			UserVo author = new UserVo();
			author.setDisplayNickName(po.getAuthor().getDisplayNickName());
			author.setUserName(po.getAuthor().getUserName());
			vo.setAuthor(author);

			// 判断图片是否被当前登录用户评价过？
			boolean hasBeenVoted = sessionData.getVotedWordImages().contains(vo.getId());
			vo.setHasBeenVoted(hasBeenVoted);

			images[i] = vo;
		}

		return images;
	}

	public void addWordImage(WordImage wordImage, User user) throws IllegalArgumentException, IllegalAccessException {
		// 如果单词的配图已经大于等于9个了，则把最后一个图片删掉（末位淘汰制）
		Word word = wordImage.getWord();
		List<WordImage> images = word.getImages();
		sortWordImages(images);
		while (images.size() >= 9) {
			// 删除数据库记录
			WordImage lastImage = images.remove(images.size() - 1);
			deleteWordImage(lastImage.getId(), user, false);
		}

		// 添加新的单词图片
		createEntity(wordImage);
		images.add(wordImage);

		Event event = new Event(EventType.NewWordImage, user, wordImage);
		Global.getEventBO().createEntity(event);
	}

	public Result<Object> deleteWordImage(int imageId, User user, boolean checkPermission) {
		WordImage image = findById(imageId);
		if (checkPermission) {
			if (!user.getIsAdmin() && (image.getAuthor() == null
					|| !image.getAuthor().getUserName().equalsIgnoreCase(user.getUserName()))) {
				return new Result<Object>(false, "无权限", null);
			}
		}

		// 删除相关的事件记录
		Event exam = new Event();
		exam.setWordImage(image);
		EventBO eventBO = Global.getEventBO();
		eventBO.getDAO().setPreciseEntity(exam);
		List<Event> events = eventBO.queryAll();
		for (Event event : events) {
			Global.getEventBO().deleteEntity(event);
		}

		// 删除数据库记录
		image.getWord().getImages().remove(image);
		image.setWord(null);
		deleteEntity(image);

		// 删除图片文件
		File imageFile = new File(SysParamUtil.getImageBaseDir() + "/word/" + image.getImageFile());
		if (!imageFile.delete()) {
			imageFile.deleteOnExit();
		}

		return new Result<Object>(true, null, null);
	}
}
