package beidanci.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;

import beidanci.po.User;
import beidanci.util.BeanUtils;
import beidanci.vo.UserVo;

@SuppressWarnings("deprecation")
public class UserDetailsImpl implements UserDetails {
	private static final long serialVersionUID = 1L;

	private UserVo userVo;

	public UserDetailsImpl(User user) {
		userVo = BeanUtils.makeVO(user, UserVo.class, new String[] { "invitedBy", "StudyGroupVo.creator",
				"StudyGroupVo.users", "StudyGroupVo.managers", "studyGroupPosts", "userGames" });
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthorityImpl> roles = new ArrayList<GrantedAuthorityImpl>();
		roles.add(new GrantedAuthorityImpl("USER"));
		if (userVo.getIsSuper()) {
			roles.add(new GrantedAuthorityImpl("SUPER"));
		}
		if (userVo.getIsAdmin()) {
			roles.add(new GrantedAuthorityImpl("ADMIN"));
		}
		if (userVo.getIsInputor()) {
			roles.add(new GrantedAuthorityImpl("INPUTOR"));
		}
		return roles;
	}

	@Override
	public String getPassword() {
		return userVo.getPassword();
	}

	@Override
	public String getUsername() {
		return userVo.getUserName();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UserDetailsImpl)) {
			return false;
		}
		UserDetailsImpl otherObject = (UserDetailsImpl) obj;
		return userVo.getUserName().equals(otherObject.getUsername());
	}

	@Override
	public int hashCode() {
		return userVo.getUserName().hashCode();
	}

	public UserVo getUserVo() {
		return userVo;
	}

}
