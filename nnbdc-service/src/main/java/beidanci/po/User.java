package beidanci.po;

import beidanci.Global;
import beidanci.bo.StudyGroupBO;
import beidanci.util.EmojiFilter;
import beidanci.util.Util;
import beidanci.util.Utils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user", indexes = {@Index(name = "idx_userName", columnList = "userName", unique = true)})
@SuppressWarnings({"rawtypes", "unchecked"})
@Cache(region = "userCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class User extends Po implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
    @Column(name = "id")
    private Integer id = null;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    private static List<Level> levels;
    @Column(name = "userName", length = 100)
    private String userName;
    @Column(name = "nickName", length = 100)
    private String nickName;
    @Column(name = "password", length = 64)
    private String password;
    @Column(name = "lastLoginTime")
    private Date lastLoginTime;
    @Column(name = "lastShareTime")
    private Date lastShareTime;
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "xiaoAppOpenId", length = 64)
    private String xiaoAppOpenId;
    @Column(name = "xiaoAppNickName", length = 100)
    private String xiaoAppNickName;
    @Column(name = "xiaoAppFigureUrl", length = 200)
    private String xiaoAppFigureUrl;

    @Column(name = "lastLearningDate")
    private Date lastLearningDate;
    @Column(name = "learnedDays", nullable = false)
    private Integer learnedDays;
    @Column(name = "lastLearningPosition")
    private Integer lastLearningPosition;
    @Column(name = "lastLearningMode")
    private Integer lastLearningMode;
    @Column(name = "learningFinished", nullable = false)
    private Boolean learningFinished;
    @Column(name = "inviteAwardTaken", nullable = false)
    private Boolean inviteAwardTaken;
    @Column(name = "isSuper", nullable = false)
    private Boolean isSuper;
    @Column(name = "isAdmin", nullable = false)
    private Boolean isAdmin;
    @Column(name = "isInputor", nullable = false)
    private Boolean isInputor;
    @Column(name = "isRawWordBookPrivileged", nullable = false)
    private Boolean isRawWordBookPrivileged;
    @Column(name = "autoPlaySentence", nullable = false)
    private Boolean autoPlaySentence;
    @Column(name = "wordsPerDay", nullable = false)
    private Integer wordsPerDay;
    @Column(name = "dakaDayCount", nullable = false)
    private Integer dakaDayCount;
    @Column(name = "masteredWords", nullable = false)
    private Integer masteredWordsCount;
    @Column(name = "cowDung", nullable = false)
    private Integer cowDung;
    @Column(name = "throwDiceChance", nullable = false)
    private Integer throwDiceChance;

    public Integer getGameScore() {
        return gameScore;
    }

    public void setGameScore(Integer gameScore) {
        this.gameScore = gameScore;
    }

    @Column(name = "gameScore", nullable = false)
    private Integer gameScore;

    /**
     * 是否直接显示备选答案
     */
    @Column(name = "showAnswersDirectly", nullable = false)
    private Boolean showAnswersDirectly;
    /**
     * 是否自动朗读单词发音
     */
    @Column(name = "autoPlayWord", nullable = false)
    private Boolean autoPlayWord;
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE,
            CascadeType.MERGE}, mappedBy = "user", fetch = FetchType.LAZY)
    private List<SelectedDict> selectedDicts;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE,
            CascadeType.MERGE}, mappedBy = "user", fetch = FetchType.LAZY)
    @OrderBy("dictId asc")
    private List<LearningDict> learningDicts;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE,
            CascadeType.MERGE}, mappedBy = "user", fetch = FetchType.LAZY)
    private List<MasteredWord> masteredWords;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE,
            CascadeType.MERGE}, mappedBy = "user", fetch = FetchType.LAZY)
    private List<LearningWord> learningWords;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE,
            CascadeType.MERGE}, mappedBy = "fromUser", fetch = FetchType.LAZY)
    private List<Msg> sentMsgs;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE,
            CascadeType.MERGE}, mappedBy = "toUser", fetch = FetchType.LAZY)
    private List<Msg> recvedMsgs;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE,
            CascadeType.MERGE}, mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserGame> userGames;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE,
            CascadeType.MERGE}, mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserCowDungLog> userCowDungLogs;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE,
            CascadeType.MERGE}, mappedBy = "user", fetch = FetchType.LAZY)
    private List<Daka> dakas;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE,
            CascadeType.MERGE}, mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserScoreLog> userScoreLogs;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE,
            CascadeType.MERGE}, mappedBy = "user", fetch = FetchType.LAZY)
    @OrderBy("theDate asc")
    private List<UserSnapshotDaily> userSnapshotDailys;
    @ManyToOne
    @JoinColumn(name = "invitedBy", nullable = true)
    private User invitedBy;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE,
            CascadeType.MERGE}, mappedBy = "invitedBy", fetch = FetchType.LAZY)
    private List<User> invitedUsers;
    @ManyToMany(mappedBy = "users")
    private List<StudyGroup> studyGroups;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE,
            CascadeType.MERGE}, mappedBy = "creator", fetch = FetchType.LAZY)
    private List<StudyGroup> createdStudyGroups;
    @ManyToMany(mappedBy = "managers")
    private List<StudyGroup> managedStudyGroups;
    /**
     * 缓存发到客户端的单词，当这个缓存累积到一定大小（比如10个），服务端就会指示客户端进入阶段复习页面，当阶段复习完成后，此缓存清空
     */
    @ManyToMany
    @JoinTable(name = "user_stage_word", joinColumns = {@JoinColumn(name = "userId")}, inverseJoinColumns = {
            @JoinColumn(name = "wordId")})
    @Fetch(FetchMode.SUBSELECT)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<Word> stageWords;

    /**
     * 缓存用户回答错误的单词
     */
    @ManyToMany
    @JoinTable(name = "user_wrong_word", joinColumns = {@JoinColumn(name = "userId")}, inverseJoinColumns = {
            @JoinColumn(name = "wordId")})
    private List<Word> wrongWords;

    /**
     * 连续打卡天数
     */
    @Column(name = "continuousDakaDayCount", nullable = false)
    private Integer continuousDakaDayCount;

    /**
     * 最大连续打卡天数
     */
    @Column(name = "maxContinuousDakaDayCount", nullable = false)
    private Integer maxContinuousDakaDayCount;

    /**
     * 最近一次打卡的日期
     */
    @Column(name = "lastDakaDate", nullable = true)
    private Date lastDakaDate;

    public void setDakaScore(Integer dakaScore) {
        this.dakaScore = dakaScore;
    }

    /**
     * 打卡积分
     */
    @Column(name = "dakaScore", nullable = false)
    private Integer dakaScore;

    public String getXiaoAppNickName() {
        return xiaoAppNickName;
    }

    public void setXiaoAppNickName(String xiaoAppNickName) {
        this.xiaoAppNickName = xiaoAppNickName;
    }

    public String getXiaoAppFigureUrl() {
        return xiaoAppFigureUrl;
    }

    public void setXiaoAppFigureUrl(String xiaoAppFigureUrl) {
        this.xiaoAppFigureUrl = xiaoAppFigureUrl;
    }

    /**
     * default constructor
     */
    public User() {
    }

    public String getXiaoAppOpenId() {
        return xiaoAppOpenId;
    }

    public void setXiaoAppOpenId(String xiaoAppOpenId) {
        this.xiaoAppOpenId = xiaoAppOpenId;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Boolean getShowAnswersDirectly() {
        return showAnswersDirectly;
    }

    public void setShowAnswersDirectly(Boolean showAnswersDirectly) {
        this.showAnswersDirectly = showAnswersDirectly;
    }

    public Integer getContinuousDakaDayCount() {
        return continuousDakaDayCount;
    }

    public void setContinuousDakaDayCount(Integer continuousDakaDayCount) {
        this.continuousDakaDayCount = continuousDakaDayCount;
    }

    public Integer getMaxContinuousDakaDayCount() {
        return maxContinuousDakaDayCount;
    }

    public void setMaxContinuousDakaDayCount(Integer maxContinuousDakaDayCount) {
        this.maxContinuousDakaDayCount = maxContinuousDakaDayCount;
    }

    public Date getLastDakaDate() {
        return lastDakaDate;
    }

    public void setLastDakaDate(Date lastDakaDate) {
        this.lastDakaDate = lastDakaDate;
    }

    public List<Word> getWrongWords() {
        return wrongWords;
    }

    public void setWrongWords(List<Word> wrongWords) {
        this.wrongWords = wrongWords;
    }

    // Constructors

    public List<Word> getStageWords() {
        return stageWords;
    }

    // Property accessors

    public void setStageWords(List<Word> stageWords) {
        this.stageWords = stageWords;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = EmojiFilter.filterEmoji(nickName);
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getLastLearningDate() {
        return this.lastLearningDate;
    }

    public void setLastLearningDate(Date lastLearningDate) {
        this.lastLearningDate = lastLearningDate;
    }

    public Integer getLearnedDays() {
        return this.learnedDays;
    }

    public void setLearnedDays(Integer learnedDays) {
        this.learnedDays = learnedDays;
    }

    public Integer getLastLearningPosition() {
        return this.lastLearningPosition;
    }

    public void setLastLearningPosition(Integer lastLearningPosition) {
        this.lastLearningPosition = lastLearningPosition;
    }

    public Integer getLastLearningMode() {
        return this.lastLearningMode;
    }

    public void setLastLearningMode(Integer lastLearningMode) {
        this.lastLearningMode = lastLearningMode;
    }

    public Boolean getLearningFinished() {
        return this.learningFinished;
    }

    public void setLearningFinished(Boolean learningFinished) {
        this.learningFinished = learningFinished;
    }

    public Integer getWordsPerDay() {
        return this.wordsPerDay;
    }

    public void setWordsPerDay(Integer wordsPerDay) {
        this.wordsPerDay = wordsPerDay;
    }

    public Integer getMasteredWordsCount() {
        return this.masteredWordsCount;
    }

    public void setMasteredWordsCount(Integer masteredWords) {
        this.masteredWordsCount = masteredWords;
    }

    public Integer getCowDung() {
        return this.cowDung;
    }

    public void setCowDung(Integer cowDung) {
        this.cowDung = cowDung;
    }

    @Override
    public int hashCode() {
        return userName.hashCode();
    }

    public boolean equals(Object obj) {
        return userName.equals(((User) obj).getUserName());
    }

    public Integer getThrowDiceChance() {
        return throwDiceChance;
    }

    public void setThrowDiceChance(Integer throwDiceChance) {
        this.throwDiceChance = throwDiceChance;
    }

    public User getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(User invitedBy) {
        this.invitedBy = invitedBy;
    }

    public Boolean getInviteAwardTaken() {
        return inviteAwardTaken;
    }

    public void setInviteAwardTaken(Boolean inviteAwardTaken) {
        this.inviteAwardTaken = inviteAwardTaken;
    }

    public List<UserSnapshotDaily> getUserSnapshotDailys() {
        return userSnapshotDailys;
    }

    public void setUserSnapshotDailys(List<UserSnapshotDaily> userSnapshotDailys) {
        this.userSnapshotDailys = userSnapshotDailys;
    }

    /**
     * 判断用户今日是否已打卡
     *
     * @return
     */
    public boolean getHasDakaToday() {
        DakaId id = new DakaId(this.id, Utils.getPureDate(new Date()));
        Daka daka = Global.getDakaBO().findById(id);
        return daka != null;
    }

    /**
     * 获取用户从注册至今的存在天数
     *
     * @return
     */
    public int getExistDays() {

        long existTime = Utils.getPureDate(new Date()).getTime() - Utils.getPureDate(getCreateTime()).getTime();
        int existDays = (int) (existTime / 1000 / 60 / 60 / 24) + 1;

        return existDays;
    }

    /**
     * 获取用户的打卡率
     *
     * @return
     */
    public double getDakaRatio() {
        int existDays = getExistDays();
        double dakaRatio = (dakaDayCount + 0.0) / existDays;
        return dakaRatio;
    }

    public String getDisplayNickName() {
        return Util.getNickNameOfUser(this);
    }

    /**
     * 计算用户的打卡积分
     *
     * @return
     */
    public Integer getDakaScore() {
        return dakaScore;
    }

    /**
     * 获取用户的积分（包括打卡分和游戏积分）
     *
     * @return
     */
    public int getTotalScore() {
        return getDakaScore() + getGameScore();
    }

    private List<Level> getLevels() {
        if (levels == null) {
            levels = new ArrayList<Level>();
            levels.addAll(Global.getLevelBO().queryAll());
        }
        return levels;
    }

    public static void setLevels(List<Level> levels) {
        User.levels = levels;
    }

    /**
     * 获取用户的等级
     *
     * @return
     */
    public Level getLevel() {
        int userTotalScore = getTotalScore();
        List<Level> levels = getLevels();
        for (Level level : levels) {
            if (userTotalScore >= level.getMinScore() && userTotalScore <= level.getMaxScore()) {
                return level;
            }
        }
        return null;
    }

    public Boolean getIsSuper() {
        return isSuper;
    }

    public void setIsSuper(Boolean isSuper) {
        this.isSuper = isSuper;
    }

    public Integer getDakaDayCount() {
        return dakaDayCount;
    }

    public void setDakaDayCount(Integer dakaDayCount) {
        this.dakaDayCount = dakaDayCount;
    }

    public Boolean getIsRawWordBookPrivileged() {
        return isRawWordBookPrivileged;
    }

    public void setIsRawWordBookPrivileged(Boolean isRawWordBookPrivileged) {
        this.isRawWordBookPrivileged = isRawWordBookPrivileged;
    }

    public Boolean getAutoPlaySentence() {
        return autoPlaySentence;
    }

    public void setAutoPlaySentence(Boolean autoPlaySentence) {
        this.autoPlaySentence = autoPlaySentence;
    }

    public String exitGroup(int groupID) throws IllegalArgumentException, IllegalAccessException {
        StudyGroupBO groupDAO = Global.getStudyGroupBO();
        StudyGroup group = groupDAO.findById(groupID);

        // 创建者不允许退出小组
        if (group.getCreator().getId().equals(id)) {
            return "小组的创建者不允许退出小组";
        }

        // 首先尝试把用户从管理员中删除
        group.getManagers().remove(this);

        // 然后把用户从组中删除
        group.getUsers().remove(this);
        groupDAO.updateEntity(group);
        return null;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getLastShareTime() {
        return lastShareTime;
    }

    public void setLastShareTime(Date lastShareTime) {
        this.lastShareTime = lastShareTime;
    }

    public Boolean getAutoPlayWord() {
        return autoPlayWord;
    }

    public void setAutoPlayWord(Boolean autoPlayWord) {
        this.autoPlayWord = autoPlayWord;
    }

    public Boolean getIsInputor() {
        return isInputor;
    }

    public void setIsInputor(Boolean isInputor) {
        this.isInputor = isInputor;
    }

    public Boolean getRawWordBookPrivileged() {
        return isRawWordBookPrivileged;
    }

    public void setRawWordBookPrivileged(Boolean rawWordBookPrivileged) {
        isRawWordBookPrivileged = rawWordBookPrivileged;
    }

    public List<SelectedDict> getSelectedDicts() {
        return selectedDicts;
    }

    public void setSelectedDicts(List<SelectedDict> selectedDicts) {
        this.selectedDicts = selectedDicts;
    }

    public List<LearningDict> getLearningDicts() {
        return learningDicts;
    }

    public void setLearningDicts(List<LearningDict> learningDicts) {
        this.learningDicts = learningDicts;
    }

    public List<MasteredWord> getMasteredWords() {
        return masteredWords;
    }

    public void setMasteredWords(List<MasteredWord> masteredWords) {
        this.masteredWords = masteredWords;
    }

    public List<LearningWord> getLearningWords() {
        return learningWords;
    }

    public void setLearningWords(List<LearningWord> learningWords) {
        this.learningWords = learningWords;
    }

    public List<Msg> getSentMsgs() {
        return sentMsgs;
    }

    public void setSentMsgs(List<Msg> sentMsgs) {
        this.sentMsgs = sentMsgs;
    }

    public List<Msg> getRecvedMsgs() {
        return recvedMsgs;
    }

    public void setRecvedMsgs(List<Msg> recvedMsgs) {
        this.recvedMsgs = recvedMsgs;
    }

    public List<UserGame> getUserGames() {
        return userGames;
    }

    public void setUserGames(List<UserGame> userGames) {
        this.userGames = userGames;
    }

    public List<UserCowDungLog> getUserCowDungLogs() {
        return userCowDungLogs;
    }

    public void setUserCowDungLogs(List<UserCowDungLog> userCowDungLogs) {
        this.userCowDungLogs = userCowDungLogs;
    }

    public List<Daka> getDakas() {
        return dakas;
    }

    public void setDakas(List<Daka> dakas) {
        this.dakas = dakas;
    }

    public List<UserScoreLog> getUserScoreLogs() {
        return userScoreLogs;
    }

    public void setUserScoreLogs(List<UserScoreLog> userScoreLogs) {
        this.userScoreLogs = userScoreLogs;
    }

    public List<User> getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(List<User> invitedUsers) {
        this.invitedUsers = invitedUsers;
    }

    public List<StudyGroup> getStudyGroups() {
        return studyGroups;
    }

    public void setStudyGroups(List<StudyGroup> studyGroups) {
        this.studyGroups = studyGroups;
    }

    public List<StudyGroup> getCreatedStudyGroups() {
        return createdStudyGroups;
    }

    public void setCreatedStudyGroups(List<StudyGroup> createdStudyGroups) {
        this.createdStudyGroups = createdStudyGroups;
    }

    public List<StudyGroup> getManagedStudyGroups() {
        return managedStudyGroups;
    }

    public void setManagedStudyGroups(List<StudyGroup> managedStudyGroups) {
        this.managedStudyGroups = managedStudyGroups;
    }
}