package beidanci.vo;

/**
 * 某个单词释义的一个同义词
 *
 * @author MaYubing
 */
public class SynonymVo extends Vo {

	private MeaningItemVo meaningItem;

	/**
	 * 近义词的ID
	 */
	private Integer wordId;

	public String getSpell() {
		return spell;
	}

	public void setSpell(String spell) {
		this.spell = spell;
	}

	/**
	 * 近义词的拼写
	 */
	private String spell;

	public MeaningItemVo getMeaningItem() {
		return meaningItem;
	}

	public void setMeaningItem(MeaningItemVo meaningItem) {
		this.meaningItem = meaningItem;
	}

	public Integer getWordId() {
		return wordId;
	}

	public void setWordId(Integer wordId) {
		this.wordId = wordId;
	}

}
