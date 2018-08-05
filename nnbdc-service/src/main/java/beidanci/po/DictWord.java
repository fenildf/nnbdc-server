package beidanci.po;

import javax.persistence.*;

@Entity
@Table(name = "dict_word", indexes = {
		@Index(name = "idx_dict_md5index", columnList = "dictId,md5IndexNo", unique = false) })
public class DictWord extends Po implements java.io.Serializable {
	public DictWord() {

	}

	public DictWord(DictWordId id, Dict dict, Word word) {
		super();
		this.id = id;
		this.dict = dict;
		this.word = word;
	}

	private static final long serialVersionUID = 1L;

	@Id
	private DictWordId id;

	@ManyToOne
	@JoinColumn(name = "dictId", nullable = false, updatable = false, insertable = false)
	private Dict dict;

	@ManyToOne
	@JoinColumn(name = "wordId", nullable = false, updatable = false, insertable = false)
	private Word word;

	/**
	 * 单词按照MD5排序在单词书中的顺序号，从1开始
	 */
	@Column(name = "md5IndexNo", nullable = true)
	private Integer md5IndexNo;

	public Integer getMd5IndexNo() {
		return md5IndexNo;
	}

	public void setMd5IndexNo(Integer md5IndexNo) {
		this.md5IndexNo = md5IndexNo;
	}

	public DictWordId getId() {
		return id;
	}

	public void setId(DictWordId id) {
		this.id = id;
	}

	public Dict getDict() {
		return dict;
	}

	public void setDict(Dict dict) {
		this.dict = dict;
	}

	public Word getWord() {
		return word;
	}

	public void setWord(Word word) {
		this.word = word;
	}

	public DictWord(DictWordId id, Dict dict, Word word, Integer md5IndexNo) {
		super();
		this.id = id;
		this.dict = dict;
		this.word = word;
		this.md5IndexNo = md5IndexNo;
	}

}
