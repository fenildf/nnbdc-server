package beidanci.vo;

import java.util.List;
import java.util.Set;

public class ForumVo extends Vo {
    private Integer id;

    private String name;

    private List<UserVo> managers;

    private List<ForumPostVo> forumPosts;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserVo> getManagers() {
        return managers;
    }

    public void setManagers(List<UserVo> managers) {
        this.managers = managers;
    }

    public List<ForumPostVo> getForumPosts() {
        return forumPosts;
    }

    public void setForumPosts(List<ForumPostVo> forumPosts) {
        this.forumPosts = forumPosts;
    }
}
