package beidanci.vo;

public class WordImageVo extends Vo {
    private Integer id;

    private String imageFile;

    private Integer hand;

    private Integer foot;

    private UserVo author;
    
    /**
     * 图片是否被当前登录用户评级过？
     */
    private Boolean hasBeenVoted;

    public Boolean getHasBeenVoted() {
		return hasBeenVoted;
	}

	public void setHasBeenVoted(Boolean hasBeenVoted) {
		this.hasBeenVoted = hasBeenVoted;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public UserVo getAuthor() {
        return author;
    }

    public void setAuthor(UserVo author) {
        this.author = author;
    }
}
