package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.GameHall;
import beidanci.po.GameHallId;
import beidanci.util.BeanUtils;
import beidanci.vo.GameHallVo;

@Service("GameHallBO")
@Scope("prototype")
public class GameHallBO extends BaseBo<GameHall> {
	public GameHallBO() {
		setDao(new BaseDao<GameHall>() {
		});
	}

	public GameHallVo getGameHallVoById(GameHallId id) {
		GameHall gameHall = findById(id);
		GameHallVo vo = BeanUtils.makeVO(gameHall, GameHallVo.class,
				new String[] { "GameHallVo.hallGroup", "dictWords" });
		return vo;
	}
}
