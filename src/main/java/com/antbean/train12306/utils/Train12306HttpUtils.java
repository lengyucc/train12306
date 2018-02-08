package com.antbean.train12306.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.antbean.train12306.constants.TicketTypes;
import com.antbean.train12306.constants.Train12306Urls;
import com.antbean.train12306.entity.LoginedUserInfo;
import com.antbean.train12306.entity.TrainTicket;
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
	public static int writeCaptcha(File outFile) {
		OutputStream out = null;
		InputStream in = null;
		try {
			out = new FileOutputStream(outFile);
			in = (InputStream) doReq(Train12306Urls.GET_CAPTCHA_URL, "get", null, STREAM_HTTP_RESPONSE_HANDLER);
			return IOUtils.copy(in, out);
		} catch (IOException e) {
			throw new RuntimeException("获取验证码失败", e);
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
		}
	}

	public static void checkCaptcha(String captcha) {
		// 1.校验验证码
		doReq(Train12306Urls.CHECK_CAPTCHA_URL, "get", "answer=" + captcha + "&login_site=E&rand=sjrand",
				new HttpResponseHandler<String>() {
					@Override
					public String process(int responseCode, HttpMethod httpMethod) throws IOException {
						if (200 == responseCode) {
							String responseBodyAsString = httpMethod.getResponseBodyAsString();
							JSONObject jsonObject = JSONObject.parseObject(responseBodyAsString);
							if (!"4".equals(jsonObject.getString("result_code"))) {
								throw new RuntimeException(jsonObject.getString("result_message"));
							}
							return httpMethod.getResponseBodyAsString();
						}
						throw new RuntimeException("错误的状态码" + responseCode);
					}
				});
	}

	/**
	 * 登录到12306
	 */
	public static void login(String username, String password) {
		// 1.登录
		doReq(Train12306Urls.LOGIN_URL, "post", "username=" + username + "&password=" + password + "&appid=otn",
				new HttpResponseHandler<String>() {
					@Override
					public String process(int responseCode, HttpMethod httpMethod) throws IOException {
						if (200 == responseCode) {
							String responseBodyAsString = httpMethod.getResponseBodyAsString();
							JSONObject jsonObject = JSONObject.parseObject(responseBodyAsString);
							if (0 != jsonObject.getIntValue("result_code")) {
								throw new RuntimeException(jsonObject.getString("result_message"));
							}
							return responseBodyAsString;
						}
						throw new RuntimeException("错误的状态码" + responseCode);
					}
				});
		// 2.验证登录
		getDefaultHttpClient().getState().addCookie(new Cookie("kyfw.12306.cn", "current_captcha_type", "Z"));
		String newapptk = (String) doReq("https://kyfw.12306.cn/passport/web/auth/uamtk?appid=otn", "post", null,
				new HttpResponseHandler<String>() {
					@Override
					public String process(int responseCode, HttpMethod httpMethod) throws IOException {
						if (200 == responseCode) {
							String responseBodyAsString = httpMethod.getResponseBodyAsString();
							// {"result_message":"验证通过","result_code":0,"apptk":null,"newapptk":"Cz2EHtYAmjS5slF6SMP5pdvzlzbmro07mN7dW46t_e_ZF9VRty1210"}
							JSONObject jsonObject = JSONObject.parseObject(responseBodyAsString);
							if (0 != jsonObject.getIntValue("result_code")) {
								throw new RuntimeException(jsonObject.getString("result_message"));
							}
							return jsonObject.getString("newapptk");
						}
						throw new RuntimeException("错误的状态码" + responseCode);
					}
				});
		// 添加cookie:tk
		CookieUtils.addCookie("tk", newapptk, "/otn");
		// 设置cookie：uamtk过期
		CookieUtils.removeCookie("uamtk", "/passport");
		doReq("https://kyfw.12306.cn/otn/uamauthclient?tk=" + newapptk, "post", null,
				new HttpResponseHandler<String>() {
					@Override
					public String process(int responseCode, HttpMethod httpMethod) throws IOException {
						if (200 == responseCode) {
							String responseBodyAsString = httpMethod.getResponseBodyAsString();
							// {"apptk":"3hYbeze0RDb-XELynhG0uFo-awwsgpUZdEGVOIdWhTXwISp7rw1210","result_code":0,"result_message":"验证通过","username":"李明会"}
							JSONObject jsonObject = JSONObject.parseObject(responseBodyAsString);
							if (0 != jsonObject.getIntValue("result_code")) {
								throw new RuntimeException(jsonObject.getString("result_message"));
							}
							System.out.println("验证通过,当前登录用户:" + jsonObject.getString("username"));
							return responseBodyAsString;
						}
						throw new RuntimeException("错误的状态码" + responseCode);
					}
				});

	}

	/**
	 * 检查登录状态
	 */
	public static LoginedUserInfo checkLoginStatus() {
		return (LoginedUserInfo) doReq(Train12306Urls.QUERY_USER_INFO_URL, "get", null,
				new HttpResponseHandler<LoginedUserInfo>() {
					@Override
					public LoginedUserInfo process(int responseCode, HttpMethod httpMethod) throws IOException {
						if (200 == responseCode) {
							String responseBodyAsString = httpMethod.getResponseBodyAsString();
							if (responseBodyAsString.contains("modifyUserForm")) {
								LoginedUserInfo loginedUserInfo = LoginedUserInfoUtils
										.parseLoginedUserInfo(responseBodyAsString);
								Train12306Context.setLoginedUserInfo(loginedUserInfo);
								return loginedUserInfo;
							}
						}
						return null;
					}
				});
	}

	/**
	 * 查票
	 */
	public static List<TrainTicket> queryTickets(String trainDate, String fromStation, String toStation,
			String ticketType) {
		String queryString = "leftTicketDTO.train_date=" + trainDate + "&leftTicketDTO.from_station=" + fromStation
				+ "&leftTicketDTO.to_station=" + toStation + "&purpose_codes=" + ticketType;
		return (List<TrainTicket>) doReq(Train12306Urls.QUERY_TICKET_URL, "get", queryString,
				new HttpResponseHandler<List<TrainTicket>>() {
					@Override
					public List<TrainTicket> process(int responseCode, HttpMethod httpMethod) throws IOException {
						if (200 == responseCode) {
							String responseBodyAsString = httpMethod.getResponseBodyAsString();
							System.out.println(responseBodyAsString);
							JSONObject jsonObject = JSONObject.parseObject(responseBodyAsString);
							if (200 != jsonObject.getIntValue("httpstatus")) {
								throw new RuntimeException(jsonObject.getString("messages"));
							}
							String data = jsonObject.getString("data");
							List<TrainTicket> tickets = TicketUtils.parseTickets(data);
							System.out.println(tickets.size());
							return tickets;
						}
						return Collections.EMPTY_LIST;
					}
				});
	}

	public static void main(String[] args) throws Exception {
		List<TrainTicket> tickets = queryTickets("2018-02-21", "HGH", "FYH", TicketTypes.ADULT);
		System.out.println("车次\t出发时间\t到达时间\t历时\t高级软卧\t软卧\t软座\t无座\t硬卧\t硬座\t二等座\t一等座\t商务特等座\t是否可购票");
		for (TrainTicket tt : tickets) {
			System.out.println(tt.getF3() + "\t" + tt.getF8() + "\t" + tt.getF9() + "\t" + tt.getF10()//
					+ "\t" + (StringUtils.isBlank(tt.getF21()) ? "\t" : tt.getF21()) // 高级软卧
					+ "\t" + (StringUtils.isBlank(tt.getF23()) ? "\t" : tt.getF23()) // 软卧
					+ "\t" + (StringUtils.isBlank(tt.getF24()) ? "\t" : tt.getF24()) // 软座
					+ "\t" + (StringUtils.isBlank(tt.getF26()) ? "\t" : tt.getF26()) // 无座
					+ "\t" + (StringUtils.isBlank(tt.getF28()) ? "\t" : tt.getF28()) // 硬卧
					+ "\t" + (StringUtils.isBlank(tt.getF29()) ? "\t" : tt.getF29()) // 硬座
					+ "\t" + (StringUtils.isBlank(tt.getF30()) ? "\t" : tt.getF30()) // 二等座
					+ "\t" + (StringUtils.isBlank(tt.getF31()) ? "\t" : tt.getF31()) // 一等座
					+ "\t" + (StringUtils.isBlank(tt.getF32()) ? "\t" : tt.getF32()) // 商务特等座
					+ "\t" + (StringUtils.isBlank(tt.getF11()) ? "\t" : tt.getF11()) // 是否可购票
			);
		}
	}

}
