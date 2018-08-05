package beidanci.vo;

/**
 * Created by Administrator on 2015/12/11.
 */
public class SelectedDictVo extends Vo {
    private DictVo dict;

    private UserVo user;

    private Boolean isPrivileged;

    public DictVo getDict() {
        return dict;
    }

    public void setDict(DictVo dict) {
        this.dict = dict;
    }

    public UserVo getUser() {
        return user;
    }

    public void setUser(UserVo user) {
        this.user = user;
    }

    public Boolean getIsPrivileged() {
        return isPrivileged;
    }

    public void setIsPrivileged(Boolean privileged) {
        isPrivileged = privileged;
    }
}
