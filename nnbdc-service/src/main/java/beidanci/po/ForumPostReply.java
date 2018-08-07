package beidanci.po;

import javax.persistence.*;

@Entity
@Table(name = "forum_post_reply")
public class ForumPostReply extends Po implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "postReplyer", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "postId", nullable = false)
	private ForumPost forumPost;

	@Column(name = "content", length = 1048576, nullable = false)
	private String content;

	// Constructors

	/**
	 * default constructor
	 */
	public ForumPostReply() {
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

	public ForumPost getForumPost() {
		return this.forumPost;
	}

	public void setForumPost(ForumPost forumPost) {
		this.forumPost = forumPost;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}