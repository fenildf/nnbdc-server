package beidanci.vo;

public class StudyGroupPostReplyVo  extends Vo {
    private Integer id;

    private UserVo user;

    private StudyGroupPostVo studyGroupPost;

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

    public StudyGroupPostVo getStudyGroupPost() {
        return studyGroupPost;
    }

    public void setStudyGroupPost(StudyGroupPostVo studyGroupPost) {
        this.studyGroupPost = studyGroupPost;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
