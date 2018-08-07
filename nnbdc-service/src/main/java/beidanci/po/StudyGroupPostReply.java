package beidanci.po;

import javax.persistence.*;

@Entity
@Table(name = "study_group_post_reply")
public class StudyGroupPostReply extends Po implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "postReplyer")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "postId")
	private StudyGroupPost studyGroupPost;

	@Column(name = "content", length = 1048576, nullable = false)
	private String content;

	// Constructors

	/**
	 * default constructor
	 */
	public StudyGroupPostReply() {
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

	public StudyGroupPost getStudyGroupPost() {
		return this.studyGroupPost;
	}

	public void setStudyGroupPost(StudyGroupPost studyGroupPost) {
		this.studyGroupPost = studyGroupPost;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}