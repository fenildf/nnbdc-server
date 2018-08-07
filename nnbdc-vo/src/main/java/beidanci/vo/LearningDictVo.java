package beidanci.vo;

/**
 * Created by Administrator on 2015/11/29.
 */
public class LearningDictVo extends Vo {

	private DictVo dict;

	public DictVo getDict() {
		return dict;
	}

	public void setDict(DictVo dict) {
		this.dict = dict;
	}

	public Integer getCurrentWordOrder() {
		return currentWordOrder;
	}

	public void setCurrentWordOrder(Integer currentWordOrder) {
		this.currentWordOrder = currentWordOrder;
	}

	private Integer currentWordOrder;

	private boolean isPrivileged;

	public boolean getIsPrivileged() {
		return isPrivileged;
	}

	public void setIsPrivileged(boolean privileged) {
		isPrivileged = privileged;
	}
}
