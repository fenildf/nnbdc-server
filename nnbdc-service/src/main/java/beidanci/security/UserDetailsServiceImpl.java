package beidanci.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import beidanci.Global;
import beidanci.po.User;

public class UserDetailsServiceImpl implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		try {
			User user = Global.getUserBO().getByUserName(userName);
			if (user != null) {
				UserDetails userDetails = new UserDetailsImpl(user);
				return userDetails;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		throw new UsernameNotFoundException(userName);
	}

}
