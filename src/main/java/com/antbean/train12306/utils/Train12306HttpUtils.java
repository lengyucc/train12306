package com.antbean.train12306.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.antbean.train12306.entity.CheckCaptchaResult;
import com.antbean.train12306.entity.LoginResult;
import com.antbean.train12306.handler.HttpResponseHandler;
import com.antbean.train12306.handler.impl.StreamHttpResponseHandler;
import com.antbean.train12306.handler.impl.StringHttpResponseHandler;

public class Train12306HttpUtils {
	public static HttpClient defaultClient;
	public static final StringHttpResponseHandler STRING_HTTP_RESPONSE_HANDLER = new StringHttpResponseHandler();
	public static final StreamHttpResponseHandler STREAM_HTTP_RESPONSE_HANDLER = new StreamHttpResponseHandler();
	private static Cookie[] cookies;
	private static String uamtk;

	public static synchronized HttpClient getDefaultHttpClient() {
		if (null == defaultClient) {
			defaultClient = new HttpClient();
			// defaultClient.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
		}
		return defaultClient;
	}

	public static String doReq(String uri) {
		return doReq(uri, "get");
	}

	public static String doReq(String uri, String method) {
		return doReq(uri, method, null);
	}

	public static String doReq(String uri, String method, String queryString) {
		return (String) doReq(uri, method, queryString, STRING_HTTP_RESPONSE_HANDLER);
	}

	public static Object doReq(String uri, String method, String queryString, HttpResponseHandler<?> handler) {
		HttpMethod httpMethod = null;
		if ("get".equalsIgnoreCase(method)) {
			httpMethod = new GetMethod(uri);
		} else if ("post".equalsIgnoreCase(method)) {
			httpMethod = new PostMethod(uri);
		} else {
			throw new IllegalArgumentException("无效的请求方式");
		}
		if (StringUtils.isNotBlank(queryString)) {
			httpMethod.setQueryString(queryString);
		}
//		httpMethod.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//		httpMethod.setRequestHeader("Accept-Encoding", "gzip, deflate, br");
//		httpMethod.setRequestHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
//		httpMethod.setRequestHeader("Connection", "keep-alive");
//		httpMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
//		httpMethod.setRequestHeader("Host", "kyfw.12306.cn");
//		httpMethod.setRequestHeader("Referer", "https://kyfw.12306.cn/otn/modifyUser/initQueryUserInfo");
//		httpMethod.setRequestHeader("Upgrade-Insecure-Requests", "1");
//		httpMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0");
//		if(cookies != null){
//			StringBuffer buff = new StringBuffer();
//			for (Cookie cookie : cookies) {
//				buff.append(cookie.getName() + "=" + cookie.getValue() + ";");
//			}
//			httpMethod.setRequestHeader("Cookie", buff.substring(0, buff.length() - 1));
//		}
		try {
			HttpClient client = getDefaultHttpClient();
//			if(uamtk != null){
//				System.out.println("---------->>>>>>>>>>>>>>>>>>>>>>>>>");
//				client.getState().addCookie(new Cookie("kyfw.12306.cn", "uamtk", uamtk));
//			}
//			if(cookies != null){
//				client.getState().addCookies(cookies);
//			}
//			System.out.println("###########################################");
//			System.out.println("----------------" + uri + "----------------");
//			System.out.println("----------------前----------------");
//			showCookies();
			int responseCode = client.executeMethod(httpMethod);
//			System.out.println("----------------后----------------");
//			showCookies();
//			System.out.println("###########################################");
//			cookies = client.getState().getCookies();
			return handler.process(responseCode, httpMethod);
		} catch (HttpException e) {
			throw new RuntimeException("网络异常", e);
		} catch (IOException e) {
			throw new RuntimeException("IO异常", e);
		}
	}

	public static void showCookies() {
		HttpClient client = getDefaultHttpClient();
		Cookie[] cookies = client.getState().getCookies();
		for (Cookie cookie : cookies) {
			System.out.println(cookie.getName() + " : " + cookie.getValue());
		}
	}

	/**
	 * @Title: writeCaptcha
	 * @Description: 获取验证码
	 * @param out
	 * @return
	 */
	public static int writeCaptcha(OutputStream out) {
		try {
			InputStream in = (InputStream) doReq(Train12306Urls.GET_CAPTCHA_URL, "get", null,
					STREAM_HTTP_RESPONSE_HANDLER);
			return IOUtils.copy(in, out);
		} catch (IOException e) {
			throw new RuntimeException("获取验证码失败", e);
		}
	}

	public static void login(String username, String password, String captcha) {
		// 1.校验验证码
		doReq(Train12306Urls.CHECK_CAPTCHA_URL, "get", "answer=" + captcha + "&login_site=E&rand=sjrand",
				new HttpResponseHandler<String>() {
					@Override
					public String process(int responseCode, HttpMethod httpMethod) throws IOException {
						if (200 == responseCode) {
							String responseBodyAsString = httpMethod.getResponseBodyAsString();
							CheckCaptchaResult ccr = JSON.parseObject(responseBodyAsString, CheckCaptchaResult.class);
							if (!"4".equals(ccr.getResult_code())) {
								throw new RuntimeException(ccr.getResult_message());
							}
							System.out.println(ccr.getResult_message());
							return httpMethod.getResponseBodyAsString();
						}
						throw new RuntimeException("错误的状态码" + responseCode);
					}
				});
		// 2.登录
		doReq(Train12306Urls.LOGIN_URL, "post", "username=" + username + "&password=" + password + "&appid=otn",
				new HttpResponseHandler<String>() {
					@Override
					public String process(int responseCode, HttpMethod httpMethod) throws IOException {
						if (200 == responseCode) {
							String responseBodyAsString = httpMethod.getResponseBodyAsString();
							LoginResult lr = JSON.parseObject(responseBodyAsString, LoginResult.class);
							if (0 != lr.getResult_code()) {
								throw new RuntimeException(lr.getResult_message());
							}
							System.out.println(lr.getResult_message());
							uamtk = lr.getUamtk();
							return httpMethod.getResponseBodyAsString();
						}
						throw new RuntimeException("错误的状态码" + responseCode);
					}
				});
	}

	public static boolean checkLoginStatus() {
		return (boolean) doReq(Train12306Urls.QUERY_USER_INFO_URL, "get", null, new HttpResponseHandler<Boolean>() {
			@Override
			public Boolean process(int responseCode, HttpMethod httpMethod) throws IOException {
				if (200 == responseCode) {
					String responseBodyAsString = httpMethod.getResponseBodyAsString();
					return responseBodyAsString.matches("<form[\\s]+id=\"modifyUserForm\".*");
				}
				return false;
			}
		});
	}

	public static void main(String[] args) throws Exception {
		System.out.println(checkLoginStatus());
	}

}
