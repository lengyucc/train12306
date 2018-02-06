package com.antbean.train12306.utils;

import java.util.Date;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.lang3.StringUtils;

public class CookieUtils {
	private static final String DEFAULT_DOMAIN = "kyfw.12306.cn";
	private static final String DEFAULT_PATH = "/";

	public static void addCookie(String domain, String name, String value, String path, Date expires, boolean secure) {
		Train12306HttpUtils.getDefaultHttpClient().getState()
				.addCookie(new Cookie(domain, name, value, path, expires, secure));
	}

	public static void addCookie(String domain, String name, String value, String path) {
		addCookie(domain, name, value, path, null, false);
	}

	public static void addCookie(String name, String value, String path) {
		addCookie(DEFAULT_DOMAIN, name, value, path);
	}

	public static void addCookie(String name, String value) {
		addCookie(name, value, DEFAULT_PATH);
	}

	public static void removeCookie(String domain, String name, String path) {
		Train12306HttpUtils.getDefaultHttpClient().getState()
				.addCookie(new Cookie(domain, name, StringUtils.EMPTY, path, new Date(1), false));
	}

	public static void removeCookie(String name, String path) {
		removeCookie(DEFAULT_DOMAIN, name, path);
	}

	public static void removeCookie(String name) {
		removeCookie(name, DEFAULT_PATH);
	}

	public static void removeAllCookies() {
		Cookie[] cookies = Train12306HttpUtils.getDefaultHttpClient().getState().getCookies();
		for (Cookie cookie : cookies) {
			removeCookie(cookie.getDomain(), cookie.getName(), cookie.getPath());
		}
	}
}
