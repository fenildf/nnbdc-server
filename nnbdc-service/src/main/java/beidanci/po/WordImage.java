package beidanci.po;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "word_image")
@Cache(region = "wordCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class WordImage extends Po {
	private static final long serialVersionUID = 1L;

	public WordImage(Word word, String imageFile, Integer hand, Integer foot, User author) {
		super();
		this.word = word;
		this.imageFile = imageFile;
		this.hand = hand;
		this.foot = foot;
		this.author = author;
	}

	public WordImage() {
		super();
	}

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "wordId", nullable = false)
	private Word word;

	@Column(name = "imageFile")
	private String imageFile;

	@Column(name = "hand")
	private Integer hand;

	@Column(name = "foot")
	private Integer foot;

	@ManyToOne
	@JoinColumn(name = "author", nullable = false)
	private User author;

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Word getWord() {
		return word;
	}

	public void setWord(Word word) {
		this.word = word;
	}

	public String getImageFile() {
		return imageFile;
	}

	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}

	public Integer getHand() {
		return hand;
	}

	public void setHand(Integer hand) {
		this.hand = hand;
	}

	public Integer getFoot() {
		return foot;
	}

	public void setFoot(Integer foot) {
		this.foot = foot;
	}

	public WordImage(Integer id, Word word, String imageFile, Integer hand, Integer foot, User author) {
		super();
		this.id = id;
		this.word = word;
		this.imageFile = imageFile;
		this.hand = hand;
		this.foot = foot;
		this.author = author;
	}
}
