package com.antbean.train12306.utils;

import com.antbean.train12306.entity.LoginedUserInfo;

public final class Train12306Context {
	private static LoginedUserInfo lui;

	public static LoginedUserInfo getLoginedUserInfo() {
		return lui;
	}

	protected static void setLoginedUserInfo(LoginedUserInfo loginedUserInfo) {
		lui = loginedUserInfo;
	}

}
