package beidanci.po;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

@Entity
@Table(name = "study_group_post")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class StudyGroupPost extends Po implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "postCreator")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "groupId")
	private StudyGroup studyGroup;

	@Column(name = "postTitle", length = 100, nullable = false)
	private String postTitle;

	@Column(name = "postContent", length = 1048576, nullable = false)
	private String postContent;

	@Column(name = "replyCount", nullable = false)
	private Integer replyCount;

	@Column(name = "browseCount", nullable = false)
	private Integer browseCount;

	@Column(name = "lastReplyTime")
	private Date lastReplyTime;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE,
			CascadeType.MERGE }, mappedBy = "studyGroupPost", fetch = FetchType.LAZY)
	@OrderBy("updateTime asc")
	private List<StudyGroupPostReply> studyGroupPostReplies;

	/** default constructor */
	public StudyGroupPost() {
	}

	/** minimal constructor */
	public StudyGroupPost(Integer id, User user, StudyGroup studyGroup, String postTitle, String postContent,
			Integer replyCount, Timestamp lastReplyTime) {
		this.id = id;
		this.user = user;
		this.studyGroup = studyGroup;
		this.postTitle = postTitle;
		this.postContent = postContent;
		this.replyCount = replyCount;
		this.lastReplyTime = lastReplyTime;
	}

	/** full constructor */
	public StudyGroupPost(Integer id, User user, StudyGroup studyGroup, String postTitle, String postContent,
			Integer replyCount, Timestamp lastReplyTime, List studyGroupPostReplies) {
		this.id = id;
		this.user = user;
		this.studyGroup = studyGroup;
		this.postTitle = postTitle;
		this.postContent = postContent;
		this.replyCount = replyCount;
		this.lastReplyTime = lastReplyTime;
		this.studyGroupPostReplies = studyGroupPostReplies;
	}

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public StudyGroup getStudyGroup() {
		return this.studyGroup;
	}

	public void setStudyGroup(StudyGroup studyGroup) {
		this.studyGroup = studyGroup;
	}

	public String getPostTitle() {
		return this.postTitle;
	}

	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}

	public String getPostContent() {
		return this.postContent;
	}

	public void setPostContent(String postContent) {
		this.postContent = postContent;
	}

	public Integer getReplyCount() {
		return this.replyCount;
	}

	public void setReplyCount(Integer replyCount) {
		this.replyCount = replyCount;
	}

	public Integer getBrowseCount() {
		return browseCount;
	}

	public void setBrowseCount(Integer browseCount) {
		this.browseCount = browseCount;
	}

	public Date getLastReplyTime() {
		return lastReplyTime;
	}

	public void setLastReplyTime(Date lastReplyTime) {
		this.lastReplyTime = lastReplyTime;
	}

	public List<StudyGroupPostReply> getStudyGroupPostReplies() {
		return studyGroupPostReplies;
	}

	public void setStudyGroupPostReplies(List<StudyGroupPostReply> studyGroupPostReplies) {
		this.studyGroupPostReplies = studyGroupPostReplies;
	}
}