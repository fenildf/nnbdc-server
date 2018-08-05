package beidanci.po;

import javax.persistence.*;

import beidanci.vo.EventType;

@Entity
@Table(name = "event")
public class Event extends Po {
	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@Enumerated(EnumType.STRING)
	@Column(name = "eventType", nullable = false, length = 30)
	private EventType eventType;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "wordImage", nullable = true)
	private WordImage wordImage;

	@ManyToOne
	@JoinColumn(name = "sentenceDiyItem", nullable = true)
	private SentenceDiyItem sentenceDiyItem;

	@ManyToOne
	@JoinColumn(name = "wordShortDescChinese", nullable = true)
	private WordShortDescChinese wordShortDescChinese;

	public Event(EventType eventType, User user, WordImage wordImage) {
		this.eventType = eventType;
		this.user = user;
		this.wordImage = wordImage;
	}

	public Event(EventType eventType, User user, SentenceDiyItem diyItem) {
		this.eventType = eventType;
		this.user = user;
		this.sentenceDiyItem = diyItem;
	}

	public Event(EventType eventType, User user, WordShortDescChinese wordShortDescChinese) {
		this.eventType = eventType;
		this.user = user;
		this.wordShortDescChinese = wordShortDescChinese;
	}

	public Event() {

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public WordImage getWordImage() {
		return wordImage;
	}

	public void setWordImage(WordImage wordImage) {
		this.wordImage = wordImage;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public SentenceDiyItem getSentenceDiyItem() {
		return sentenceDiyItem;
	}

	public void setSentenceDiyItem(SentenceDiyItem sentenceDiyItem) {
		this.sentenceDiyItem = sentenceDiyItem;
	}

	public WordShortDescChinese getWordShortDescChinese() {
		return wordShortDescChinese;
	}

	public void setWordShortDescChinese(WordShortDescChinese wordShortDescChinese) {
		this.wordShortDescChinese = wordShortDescChinese;
	}
}
