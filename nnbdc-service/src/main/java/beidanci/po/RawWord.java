package beidanci.po;

import java.io.IOException;

import javax.persistence.*;

import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;

@Entity
@Table(name = "raw_word")
public class RawWord extends Po implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@javax.persistence.TableGenerator(name = "id_gen", allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "wordId", nullable = false)
	private Word word;

	@Column(name = "createManner", length = 50, nullable = false)
	private String createManner;

	// Constructors

	/**
	 * default constructor
	 */
	public RawWord() {
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

	public String getCreateManner() {
		return this.createManner;
	}

	public void setCreateManner(String createManner) {
		this.createManner = createManner;
	}

	public void setWord(Word word) {
		this.word = word;
	}

	public Word getWord() throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		return word;
	}
}