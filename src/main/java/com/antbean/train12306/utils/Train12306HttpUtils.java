package com.antbean.train12306.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.alibaba.fastjson.JSON;
import com.antbean.train12306.entity.CheckCaptchaResult;
import com.antbean.train12306.entity.LoginResult;
import com.antbean.train12306.entity.UamtkAuthResult;
import com.antbean.train12306.handler.HttpResponseHandler;
import com.antbean.train12306.handler.impl.StreamHttpResponseHandler;
import com.antbean.train12306.handler.impl.StringHttpResponseHandler;

public class Train12306HttpUtils {
	public static HttpClient defaultClient;
	public static final StringHttpResponseHandler STRING_HTTP_RESPONSE_HANDLER = new StringHttpResponseHandler();
	public static final StreamHttpResponseHandler STREAM_HTTP_RESPONSE_HANDLER = new StreamHttpResponseHandler();

	public static synchronized HttpClient getDefaultHttpClient() {
		if (null == defaultClient) {
			defaultClient = new HttpClient();
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
		try {
			HttpClient client = getDefaultHttpClient();
			int responseCode = client.executeMethod(httpMethod);
			showCookies();
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
	 * 获取验证码
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
							return httpMethod.getResponseBodyAsString();
						}
						throw new RuntimeException("错误的状态码" + responseCode);
					}
				});
		// 3.验证登录
		getDefaultHttpClient().getState().addCookie(new Cookie("kyfw.12306.cn", "current_captcha_type", "Z"));
		UamtkAuthResult uamtkAuthResult = (UamtkAuthResult) doReq(
				"https://kyfw.12306.cn/passport/web/auth/uamtk?appid=otn", "post", null,
				new HttpResponseHandler<UamtkAuthResult>() {
					@Override
					public UamtkAuthResult process(int responseCode, HttpMethod httpMethod) throws IOException {
						if (200 == responseCode) {
							String responseBodyAsString = httpMethod.getResponseBodyAsString();
							// {"result_message":"验证通过","result_code":0,"apptk":null,"newapptk":"Cz2EHtYAmjS5slF6SMP5pdvzlzbmro07mN7dW46t_e_ZF9VRty1210"}
							UamtkAuthResult uamtkAuthResult = JSON.parseObject(responseBodyAsString,
									UamtkAuthResult.class);

							System.out.println(responseBodyAsString);
							return uamtkAuthResult;
						}
						throw new RuntimeException("错误的状态码" + responseCode);
					}
				});
		// 添加cookie:tk
		getDefaultHttpClient().getState().addCookie(//
				new Cookie("kyfw.12306.cn"// domain
						, "tk"// name
						, uamtkAuthResult.getNewapptk()//// value
						, "/otn"// path
						, DateUtils.addDays(new Date(), 20)// expires
						, false));
		// 设置cookie：uamtk过期
		getDefaultHttpClient().getState().addCookie(//
				new Cookie("kyfw.12306.cn"// domain
						, "uamtk"// name
						, "", // value
						"/passport"// path
						, new Date(System.currentTimeMillis() - 1000)// expires
						, false));
		doReq("https://kyfw.12306.cn/otn/uamauthclient?tk=" + uamtkAuthResult.getNewapptk(), "post", null,
				new HttpResponseHandler<String>() {
					@Override
					public String process(int responseCode, HttpMethod httpMethod) throws IOException {
						if (200 == responseCode) {
							String responseBodyAsString = httpMethod.getResponseBodyAsString();
							System.out.println(responseBodyAsString);
							return responseBodyAsString;
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
					if (responseBodyAsString.contains("modifyUserForm")) {
						final String REGEX_LOGINED_INFO = "class=\"info-item\".+class=\"label\">(.+):.+class=\"con\">(.+)<";
						Pattern pattern = Pattern.compile(REGEX_LOGINED_INFO);
						Matcher matcher = pattern.matcher(responseBodyAsString);
						while (matcher.find()) {
							System.out.println("----------->>>> " + matcher.group());
						}
						return true;
					}
				}
				return false;
			}
		});
	}

	public static void main(String[] args) throws Exception {
		System.out.println(checkLoginStatus());
	}

}
