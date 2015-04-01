package com.google.gwt.sample.crimehub.server;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.sample.crimehub.client.LoginInfo;
import com.google.gwt.sample.crimehub.client.LoginService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class LoginServiceImpl extends RemoteServiceServlet implements
LoginService {

	public LoginInfo login(String requestUri) {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		LoginInfo loginInfo = new LoginInfo();

		if (user != null) {
			if (userService.isUserAdmin()) {
				loginInfo.setAdmin(true);
			}
			loginInfo.setLoggedIn(true);
			loginInfo.setEmailAddress(user.getEmail());
			loginInfo.setNickname(user.getNickname());
			loginInfo.setLogoutUrl(userService.createLogoutURL(requestUri));
			System.out.println("User logged in? " + loginInfo.isLoggedIn());
		} else {
		    loginInfo.setLoginUrl(userService.createLoginURL(requestUri));
		    System.out.println("User logged in? " + loginInfo.isLoggedIn());
		}
		return loginInfo;
	}
}
