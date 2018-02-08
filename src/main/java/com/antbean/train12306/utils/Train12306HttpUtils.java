package com.antbean.train12306.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.antbean.train12306.Main;
import com.antbean.train12306.constants.TicketTypes;
import com.antbean.train12306.constants.Train12306Urls;
import com.antbean.train12306.entity.LoginedUserInfo;
import com.antbean.train12306.entity.Station;
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
		return (String) doReq(uri, method, STRING_HTTP_RESPONSE_HANDLER, queryString);
	}

	public static Object doReq(String uri, String method, HttpResponseHandler<?> handler, String queryString) {
		HttpMethod httpMethod = getHttpMethod(uri, method);
		httpMethod.setQueryString(queryString);
		return exeHttpMethod(httpMethod, handler);
	}

	public static Object doReq(String uri, String method, HttpResponseHandler<?> handler,
			NameValuePair... queryString) {
		HttpMethod httpMethod = getHttpMethod(uri, method);
		httpMethod.setQueryString(queryString);
		return exeHttpMethod(httpMethod, handler);
	}

	public static Object exeHttpMethod(HttpMethod httpMethod, HttpResponseHandler<?> handler) {
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

	public static HttpMethod getHttpMethod(String uri, String method) {
		HttpMethod httpMethod = null;
		if ("get".equalsIgnoreCase(method)) {
			httpMethod = new GetMethod(uri);
		} else if ("post".equalsIgnoreCase(method)) {
			httpMethod = new PostMethod(uri);
		} else {
			throw new IllegalArgumentException("无效的请求方式");
		}
		return httpMethod;
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
			in = (InputStream) doReq(Train12306Urls.GET_CAPTCHA_URL, "get", STREAM_HTTP_RESPONSE_HANDLER,
					StringUtils.EMPTY);
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
		doReq(Train12306Urls.CHECK_CAPTCHA_URL, "get", new HttpResponseHandler<String>() {
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
		}, "answer=" + captcha + "&login_site=E&rand=sjrand");
	}

	/**
	 * 登录到12306
	 */
	public static void login(String username, String password) {
		// 0.获取广告中携带的cookie
		doReq("https://ad.12306.cn/sdk/webservice/rest/appService/getAdAppInfo.json", "post",
				new HttpResponseHandler<String>() {
					@Override
					public String process(int responseCode, HttpMethod httpMethod) throws IOException {
						if (200 == responseCode) {
							String responseBodyAsString = httpMethod.getResponseBodyAsString();
							// {"code":"00","message":"获取成功","materialsList":[{"billId":"8d9a96ca58eb49bf9821793804b35a0d","billMaterialsId":"a12338c7642e40119147c8f86a05f3dc","filePath":"https://ad.12306.cn/res/delivery/0004/2018/02/05/201802051642209874.jpg","title":"中铁联名卡","placementPattern":"jpg","linkUri":"https://creditcard.bankcomm.com/content/dam/pc/activity/ztcard/zhongtieka4.html","marginBottom":"0","skipTime":"5000","linkType":"1","showSkipBtn":"0","btnhui":"https://ad.12306.cn/sdk/webservice/rest/appService/btnhui.json?placementNo=0004&billMaterialsId=a12338c7642e40119147c8f86a05f3dc","btnview":"https://ad.12306.cn/sdk/webservice/rest/appService/btnview.json?placementNo=0004&billMaterialsId=a12338c7642e40119147c8f86a05f3dc","thirdViewUrlList":[],"thirdClickUrlList":[],"skipTimeAgain":"0","fileList":[{"filePath":"https://ad.12306.cn/res/delivery/0004/2018/02/05/201802051642209874.jpg","fileHeight":"360","marginBottom":"0"}]}],"getType":"1","serviceUrl":"https://ad.12306.cn/sdk/webservice/rest/appService/getAdAppInfo.json","isDefault":"0","btnhui":"https://ad.12306.cn/sdk/webservice/rest/appService/btnhui.json"}
							JSONObject jsonObject = JSONObject.parseObject(responseBodyAsString);
							if (!"00".equals(jsonObject.getString("code"))) {
								throw new RuntimeException(jsonObject.getString("message"));
							}
							return responseBodyAsString;
						}
						throw new RuntimeException("错误的状态码" + responseCode);
					}
				}, "placementNo=0004&clientType=2&billMaterialsId=ac3398e4e6b7417789051de0894b4516");

		CookieUtils.addCookie(".12306.cn", "RAIL_DEVICEID",
				"i8p6-kWwc5_ZDKi4DnWIqb43vUmyuK-JdCnUJ6X3qAlupQP3740yvjKFm0kgQ8aV3jUCzwyprn9ci4aXNCb-a3AbViYIcZ0SWIShh8JVVCXTOsEI7LbwvfACqBAy-NK7QcpdN-Qnno4QlVtzMSrylYMfu81_lEdL",
				"/");
		CookieUtils.addCookie(".12306.cn", "RAIL_EXPIRATION", "1518425619075", "/");

		// 1.登录
		doReq(Train12306Urls.LOGIN_URL, "post", new HttpResponseHandler<String>() {
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
		}, "username=" + username + "&password=" + password + "&appid=otn");

		doReq(Train12306Urls.USER_LOGIN_URL, "post", new HttpResponseHandler<Boolean>() {
			@Override
			public Boolean process(int responseCode, HttpMethod httpMethod) throws IOException {
				if (302 == responseCode) {
					return true;
				}
				throw new RuntimeException("错误的状态码" + responseCode);
			}
		}, "_json_att=");

		// 2.验证登录
		getDefaultHttpClient().getState().addCookie(new Cookie("kyfw.12306.cn", "current_captcha_type", "Z"));
		String newapptk = (String) doReq(Train12306Urls.WEB_AUTH_URL, "post", new HttpResponseHandler<String>() {
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
		}, "appid=otn");
		// 添加cookie:tk
		CookieUtils.addCookie("tk", newapptk, "/otn");
		// 设置cookie：uamtk过期
		CookieUtils.removeCookie("uamtk", "/passport");
		doReq(Train12306Urls.UAM_AUTH_CLIENT_URL, "post", new HttpResponseHandler<String>() {
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
		}, "tk=" + newapptk);

	}

	/**
	 * 检查登录状态
	 */
	public static LoginedUserInfo checkLoginStatus() {
		return (LoginedUserInfo) doReq(Train12306Urls.QUERY_USER_INFO_URL, "get",
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
				}, StringUtils.EMPTY);
	}

	/**
	 * 查票
	 */
	@SuppressWarnings("unchecked")
	public static List<TrainTicket> queryTickets(String trainDate, String fromStation, String toStation,
			String ticketType) {
		String oF = fromStation;
		String oT = toStation;
		List<Station> fromStations = StationUtils.findStationsByKeywords(fromStation);
		List<Station> toStations = StationUtils.findStationsByKeywords(toStation);
		if (CollectionUtils.isEmpty(fromStations)) {
			throw new RuntimeException("出发站【" + fromStation + "】不存在");
		}
		if (CollectionUtils.isEmpty(toStations)) {
			throw new RuntimeException("目的站【" + fromStation + "】不存在");
		}
		fromStation = fromStations.get(0).getG3();
		toStation = toStations.get(0).getG3();

		// 设置一些cookie
		CookieUtils.addCookie("_jc_save_fromDate", trainDate);
		CookieUtils.addCookie("_jc_save_fromStation", oF + "," + fromStation);
		CookieUtils.addCookie("_jc_save_toDate", trainDate);
		CookieUtils.addCookie("_jc_save_toStation", oT + "," + toStation);
		CookieUtils.addCookie("_jc_save_wfdc_flag", "dc");

		String queryString = "leftTicketDTO.train_date=" + trainDate + "&leftTicketDTO.from_station=" + fromStation
				+ "&leftTicketDTO.to_station=" + toStation + "&purpose_codes=" + ticketType;
		return (List<TrainTicket>) doReq(Train12306Urls.QUERY_TICKET_URL, "get",
				new HttpResponseHandler<List<TrainTicket>>() {
					@Override
					public List<TrainTicket> process(int responseCode, HttpMethod httpMethod) throws IOException {
						if (200 == responseCode) {
							String responseBodyAsString = httpMethod.getResponseBodyAsString();
							System.out.println(Train12306Urls.QUERY_TICKET_URL + " >>>>>>>>>> " + responseBodyAsString);
							JSONObject jsonObject = JSONObject.parseObject(responseBodyAsString);
							if (200 != jsonObject.getIntValue("httpstatus")) {
								throw new RuntimeException(jsonObject.getString("messages"));
							}
							String data = jsonObject.getString("data");
							List<TrainTicket> tickets = TicketUtils.parseTickets(data);
							return tickets;
						}
						return Collections.EMPTY_LIST;
					}
				}, queryString);
	}

	public static void buyTickets(List<String> names, String trainDate, String fromStation, String toStation,
			List<String> trainNos, List<String> seatLevels) {

		// doReq("https://ad.12306.cn/sdk/webservice/rest/appService/getAdAppInfo.json",
		// "get",
		// new HttpResponseHandler<Boolean>() {
		// @Override
		// public Boolean process(int responseCode, HttpMethod httpMethod)
		// throws IOException {
		// if (200 == responseCode) {
		// String responseBodyAsString = httpMethod.getResponseBodyAsString();
		// System.out.println(responseBodyAsString);
		// return true;
		//
		// }
		// throw new RuntimeException("错误的状态码" + responseCode);
		// }
		// },
		// "placementNo=0004&clientType=2&billMaterialsId=a12338c7642e40119147c8f86a05f3dc");

		// 查票
		List<TrainTicket> tickets = queryTickets(trainDate, fromStation, toStation, TicketTypes.ADULT);
		if (CollectionUtils.isEmpty(tickets)) {
			throw new RuntimeException("没有查询到【" + fromStation + "】到【" + toStation + "】的车次！");
		}
		// 剔除不需要的车次
		Iterator<TrainTicket> iterator = tickets.iterator();
		while (iterator.hasNext()) {
			TrainTicket ticket = iterator.next();
			if (!trainNos.contains(ticket.getF3()) || !"Y".equalsIgnoreCase(ticket.getF11())) {
				// 不包含此车次或此车次无票
				iterator.remove();
			}
		}
		if (0 == tickets.size()) {
			throw new RuntimeException("你要预定的车次无票");
		}
		// 根据车次优先级排序
		for (int i = 0; i < trainNos.size(); i++) {
			String tNo = trainNos.get(i);
			int index = -1;
			for (int j = 0; j < tickets.size(); j++) {
				TrainTicket trainTicket = tickets.get(j);
				if (tNo.equals(trainTicket.getF3())) {
					index = j;
					break;
				}
			}
			TrainTicket tt = tickets.remove(index);
			tickets.add(i, tt);
		}
		System.out.println(tickets);
		//
		for (TrainTicket trainTicket : tickets) {
			// 点击预定
			// 检查用户
			boolean checkUser = (boolean) doReq(Train12306Urls.CHECK_USER_URL, "post",
					new HttpResponseHandler<Boolean>() {
						@Override
						public Boolean process(int responseCode, HttpMethod httpMethod) throws IOException {
							if (200 == responseCode) {
								String responseBodyAsString = httpMethod.getResponseBodyAsString();
								System.out.println(responseBodyAsString);
								// 已登录
								// {"validateMessagesShowId":"_validatorMessage","status":true,"httpstatus":200,"data":{"flag":true},"messages":[],"validateMessages":{}}
								// 未登录
								// {"validateMessagesShowId":"_validatorMessage","status":true,"httpstatus":200,"data":{"flag":false},"messages":[],"validateMessages":{}}
								return JSONObject.parseObject(responseBodyAsString).getJSONObject("data")
										.getBooleanValue("flag");

							}
							throw new RuntimeException("错误的状态码" + responseCode);
						}
					}, "_json_att=");
			if (!checkUser)
				throw new RuntimeException("用户未登录，无法预定车票!");
			// 提交订单请求

			String secretStr = null;
			try {
				secretStr = URLDecoder.decode(trainTicket.getF0(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}

			doReq(Train12306Urls.SUBMIT_ORDER_REQUEST_URL, "post", new HttpResponseHandler<Boolean>() {
				@Override
				public Boolean process(int responseCode, HttpMethod httpMethod) throws IOException {
					if (200 == responseCode) {
						String responseBodyAsString = httpMethod.getResponseBodyAsString();
						System.out.println(responseBodyAsString);
						// 失败
						// {"validateMessagesShowId":"_validatorMessage","status":false,"httpstatus":200,"messages":["提交失败，请重试..."],"validateMessages":{}}
						// 成功
						// {"validateMessagesShowId":"_validatorMessage","status":true,"httpstatus":200,"data":"N","messages":[],"validateMessages":{}}
						JSONObject jsonObject = JSONObject.parseObject(responseBodyAsString);
						if (!jsonObject.getBooleanValue("status"))
							throw new RuntimeException(jsonObject.getString("messages"));
						return true;

					}
					throw new RuntimeException("错误的状态码" + responseCode);
				}
			}, new NameValuePair[] { //
					new NameValuePair("secretStr", secretStr) //
					, new NameValuePair("train_date", trainDate) //
					, new NameValuePair("back_train_date", trainDate) //
					, new NameValuePair("tour_flag", "dc") //
					, new NameValuePair("purpose_codes", TicketTypes.ADULT) //
					, new NameValuePair("query_from_station_name", fromStation) //
					, new NameValuePair("query_to_station_name", toStation) //
					, new NameValuePair("undefined", "") //
			});

			break;
		}
	}

	public static void main(String[] args) throws Exception {
		Main.do11();
		TimeUnit.SECONDS.sleep(3);
		buyTickets(null, "2018-02-09", "杭州", "上海", Arrays.asList("K80", "K528", "Z248", "K150"), null);
	}

}
