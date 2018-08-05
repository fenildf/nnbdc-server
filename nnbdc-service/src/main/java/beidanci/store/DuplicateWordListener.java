package beidanci.store;

import beidanci.vo.WordVo;

public interface DuplicateWordListener {
	public void onDuplicateWord(WordVo word);
}
