package beidanci.vo;

import java.util.List;

public class GetWordResult {
	public GetWordResult(LearningWordVo learningWord, int learningMode, WordVo[] otherWords, int[] progress,
			String soundPath, boolean isFinished, boolean isNoWord, String[] cigens,
			WordAdditionalInfoVo[] additionalInfos, List<ErrorReportVo> errorReports, String shortDesc,
			boolean shouldEnterReviewMode, WordImageVo[] images, VerbTenseVo[] verbTenses, List<SentenceVo> sentences,
			List<WordShortDescChineseVo> shortDescChineses) {
		super();
		this.learningWord = learningWord;
		this.otherWords = otherWords;
		this.progress = progress;
		this.learningMode = learningMode;
		this.soundPath = soundPath;
		this.finished = isFinished;
		this.noWord = isNoWord;
		this.cigens = cigens;
		this.additionalInfos = additionalInfos;
		this.errorReports = errorReports;
		this.shortDesc = shortDesc;
		this.shouldEnterReviewMode = shouldEnterReviewMode;
		this.images = images;
		this.verbTenses = verbTenses;
		this.sentences = sentences;
		this.shortDescChineses = shortDescChineses;
	}

	private LearningWordVo learningWord;

	private WordVo[] otherWords;

	private int[] progress;

	private String soundPath;

	private String[] cigens;

	private WordAdditionalInfoVo[] additionalInfos;

	private List<ErrorReportVo> errorReports;

	private String shortDesc;

	private List<SentenceVo> sentences;

	/**
	 * 通知客户端是否应该进入阶段复习模式
	 */
	private boolean shouldEnterReviewMode;

	/**
	 * 是否学完了今日单词？
	 */
	private boolean finished;

	/**
	 * 是否已经学完了所选单词书的所有单词？
	 */
	private boolean noWord;

	/**
	 * 单词的图片
	 */
	private WordImageVo[] images;

	private List<WordShortDescChineseVo> shortDescChineses;

	/**
	 * 单词各个时态（对于动词有效）
	 */
	private VerbTenseVo[] verbTenses;

	public VerbTenseVo[] getVerbTenses() {
		return verbTenses;
	}

	public void setVerbTenses(VerbTenseVo[] verbTenses) {
		this.verbTenses = verbTenses;
	}

	public WordImageVo[] getImages() {
		return images;
	}

	public void setImages(WordImageVo[] images) {
		this.images = images;
	}

	public LearningWordVo getLearningWord() {
		return learningWord;
	}

	public int[] getProgress() {
		return progress;
	}

	private int learningMode;

	public int getLearningMode() {
		return learningMode;
	}

	public String getSoundPath() {
		return soundPath;
	}

	public boolean isFinished() {
		return finished;
	}

	public boolean isNoWord() {
		return noWord;
	}

	public String[] getCigens() {
		return cigens;
	}

	public WordAdditionalInfoVo[] getAdditionalInfos() {
		return additionalInfos;
	}

	public void setAdditionalInfos(WordAdditionalInfoVo[] additionalInfos) {
		this.additionalInfos = additionalInfos;
	}

	public List<ErrorReportVo> getErrorReports() {
		return errorReports;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}

	public boolean getShouldEnterReviewMode() {
		return shouldEnterReviewMode;
	}

	public WordVo[] getOtherWords() {
		return otherWords;
	}

	public List<SentenceVo> getSentences() {
		return sentences;
	}

	public List<WordShortDescChineseVo> getShortDescChineses() {
		return shortDescChineses;
	}

}
