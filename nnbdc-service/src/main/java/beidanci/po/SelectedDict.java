package beidanci.po;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "selected_dict")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SelectedDict extends Po implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private SelectedDictId id;

	@ManyToOne
	@JoinColumn(name = "dictId", nullable = false, updatable = false, insertable = false)
	private Dict dict;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false, updatable = false, insertable = false)
	private User user;

	@Column(name = "IsPrivileged", nullable = false)
	private Boolean isPrivileged;

	// Constructors

	/**
	 * default constructor
	 */
	public SelectedDict() {
	}

	/**
	 * full constructor
	 */
	public SelectedDict(SelectedDictId id, Dict dict, User user, Boolean isPrivileged) {
		this.id = id;
		this.dict = dict;
		this.user = user;
		this.isPrivileged = isPrivileged;
	}

	// Property accessors

	public SelectedDictId getId() {
		return this.id;
	}

	public void setId(SelectedDictId id) {
		this.id = id;
	}

	public Dict getDict() {
		return this.dict;
	}

	public void setDict(Dict dict) {
		this.dict = dict;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Boolean getIsPrivileged() {
		return isPrivileged;
	}

	public void setIsPrivileged(Boolean isPrivileged) {
		this.isPrivileged = isPrivileged;
	}

}