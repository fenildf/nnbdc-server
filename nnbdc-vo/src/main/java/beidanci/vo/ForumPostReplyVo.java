package beidanci.vo;

public class ForumPostReplyVo extends Vo {
    private Integer id;

    private UserVo user;

    private ForumPostVo forumPost;

    private String content;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserVo getUser() {
        return user;
    }

    public void setUser(UserVo user) {
        this.user = user;
    }

    public ForumPostVo getForumPost() {
        return forumPost;
    }

    public void setForumPost(ForumPostVo forumPost) {
        this.forumPost = forumPost;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
