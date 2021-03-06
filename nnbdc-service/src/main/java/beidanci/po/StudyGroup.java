package beidanci.po;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import beidanci.Global;
import beidanci.util.Util;
import beidanci.vo.StudyGroupSummary;

@Entity
@Table(name = "study_group")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class StudyGroup extends Po implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "grade")
	private StudyGroupGrade studyGroupGrade;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator")
	private User creator;

	@Column(name = "groupName", length = 100, nullable = false, unique = true)
	private String groupName;

	@Column(name = "groupTitle", length = 100, nullable = false)
	private String groupTitle;

	@Column(name = "groupRemark", length = 4000, nullable = false)
	private String groupRemark;

	@ManyToMany
	@JoinTable(name = "study_group_and_user_link", joinColumns = {
			@JoinColumn(name = "groupId") }, inverseJoinColumns = { @JoinColumn(name = "userId") })
	private List<User> users;

	@ManyToMany
	@JoinTable(name = "study_group_and_manager_link", joinColumns = {
			@JoinColumn(name = "groupID") }, inverseJoinColumns = { @JoinColumn(name = "userId") })
	private List<User> managers;

	@Column(name = "cowDung", nullable = false)
	private Integer cowDung;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE,
			CascadeType.MERGE }, mappedBy = "studyGroup", fetch = FetchType.LAZY)
	private List<StudyGroupSnapshotDaily> snapshotDailys;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE,
			CascadeType.MERGE }, mappedBy = "studyGroup", fetch = FetchType.LAZY)
	@OrderBy("updateTime desc")
	private List<StudyGroupPost> studyGroupPosts;

	// Constructors

	/**
	 * default constructor
	 */
	public StudyGroup() {
	}

	/**
	 * minimal constructor
	 */
	public StudyGroup(Integer id, StudyGroupGrade studyGroupGrade, User creator, String groupName) {
		this.id = id;
		this.studyGroupGrade = studyGroupGrade;
		this.creator = creator;
		this.groupName = groupName;
	}

	/**
	 * full constructor
	 */
	public StudyGroup(Integer id, StudyGroupGrade studyGroupGrade, User creator, String groupName, List users,
			List managers) {
		this.id = id;
		this.studyGroupGrade = studyGroupGrade;
		this.creator = creator;
		this.groupName = groupName;
		this.users = users;
		this.managers = managers;
	}

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public StudyGroupGrade getStudyGroupGrade() {
		return this.studyGroupGrade;
	}

	public void setStudyGroupGrade(StudyGroupGrade studyGroupGrade) {
		this.studyGroupGrade = studyGroupGrade;
	}

	public User getCreator() {
		return this.creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public String getGroupName() {
		return this.groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupTitle() {
		return groupTitle;
	}

	public void setGroupTitle(String groupTitle) {
		this.groupTitle = groupTitle;
	}

	public String getGroupRemark() {
		return groupRemark;
	}

	public void setGroupRemark(String groupRemark) {
		this.groupRemark = groupRemark;
	}

	public Integer getCowDung() {
		return cowDung;
	}

	public void setCowDung(Integer cowDung) {
		this.cowDung = cowDung;
	}

	/**
	 * 判断小组是否是懒人小组。懒人小组是指：创立时间大于10天，且打卡率小于80%
	 *
	 * @return
	 */
	public boolean isBadGroup() throws ParseException {
		return (new Date().getTime() - getCreateTime().getTime()) > 10 * 24 * 60 * 60 * 1000
				&& getGroupSummary().getDakaRatio() < 0.8;
	}

	private StudyGroupSnapshotDaily getSnapshotOfDay(int groupId, Calendar calendar, Session dbSession)
			throws ParseException {
		Query query = dbSession.createQuery(" from StudyGroupSnapshotDaily where groupId=:groupId and theDate=:day");
		query.setParameter("groupId", groupId);
		query.setParameter("day", Util.removeTimePart(calendar.getTime()));
		String snapshotID = String.format("%d_%s", id, new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
		List<StudyGroupSnapshotDaily> snapshot = query.getResultList();
		return snapshot.size() > 0 ? snapshot.get(0) : null;
	}

	public StudyGroupSummary getGroupSummary() throws ParseException {
		StudyGroupSummary summary = new StudyGroupSummary();

		// 小组人数
		int memberCount = users.size();
		summary.setMemberCount(memberCount);

		// 小组游戏积分, 打卡分和打卡率
		int gameScore = 0;
		int dakaScore = 0;
		int dakaDays = 0;
		int existDays = 0;
		for (User user : users) {
			gameScore += user.getGameScore();
			dakaScore += user.getDakaScore();
			dakaDays += user.getDakaDayCount();
			existDays += user.getExistDays();
		}
		summary.setGameScore(gameScore);
		summary.setDakaScore(dakaScore);
		double dakaRatio = (dakaDays + 0.0) / existDays;
		summary.setDakaRatio(dakaRatio);

		// 取小组最近快照(为了获取小组排名，计算小组排名是个耗时操作，每天夜间计算，所以取到的是前一天的排名)
		SessionFactory sessionFactory = Global.getSessionFactory();
		Session dbSession = sessionFactory.openSession();
		try {
			Calendar calendar = Calendar.getInstance();// 当前时间
			calendar.add(Calendar.DATE, -1); // 得到昨天
			StudyGroupSnapshotDaily snapshot = getSnapshotOfDay(id, calendar, dbSession);

			if (snapshot != null) {
				summary.setGroupOrder(snapshot.getOrderNo());
			} else {// 无快照，说明是新组
				summary.setGroupOrder(1000000);
			}

			// 计算一日内排名升降
			calendar = Calendar.getInstance(); // 当前时间
			calendar.add(Calendar.DATE, -2); // 得到前天
			StudyGroupSnapshotDaily snapshot2 = getSnapshotOfDay(id, calendar, dbSession);
			if (snapshot2 != null && snapshot != null) {
				summary.setDayOrderRise(snapshot.getOrderNo() - snapshot2.getOrderNo());
			}

			// 获取一周前的快照
			calendar = Calendar.getInstance();// 当前时间
			calendar.add(Calendar.DATE, -8); // 得到上周
			StudyGroupSnapshotDaily snapshotAWeekAgo = getSnapshotOfDay(id, calendar, dbSession);
			if (snapshot != null && snapshotAWeekAgo != null) {
				summary.setWeekOrderRise(snapshot.getOrderNo() - snapshotAWeekAgo.getOrderNo());
			}

			// 获取一月前的快照
			calendar = Calendar.getInstance();// 当前时间
			calendar.add(Calendar.DATE, -31); // 得到上个月
			StudyGroupSnapshotDaily snapshotAMonthAgo = getSnapshotOfDay(id, calendar, dbSession);
			if (snapshot != null && snapshotAMonthAgo != null) {
				summary.setWeekOrderRise(snapshot.getOrderNo() - snapshotAMonthAgo.getOrderNo());
			}

		} finally {
			dbSession.close();
		}

		return summary;
	}

	/**
	 * 获取今日打卡人数
	 *
	 * @return
	 */
	public int getTodaysDakaCount() {
		int count = 0;
		for (User user : users) {
			if (user.getHasDakaToday()) {
				count++;
			}
		}
		return count;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public List<User> getManagers() {
		return managers;
	}

	public void setManagers(List<User> managers) {
		this.managers = managers;
	}

	public List<StudyGroupSnapshotDaily> getSnapshotDailys() {
		return snapshotDailys;
	}

	public void setSnapshotDailys(List<StudyGroupSnapshotDaily> snapshotDailys) {
		this.snapshotDailys = snapshotDailys;
	}

	public List<StudyGroupPost> getStudyGroupPosts() {
		return studyGroupPosts;
	}

	public void setStudyGroupPosts(List<StudyGroupPost> studyGroupPosts) {
		this.studyGroupPosts = studyGroupPosts;
	}
}