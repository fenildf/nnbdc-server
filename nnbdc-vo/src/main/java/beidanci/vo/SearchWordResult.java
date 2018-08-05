package beidanci.vo;

import java.util.List;

public class SearchWordResult {
	private WordVo word;
	private List<SentenceVo> sentencesWithUGC;

	public List<SentenceVo> getSentencesWithUGC() {
		return sentencesWithUGC;
	}

	public void setSentencesWithUGC(List<SentenceVo> sentencesWithUGC) {
		this.sentencesWithUGC = sentencesWithUGC;
	}

	public SearchWordResult(WordVo word, String soundPath,
                            List<SentenceVo> sentencesWithUGC) {
		super();
		this.word = word;
		this.soundPath = soundPath;
		this.sentencesWithUGC = sentencesWithUGC;
	}

	private String soundPath;

	public WordVo getWord() {
		return word;
	}

	public void setWord(WordVo word) {
		this.word = word;
	}

	public String getSoundPath() {
		return soundPath;
	}

	public void setSoundPath(String soundPath) {
		this.soundPath = soundPath;
	}
}
