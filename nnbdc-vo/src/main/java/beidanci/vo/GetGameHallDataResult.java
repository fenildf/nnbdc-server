package beidanci.vo;

import java.util.List;

public class GetGameHallDataResult extends Vo {
	List<HallGroupVo> hallGroups;
	List<HallVo> halls;
	List<UserGameVo> userGameVos;

	public GetGameHallDataResult() {
	}

	public List<UserGameVo> getUserGameVos() {
		return userGameVos;
	}

	public void setUserGameVos(List<UserGameVo> userGameVos) {
		this.userGameVos = userGameVos;
	}

	public List<HallGroupVo> getHallGroups() {
		return hallGroups;
	}

	public void setHallGroups(List<HallGroupVo> hallGroups) {
		this.hallGroups = hallGroups;
	}

	public List<HallVo> getHalls() {
		return halls;
	}

	public void setHalls(List<HallVo> halls) {
		this.halls = halls;
	}
}
