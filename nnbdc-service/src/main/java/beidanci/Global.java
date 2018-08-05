package beidanci;

import org.hibernate.SessionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.context.WebApplicationContext;

import beidanci.bo.*;

public class Global {
	private static WebApplicationContext webAppCtx;

	public static SessionFactory getSessionFactory() {
		return (SessionFactory) webAppCtx.getBean("sessionFactory");
	}

	public static SessionRegistry getSessionRegistry() {
		return (SessionRegistry) webAppCtx.getBean("sessionRegistry");
	}

	public static UserBO getUserBO() {
		return (UserBO) webAppCtx.getBean("UserBO");
	}

	public static DictWordBO getDictWordBO() {
		return (DictWordBO) webAppCtx.getBean("DictWordBO");
	}

	public static EventBO getEventBO() {
		return (EventBO) webAppCtx.getBean("EventBO");
	}

	public static LoginLogBO getLoginLogBO() {
		return (LoginLogBO) webAppCtx.getBean("LoginLogBO");
	}

	public static WordImageBO getWordImageBO() {
		return (WordImageBO) webAppCtx.getBean("WordImageBO");
	}

	public static void setWebAppCtx(WebApplicationContext webAppCtx) {
		Global.webAppCtx = webAppCtx;
	}

	public static SysParamBO getSysParamBO() {
		return (SysParamBO) webAppCtx.getBean("SysParamBO");
	}

	public static ArticleBO getArticleBO() {
		return (ArticleBO) webAppCtx.getBean("ArticleBO");
	}

	public static CigenWordLinkBO getCigenWordLinkBO() {
		return (CigenWordLinkBO) webAppCtx.getBean("CigenWordLinkBO");
	}

	public static DakaBO getDakaBO() {
		return (DakaBO) webAppCtx.getBean("DakaBO");
	}

	public static DictBO getDictBO() {
		return (DictBO) webAppCtx.getBean("DictBO");
	}

	public static DictGroupBO getDictGroupBO() {
		return (DictGroupBO) webAppCtx.getBean("DictGroupBO");
	}

	public static ErrorReportBO getErrorReportBO() {
		return (ErrorReportBO) webAppCtx.getBean("ErrorReportBO");
	}

	public static ForumBO getForumBO() {
		return (ForumBO) webAppCtx.getBean("ForumBO");
	}

	public static ForumPostBO getForumPostBO() {
		return (ForumPostBO) webAppCtx.getBean("ForumPostBO");
	}

	public static ForumPostReplyBO getForumPostReplyBO() {
		return (ForumPostReplyBO) webAppCtx.getBean("ForumPostReplyBO");
	}

	public static GameHallBO getGameHallBO() {
		return (GameHallBO) webAppCtx.getBean("GameHallBO");
	}

	public static GetPwdLogBO getGetPwdLogBO() {
		return (GetPwdLogBO) webAppCtx.getBean("GetPwdLogBO");
	}

	public static HallGroupBO getHallGroupBO() {
		return (HallGroupBO) webAppCtx.getBean("HallGroupBO");
	}

	public static InfoVoteLogBO getInfoVoteLogBO() {
		return (InfoVoteLogBO) webAppCtx.getBean("InfoVoteLogBO");
	}

	public static LearningDictBO getLearningDictBO() {
		return (LearningDictBO) webAppCtx.getBean("LearningDictBO");
	}

	public static LearningWordBO getLearningWordBO() {
		return (LearningWordBO) webAppCtx.getBean("LearningWordBO");
	}

	public static LevelBO getLevelBO() {
		return (LevelBO) webAppCtx.getBean("LevelBO");
	}

	public static MasteredWordBO getMasteredWordBO() {
		return (MasteredWordBO) webAppCtx.getBean("MasteredWordBO");
	}

	public static MsgBO getMsgBO() {
		return (MsgBO) webAppCtx.getBean("MsgBO");
	}

	public static RawWordBO getRawWordBO() {
		return (RawWordBO) webAppCtx.getBean("RawWordBO");
	}

	public static SentenceBO getSentenceBO() {
		return (SentenceBO) webAppCtx.getBean("SentenceBO");
	}

	public static SelectedDictBO getSelectedDictBO() {
		return (SelectedDictBO) webAppCtx.getBean("SelectedDictBO");
	}

	public static SentenceDiyItemBO getSentenceDiyItemBO() {
		return (SentenceDiyItemBO) webAppCtx.getBean("SentenceDiyItemBO");
	}

	public static SentenceDiyItemRemarkBO getSentenceDiyItemRemarkBO() {
		return (SentenceDiyItemRemarkBO) webAppCtx.getBean("SentenceDiyItemRemarkBO");
	}

	public static StudyGroupBO getStudyGroupBO() {
		return (StudyGroupBO) webAppCtx.getBean("StudyGroupBO");
	}

	public static StudyGroupGradeBO getStudyGroupGradeBO() {
		return (StudyGroupGradeBO) webAppCtx.getBean("StudyGroupGradeBO");
	}

	public static StudyGroupPostBO getStudyGroupPostBO() {
		return (StudyGroupPostBO) webAppCtx.getBean("StudyGroupPostBO");
	}

	public static StudyGroupPostReplyBO getStudyGroupPostReplyBO() {
		return (StudyGroupPostReplyBO) webAppCtx.getBean("StudyGroupPostReplyBO");
	}

	public static StudyGroupSnapshotDailyBO getStudyGroupSnapshotDailyBO() {
		return (StudyGroupSnapshotDailyBO) webAppCtx.getBean("StudyGroupSnapshotDailyBO");
	}

	public static UserCowDungLogBO getUserCowDungLogBO() {
		return (UserCowDungLogBO) webAppCtx.getBean("UserCowDungLogBO");
	}

	public static UserGameBO getUserGameBO() {
		return (UserGameBO) webAppCtx.getBean("UserGameBO");
	}

	public static UserScoreLogBO getUserScoreLogBO() {
		return (UserScoreLogBO) webAppCtx.getBean("UserScoreLogBO");
	}

	public static UserSnapshotDailyBO getUserSnapshotDailyBO() {
		return (UserSnapshotDailyBO) webAppCtx.getBean("UserSnapshotDailyBO");
	}

	public static WordAdditionalInfoBO getWordAdditionalInfoBO() {
		return (WordAdditionalInfoBO) webAppCtx.getBean("WordAdditionalInfoBO");
	}

	public static WordBO getWordBO() {
		return (WordBO) webAppCtx.getBean("WordBO");
	}

	public static MeaningItemBO getMeaningItemBO() {
		return (MeaningItemBO) webAppCtx.getBean("MeaningItemBO");
	}

	public static WordSentenceBO getWordSentenceBO() {
		return (WordSentenceBO) webAppCtx.getBean("WordSentenceBO");
	}

	public static SynonymBO getSynonymBO() {
		return (SynonymBO) webAppCtx.getBean("SynonymBO");
	}

	public static AuthenticationManager getAuthenticationManager() {
		return (AuthenticationManager) webAppCtx.getBean("authenticationManager");
	}

	public static SessionAuthenticationStrategy getSessionAuthenticationStrategy() {
		return (SessionAuthenticationStrategy) webAppCtx.getBean("sessionAuthenticationStrategy");
	}

	public static UpdateLogBO getUpdateLogBO() {
		return (UpdateLogBO) webAppCtx.getBean("UpdateLogBO");
	}

	public static WordShortDescChineseBO getWordShortDescChineseBO() {
		return (WordShortDescChineseBO) webAppCtx.getBean("WordShortDescChineseBO");
	}

}
