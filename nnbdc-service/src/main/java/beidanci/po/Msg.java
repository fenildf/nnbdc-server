package beidanci.po;

import javax.persistence.*;

@Entity
@Table(name = "msg")
public class Msg extends Po implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "fromUser", nullable = false, updatable = false, insertable = false)
	private User fromUser;

	@ManyToOne
	@JoinColumn(name = "toUser", nullable = false, updatable = false, insertable = false)
	private User toUser;

	@Column(name = "content", length = 4000)
	private String content;

	private String msgType;

	// Constructors

	/** default constructor */
	public Msg() {
	}

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public User getFromUser() {
		return this.fromUser;
	}

	public void setFromUser(User fromUser) {
		this.fromUser = fromUser;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getToUser() {
		return toUser;
	}

	public void setToUser(User toUser) {
		this.toUser = toUser;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

}