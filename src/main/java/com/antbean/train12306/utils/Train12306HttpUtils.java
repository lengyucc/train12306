package com.antbean.train12306.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.antbean.train12306.Main;
import com.antbean.train12306.constants.TicketTypes;
import com.antbean.train12306.constants.Train12306Urls;
import com.antbean.train12306.entity.LoginedUserInfo;
import com.antbean.train12306.entity.Passenger;
import com.antbean.train12306.entity.SeatLevel;
import com.antbean.train12306.entity.Station;
import com.antbean.train12306.entity.TrainTicket;
import com.antbean.train12306.handler.HttpResponseHandler;
import com.antbean.train12306.handler.impl.StreamHttpResponseHandler;
import com.antbean.train12306.handler.impl.StringHttpResponseHandler;

public class Train12306HttpUtils {
	private static final Logger LOG = Logger.getLogger(Train12306HttpUtils.class);
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
		LOG.info("doReq, uri:" + uri + ", method:" + queryString + ", queryString:" + Arrays.toString(queryString));
		HttpMethod httpMethod = getHttpMethod(uri, method);
		httpMethod.setQueryString(queryString);
		Object res = exeHttpMethod(httpMethod, handler);
		LOG.info("result:" + res);
		return res;
	}

	public static Object exeHttpMethod(HttpMethod httpMethod, HttpResponseHandler<?> handler) {
		try {
			HttpClient client = getDefaultHttpClient();
			int responseCode = client.executeMethod(httpMethod);
			showCookies();
			return handler.process(responseCode, httpMethod);
		} catch (HttpException e) {
			LOG.error("http exception", e);
			throw new ServiceException("网络异常", e);
		} catch (IOException e) {
			LOG.error("io exception", e);
			throw new ServiceException("IO异常", e);
		}
	}

	public static HttpMethod getHttpMethod(String uri, String method) {
		HttpMethod httpMethod = null;
		if ("get".equalsIgnoreCase(method)) {
			httpMethod = new GetMethod(uri);
		} else if ("post".equalsIgnoreCase(method)) {
			httpMethod = new PostMethod(uri);
		} else {
			LOG.info("request method error, method=" + method);
			throw new ServiceException("无效的请求方式, method:" + method);
		}
		return httpMethod;
	}

	public static void showCookies() {
		HttpClient client = getDefaultHttpClient();
		Cookie[] cookies = client.getState().getCookies();
		for (Cookie cookie : cookies) {
			LOG.info("#### cookieName:" + cookie.getName() + ", cookieValue:" + cookie.getValue());
		}
	}

	/**
	 * 获取验证码
	 */
	public static int writeCaptcha(File outFile) {
		LOG.info("write captcha to " + outFile.getAbsolutePath());
		OutputStream out = null;
		InputStream in = null;
		try {
			out = new FileOutputStream(outFile);
			in = (InputStream) doReq(Train12306Urls.GET_CAPTCHA_URL, "get", STREAM_HTTP_RESPONSE_HANDLER,
					StringUtils.EMPTY);
			return IOUtils.copy(in, out);
		} catch (IOException e) {
			LOG.error("write captcha error", e);
			throw new ServiceException("获取验证码失败");
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * 校验验证码
	 */
	public static void checkCaptcha(String captcha) {
		LOG.info("check captcha, captcha:" + captcha);
		doReq(Train12306Urls.CHECK_CAPTCHA_URL, "get", new HttpResponseHandler<String>() {
			@Override
			public String process(int responseCode, HttpMethod httpMethod) throws IOException {
				if (200 == responseCode) {
					String responseBodyAsString = httpMethod.getResponseBodyAsString();
					JSONObject jsonObject = JsonUtils.parseObject(responseBodyAsString);
					if (!"4".equals(jsonObject.getString("result_code"))) {
						LOG.info("check captcha, result: success");
						throw new ServiceException(jsonObject.getString("result_message"));
					}
					LOG.info("check captcha, result: success");
					return httpMethod.getResponseBodyAsString();
				}
				LOG.error("the response code is not expected, " + responseCode);
				throw new ServiceException("错误的状态码" + responseCode);
			}
		}, "answer=" + captcha + "&login_site=E&rand=sjrand");
	}

	/**
	 * 登录到12306
	 */
	public static void login(String username, String password) {
		LOG.info("login 12306, username: username");
		// 1.登录
		LOG.info("login step 1 entering...");
		doReq(Train12306Urls.LOGIN_URL, "post", new HttpResponseHandler<String>() {
			@Override
			public String process(int responseCode, HttpMethod httpMethod) throws IOException {
				if (200 == responseCode) {
					String responseBodyAsString = httpMethod.getResponseBodyAsString();
					JSONObject jsonObject = JsonUtils.parseObject(responseBodyAsString);
					if (0 != jsonObject.getIntValue("result_code")) {
						LOG.info("check captcha, result: success");
						throw new ServiceException(jsonObject.getString("result_message"));
					}
					LOG.info("login step 1 pass.");
					return responseBodyAsString;
				}
				LOG.error("the response code is not expected, " + responseCode);
				throw new ServiceException("错误的状态码" + responseCode);
			}
		}, "username=" + username + "&password=" + password + "&appid=otn");
		// 2.验证登录
		LOG.info("login step 2 entering...");
		getDefaultHttpClient().getState().addCookie(new Cookie("kyfw.12306.cn", "current_captcha_type", "Z"));
		String newapptk = (String) doReq(Train12306Urls.WEB_AUTH_URL, "post", new HttpResponseHandler<String>() {
			@Override
			public String process(int responseCode, HttpMethod httpMethod) throws IOException {
				if (200 == responseCode) {
					String responseBodyAsString = httpMethod.getResponseBodyAsString();
					// {"result_message":"验证通过","result_code":0,"apptk":null,"newapptk":"Cz2EHtYAmjS5slF6SMP5pdvzlzbmro07mN7dW46t_e_ZF9VRty1210"}
					JSONObject jsonObject = JsonUtils.parseObject(responseBodyAsString);
					if (0 != jsonObject.getIntValue("result_code")) {
						throw new RuntimeException(jsonObject.getString("result_message"));
					}
					LOG.info("login step 2 pass.");
					return jsonObject.getString("newapptk");
				}
				LOG.error("the response code is not expected, " + responseCode);
				throw new ServiceException("错误的状态码" + responseCode);
			}
		}, "appid=otn");

		// 3.
		LOG.info("login step 3 entering...");
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
						throw new ServiceException(jsonObject.getString("result_message"));
					}
					LOG.info("login step 3 pass.");
					LOG.info("login success. curr username: " + jsonObject.getString("username"));
					return responseBodyAsString;
				}
				LOG.error("the response code is not expected, " + responseCode);
				throw new ServiceException("错误的状态码" + responseCode);
			}
		}, "tk=" + newapptk);
	}

	/**
	 * 检查登录状态
	 */
	public static LoginedUserInfo checkLoginStatus() {
		LOG.info("check login status");
		return (LoginedUserInfo) doReq(Train12306Urls.QUERY_USER_INFO_URL, "get",
				new HttpResponseHandler<LoginedUserInfo>() {
					@Override
					public LoginedUserInfo process(int responseCode, HttpMethod httpMethod) throws IOException {
						if (200 == responseCode) {
							String responseBodyAsString = httpMethod.getResponseBodyAsString();
							if (responseBodyAsString.contains("modifyUserForm")) {
								LoginedUserInfo loginedUserInfo = LoginedUserInfoUtils
										.parseLoginedUserInfo(responseBodyAsString);
								LOG.info("curr login user info:" + loginedUserInfo);
								return loginedUserInfo;
							}
						}
						LOG.error("the response code is not expected, " + responseCode);
						throw new ServiceException("错误的状态码" + responseCode);
					}
				}, StringUtils.EMPTY);
	}

	/**
	 * 查票
	 */
	@SuppressWarnings("unchecked")
	public static List<TrainTicket> queryTickets(String trainDate, String fromStation, String toStation,
			String ticketType) {
		LOG.info("query tickets, trainDate:" + trainDate + ", fromStation:" + fromStation + ", toStation:" + toStation
				+ ", ticketType:" + ticketType);
		String oF = fromStation;
		String oT = toStation;
		List<Station> fromStations = StationUtils.findStationsByKeywords(fromStation);
		List<Station> toStations = StationUtils.findStationsByKeywords(toStation);
		if (CollectionUtils.isEmpty(fromStations)) {
			LOG.error("from station is not exists");
			throw new ServiceException("出发站【" + fromStation + "】不存在");
		}
		if (CollectionUtils.isEmpty(toStations)) {
			LOG.error("to station is not exists");
			throw new ServiceException("目的站【" + fromStation + "】不存在");
		}
		fromStation = fromStations.get(0).getG3();
		toStation = toStations.get(0).getG3();
		LOG.info("fromStation:" + fromStation + ", toStation:" + toStation);

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
							JSONObject jsonObject = JsonUtils.parseObject(responseBodyAsString);
							if (200 != jsonObject.getIntValue("httpstatus")) {
								throw new ServiceException(jsonObject.getString("messages"));
							}
							String data = jsonObject.getString("data");
							List<TrainTicket> tickets = TicketUtils.parseTickets(data);
							LOG.info("query result:" + tickets);
							return tickets;
						}
						LOG.error("the response code is not expected, " + responseCode);
						throw new ServiceException("错误的状态码" + responseCode);
					}
				}, queryString);
	}

	public static void buyTickets(List<String> names, String trainDate, String fromStation, String toStation,
			List<String> trainNos, List<String> seatLevels) {
		LOG.info("buy tickets. names:" + names + ", trainDate:" + trainDate + ", fromStation:" + fromStation
				+ ", toStation:" + toStation + ", trainNos:" + trainNos + ", seatLevels:" + seatLevels);
		// 查票
		List<TrainTicket> tickets = queryTickets(trainDate, fromStation, toStation, TicketTypes.ADULT);
		if (CollectionUtils.isEmpty(tickets))
			throw new ServiceException("没有查询到【" + fromStation + "】到【" + toStation + "】的车次！");
		// 剔除不需要的车次
		Iterator<TrainTicket> iterator = tickets.iterator();
		while (iterator.hasNext()) {
			TrainTicket ticket = iterator.next();
			if (!trainNos.contains(ticket.getF3()) || !"Y".equalsIgnoreCase(ticket.getF11())) {
				// 不包含此车次或此车次无票
				iterator.remove();
			}
		}
		if (0 == tickets.size())
			throw new ServiceException("你要预定的车次无票");
		// 剔除想要但没有的车次
		Iterator<String> tnIterator = trainNos.iterator();
		while (tnIterator.hasNext()) {
			String tn = tnIterator.next();
			boolean exists = false;
			for (TrainTicket trainTicket : tickets) {
				if (trainTicket.getF3().equals(tn)) {
					exists = true;
					break;
				}
			}
			if (!exists)
				tnIterator.remove();
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
		LOG.info("sorted tickets:" + tickets);
		//
		for (TrainTicket trainTicket : tickets) {
			// 点击预定
			// 检查用户
			LOG.info("check user...");
			boolean checkUser = (boolean) doReq(Train12306Urls.CHECK_USER_URL, "post",
					new HttpResponseHandler<Boolean>() {
						@Override
						public Boolean process(int responseCode, HttpMethod httpMethod) throws IOException {
							if (200 == responseCode) {
								String responseBodyAsString = httpMethod.getResponseBodyAsString();
								// 已登录
								// {"validateMessagesShowId":"_validatorMessage","status":true,"httpstatus":200,"data":{"flag":true},"messages":[],"validateMessages":{}}
								// 未登录
								// {"validateMessagesShowId":"_validatorMessage","status":true,"httpstatus":200,"data":{"flag":false},"messages":[],"validateMessages":{}}
								return JsonUtils.parseObject(responseBodyAsString).getJSONObject("data")
										.getBooleanValue("flag");
							}
							LOG.error("the response code is not expected, " + responseCode);
							throw new ServiceException("错误的状态码" + responseCode);
						}
					}, "_json_att=");
			if (!checkUser) {
				LOG.error("no user logined");
				throw new ServiceException("用户未登录，无法预定车票!");
			}
			LOG.info("user logined!");
			// 提交订单请求
			LOG.info("sumit order request...");
			String secretStr = null;
			try {
				secretStr = URLDecoder.decode(trainTicket.getF0(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				LOG.error("decode secret error", e);
				throw new ServiceException("解码secret错误");
			}
			doReq(Train12306Urls.SUBMIT_ORDER_REQUEST_URL, "post", new HttpResponseHandler<Boolean>() {
				@Override
				public Boolean process(int responseCode, HttpMethod httpMethod) throws IOException {
					if (200 == responseCode) {
						String responseBodyAsString = httpMethod.getResponseBodyAsString();
						// 失败
						// {"validateMessagesShowId":"_validatorMessage","status":false,"httpstatus":200,"messages":["提交失败，请重试..."],"validateMessages":{}}
						// 成功
						// {"validateMessagesShowId":"_validatorMessage","status":true,"httpstatus":200,"data":"N","messages":[],"validateMessages":{}}
						JSONObject jsonObject = JsonUtils.parseObject(responseBodyAsString);
						if (!jsonObject.getBooleanValue("status"))
							throw new ServiceException(jsonObject.getString("messages"));
						return true;
					}
					LOG.error("the response code is not expected, " + responseCode);
					throw new ServiceException("错误的状态码" + responseCode);
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
			LOG.info("sumit order request success!");
			// 到提交订单页
			LOG.info("to init order page...");
			String initDocHtml = (String) doReq(Train12306Urls.INIT_DOC_URL, "get", new HttpResponseHandler<String>() {
				@Override
				public String process(int responseCode, HttpMethod httpMethod) throws IOException {
					if (200 == responseCode) {
						String responseBodyAsString = httpMethod.getResponseBodyAsString();
						return responseBodyAsString;

					}
					LOG.error("the response code is not expected, " + responseCode);
					throw new ServiceException("错误的状态码" + responseCode);
				}
			}, "_json_att=");

			// 获取REPEAT_SUBMIT_TOKEN
			String repeatSubmitToken = null;
			Pattern pattern = Pattern.compile("var\\s+globalRepeatSubmitToken\\s*=\\s*'(.+)'");
			Matcher matcher = pattern.matcher(initDocHtml);
			if (matcher.find()) {
				repeatSubmitToken = matcher.group(1);
			} else {
				throw new ServiceException("获取REPEAT_SUBMIT_TOKEN失败");
			}

			// 获取leftTicket、fromStationTelecode、purpose_codes
			String leftTicket = null;
			String fromStationTelecode = null;
			String toStationTelecode = null;
			String purposeCodes = null;
			String trainLocation = null;
			String trainNo = null;
			String keyCheckIsChange = null;
			pattern = Pattern.compile("var\\s+ticketInfoForPassengerForm\\s*=\\s*(\\{.*\\})");
			matcher = pattern.matcher(initDocHtml);
			if (matcher.find()) {
				String jsonString = matcher.group(1);
				JSONObject jsonObject = JSONObject.parseObject(jsonString);
				leftTicket = jsonObject.getString("leftTicketStr");
				trainLocation = jsonObject.getString("train_location");
				fromStationTelecode = jsonObject.getJSONObject("queryLeftNewDetailDTO")
						.getString("from_station_telecode");
				toStationTelecode = jsonObject.getJSONObject("queryLeftNewDetailDTO").getString("to_station_telecode");
				purposeCodes = jsonObject.getJSONObject("queryLeftTicketRequestDTO").getString("purpose_codes");
				trainNo = jsonObject.getJSONObject("queryLeftTicketRequestDTO").getString("train_no");
				keyCheckIsChange = jsonObject.getString("key_check_isChange");
			} else {
				throw new ServiceException("获取ticketInfoForPassengerForm信息失败");
			}

			// 获取有票座位类型
			String tourFlag = null; //
			List<SeatLevel> sLevels = null;
			pattern = Pattern.compile("var\\s+ticketInfoForPassengerForm\\s*=\\s*(\\{.*\\})");
			matcher = pattern.matcher(initDocHtml);
			if (matcher.find()) {
				String jsonString = matcher.group(1);
				sLevels = new ArrayList<>();
				JSONObject jsonObject = JsonUtils.parseObject(jsonString);
				tourFlag = jsonObject.getString("tour_flag");
				JSONArray seatTypeCodes = jsonObject.getJSONObject("limitBuySeatTicketDTO")
						.getJSONArray("seat_type_codes");
				for (int i = 0; i < seatTypeCodes.size(); i++) {
					JSONObject seatTypeCode = seatTypeCodes.getJSONObject(i);
					String id = seatTypeCode.getString("id");
					String value = seatTypeCode.getString("value");
					sLevels.add(new SeatLevel(id, value));
				}
			}
			if (CollectionUtils.isEmpty(sLevels)) {
				LOG.info("车次【" + trainTicket.getF3() + "】已无票");
				continue;
			}
			// 移除不要的座位类型
			Iterator<SeatLevel> slIterator = sLevels.iterator();
			while (slIterator.hasNext()) {
				SeatLevel seatLevel = slIterator.next();
				if (!seatLevels.contains(seatLevel.getLevel()))
					slIterator.remove();
			}
			// 剔除想要但没有的座席
			Iterator<String> slItr = seatLevels.iterator();
			while (slItr.hasNext()) {
				String sl = slItr.next();
				boolean exists = false;
				for (SeatLevel seatLevel : sLevels) {
					if (seatLevel.getLevel().equals(sl)) {
						exists = true;
						break;
					}
				}
				if (!exists)
					slItr.remove();
			}
			if (0 == sLevels.size())
				throw new ServiceException("订购的座席已无票");
			// 根据座位类型优先级排序
			for (int i = 0; i < seatLevels.size(); i++) {
				String sl = seatLevels.get(i);
				int index = -1;
				for (int j = 0; j < sLevels.size(); j++) {
					SeatLevel seatLevel = sLevels.get(j);
					if (sl.equals(seatLevel.getLevel())) {
						index = j;
						break;
					}
				}
				SeatLevel tt = sLevels.remove(index);
				sLevels.add(i, tt);
			}
			LOG.info("sorted seat levels:" + sLevels);
			// 获取乘客信息
			LOG.info("get passengers...");
			@SuppressWarnings("unchecked")
			List<Passenger> passengers = (List<Passenger>) doReq(Train12306Urls.GET_PASSENGER_URL, "get",
					new HttpResponseHandler<List<Passenger>>() {
						@Override
						public List<Passenger> process(int responseCode, HttpMethod httpMethod) throws IOException {
							if (200 == responseCode) {
								String responseBodyAsString = httpMethod.getResponseBodyAsString();
								// {"validateMessagesShowId":"_validatorMessage","status":true,"httpstatus":200,"data":{"isExist":true,"exMsg":"","two_isOpenClick":["93","95","97","99"],"other_isOpenClick":["91","93","98","99","95","97"],"normal_passengers":[{"code":"5","passenger_name":"李明会","sex_code":"","born_date":"2017-08-31
								// 00:00:00","country_code":"CN","passenger_id_type_code":"1","passenger_id_type_name":"二代身份证","passenger_id_no":"412723199302105935","passenger_type":"1","passenger_flag":"0","passenger_type_name":"成人","mobile_no":"","phone_no":"","email":"","address":"","postalcode":"","first_letter":"LMH","recordCount":"10","total_times":"99","index_id":"0"},{"code":"1","passenger_name":"高亚芳","sex_code":"F","sex_name":"女","born_date":"1992-08-04
								// 00:00:00","country_code":"CN","passenger_id_type_code":"1","passenger_id_type_name":"二代身份证","passenger_id_no":"412723199208045542","passenger_type":"1","passenger_flag":"0","passenger_type_name":"成人","mobile_no":"13673571036","phone_no":"","email":"","address":"","postalcode":"","first_letter":"GYF","recordCount":"10","total_times":"99","index_id":"1"},{"code":"2","passenger_name":"郭正龙","sex_code":"","born_date":"1900-01-01
								// 00:00:00","country_code":"CN","passenger_id_type_code":"1","passenger_id_type_name":"二代身份证","passenger_id_no":"421182199201141337","passenger_type":"1","passenger_flag":"0","passenger_type_name":"成人","mobile_no":"","phone_no":"","email":"","address":"","postalcode":"","first_letter":"GZL","recordCount":"10","total_times":"99","index_id":"2"},{"code":"4","passenger_name":"李揪","sex_code":"","born_date":"2017-02-03
								// 00:00:00","country_code":"CN","passenger_id_type_code":"1","passenger_id_type_name":"二代身份证","passenger_id_no":"412723197408195933","passenger_type":"1","passenger_flag":"0","passenger_type_name":"成人","mobile_no":"","phone_no":"","email":"","address":"","postalcode":"","first_letter":"LJ","recordCount":"10","total_times":"99","index_id":"3"},{"code":"3","passenger_name":"李明明","sex_code":"","born_date":"2016-11-26
								// 00:00:00","country_code":"CN","passenger_id_type_code":"1","passenger_id_type_name":"二代身份证","passenger_id_no":"412723199803125977","passenger_type":"1","passenger_flag":"0","passenger_type_name":"成人","mobile_no":"","phone_no":"","email":"","address":"","postalcode":"","first_letter":"LMM","recordCount":"10","total_times":"99","index_id":"4"},{"code":"6","passenger_name":"史棉","sex_code":"","born_date":"2017-04-29
								// 00:00:00","country_code":"CN","passenger_id_type_code":"1","passenger_id_type_name":"二代身份证","passenger_id_no":"412723197007255923","passenger_type":"1","passenger_flag":"0","passenger_type_name":"成人","mobile_no":"","phone_no":"","email":"","address":"","postalcode":"","first_letter":"SM","recordCount":"10","total_times":"99","index_id":"5"},{"code":"7","passenger_name":"王合修","sex_code":"","born_date":"2017-02-03
								// 00:00:00","country_code":"CN","passenger_id_type_code":"1","passenger_id_type_name":"二代身份证","passenger_id_no":"412723197212105975","passenger_type":"1","passenger_flag":"0","passenger_type_name":"成人","mobile_no":"","phone_no":"","email":"","address":"","postalcode":"","first_letter":"WHX","recordCount":"10","total_times":"99","index_id":"6"},{"code":"8","passenger_name":"王全力","sex_code":"","born_date":"2017-02-03
								// 00:00:00","country_code":"CN","passenger_id_type_code":"1","passenger_id_type_name":"二代身份证","passenger_id_no":"412723197011245939","passenger_type":"1","passenger_flag":"0","passenger_type_name":"成人","mobile_no":"","phone_no":"","email":"","address":"","postalcode":"","first_letter":"WQL","recordCount":"10","total_times":"99","index_id":"7"},{"code":"9","passenger_name":"吴英杰","sex_code":"","born_date":"1900-01-01
								// 00:00:00","country_code":"CN","passenger_id_type_code":"1","passenger_id_type_name":"二代身份证","passenger_id_no":"412723199301075914","passenger_type":"1","passenger_flag":"0","passenger_type_name":"成人","mobile_no":"","phone_no":"","email":"","address":"","postalcode":"","first_letter":"WYJ","recordCount":"10","total_times":"99","index_id":"8"},{"code":"10","passenger_name":"朱佰厂","sex_code":"","born_date":"2017-05-06
								// 00:00:00","country_code":"CN","passenger_id_type_code":"1","passenger_id_type_name":"二代身份证","passenger_id_no":"412723197408205978","passenger_type":"1","passenger_flag":"0","passenger_type_name":"成人","mobile_no":"","phone_no":"","email":"","address":"","postalcode":"","first_letter":"ZBC","recordCount":"10","total_times":"99","index_id":"9"}],"dj_passengers":[]},"messages":[],"validateMessages":{}}
								JSONObject jsonObject = JsonUtils.parseObject(responseBodyAsString);
								if (!jsonObject.getBooleanValue("status")
										|| !jsonObject.getJSONObject("data").getBooleanValue("isExist"))
									throw new ServiceException("获取乘客信息失败");
								return jsonObject.getJSONObject("data").getJSONArray("normal_passengers")
										.toJavaList(Passenger.class);

							}
							LOG.error("the response code is not expected, " + responseCode);
							throw new ServiceException("错误的状态码" + responseCode);
						}
					}, "_json_att=");
			LOG.info("passengers:" + passengers);
			// 剔除不需要的乘客
			Iterator<Passenger> pItr = passengers.iterator();
			while (pItr.hasNext()) {
				Passenger passenger = pItr.next();
				if (!names.contains(passenger.getPassenger_name()))
					pItr.remove();
			}
			LOG.info("buy ticket for passengers:" + passengers);

			for (SeatLevel seatLevel : sLevels) {
				// 在这里尝试购买车票，比如分别尝试硬座、卧铺、无座等

				LOG.info(">>> 【" + trainTicket.getF3() + "】【" + seatLevel.getName() + "】");

				String sl = seatLevel.getLevel();
				String ticketType = "1"; // 票类型：成人票1，儿童票2，学生票3，残疾票4
				String cardType = "1"; // 证件类型：二代身份证1，港澳通行证C，台湾通行证G，护照B
				// 拼接check order info的请求参数
				String oldPassengerStr = "";
				String passengerTicketStr = "";
				for (Passenger passenger : passengers) {
					// 座位编号,0,票类型,乘客名,证件类型,证件号,手机号码,保存常用联系人(Y或N)
					passengerTicketStr += sl + ",0," + ticketType + "," + passenger.getPassenger_name() + "," + cardType
							+ "," + passenger.getPassenger_id_no() + "," + passenger.getMobile_no() + ",N_";
					// 乘客名,证件类型,证件号,乘客类型
					oldPassengerStr += passenger.getPassenger_name() + "," + cardType + ","
							+ passenger.getPassenger_id_no() + "," + ticketType + "_";
				}
				passengerTicketStr = passengerTicketStr.substring(0, passengerTicketStr.length() - 1);
				try {
					// 检查订单信息
					doReq(Train12306Urls.CHECK_ORDER_INFO_URL, "post", new HttpResponseHandler<String>() {
						@Override
						public String process(int responseCode, HttpMethod httpMethod) throws IOException {
							if (200 == responseCode) {
								String responseBodyAsString = httpMethod.getResponseBodyAsString();
								// 正确{"validateMessagesShowId":"_validatorMessage","status":true,"httpstatus":200,"data":{"ifShowPassCode":"N","canChooseBeds":"N","canChooseSeats":"N","choose_Seats":"MOP9","isCanChooseMid":"N","ifShowPassCodeTime":"1","submitStatus":true,"smokeStr":""},"messages":[],"validateMessages":{}}
								// 错误{"validateMessagesShowId":"_validatorMessage","status":true,"httpstatus":200,"data":{"errMsg":"系统繁忙，请稍后重试！","submitStatus":false},"messages":[],"validateMessages":{}}
								JSONObject jsonObject = JsonUtils.parseObject(responseBodyAsString);
								if (!jsonObject.getBooleanValue("status")) {
									throw new ServiceException(jsonObject.getString("messages"));
								}
								if (!jsonObject.getJSONObject("data").getBooleanValue("submitStatus")) {
									throw new ServiceException(jsonObject.getJSONObject("data").getString("errMsg"));
								}
								return responseBodyAsString;

							}
							LOG.error("the response code is not expected, " + responseCode);
							throw new ServiceException("错误的状态码" + responseCode);
						}
					}, new NameValuePair[] { //
							new NameValuePair("_json_att", StringUtils.EMPTY) //
							, new NameValuePair("bed_level_order_num", "000000000000000000000000000000") //
							, new NameValuePair("cancel_flag", "2") //
							, new NameValuePair("oldPassengerStr", oldPassengerStr) //
							, new NameValuePair("passengerTicketStr", passengerTicketStr) //
							, new NameValuePair("randCode", StringUtils.EMPTY) //
							, new NameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken) //
							, new NameValuePair("tour_flag", tourFlag) //
							, new NameValuePair("whatsSelect", "1") //
					});
					// 查询剩余票数
					int ticketCount = (int) doReq(Train12306Urls.GET_ORDER_QUEUE_COUNT_URL, "post",
							new HttpResponseHandler<Integer>() {
								@Override
								public Integer process(int responseCode, HttpMethod httpMethod) throws IOException {
									if (200 == responseCode) {
										String responseBodyAsString = httpMethod.getResponseBodyAsString();
										// 正确{"validateMessagesShowId":"_validatorMessage","status":true,"httpstatus":200,"data":{"count":"0","ticket":"689","op_2":"false","countT":"0","op_1":"false"},"messages":[],"validateMessages":{}}
										// 错误{"validateMessagesShowId":"_validatorMessage","url":"/leftTicket/init","status":false,"httpstatus":200,"messages":["系统忙，请稍后重试"],"validateMessages":{}}
										JSONObject jsonObject = JsonUtils.parseObject(responseBodyAsString);
										if (!jsonObject.getBooleanValue("status")) {
											throw new ServiceException(jsonObject.getString("messages"));
										}
										return jsonObject.getJSONObject("data").getInteger("ticket");

									}
									LOG.error("the response code is not expected, " + responseCode);
									throw new ServiceException("错误的状态码" + responseCode);
								}
								// train_date=Sat+Feb+10+2018+00%3A00%3A00+GMT%2B0800&train_no=57000Z438205&stationTrainCode=Z4382
								// &seatType=3&fromStationTelecode=HGH&toStationTelecode=SNH
								// &leftTicket=jrgGYjN3HaXuNluS%252Fe%252FmC4R%252BJtBuCq%252BA%252BbFpZjMk8XV2Ich0%252F82ZT6x7rEs%253D
								// &purpose_codes=00&train_location=G2&_json_att=&REPEAT_SUBMIT_TOKEN=1a6fb71819f76bf63c89038aa56246e0
							},
							new NameValuePair[] { //
									new NameValuePair("train_date", Train12306DateUtils.format(trainDate)) //
									, new NameValuePair("train_no", trainNo) //
									, new NameValuePair("stationTrainCode", trainTicket.getF3()) //
									, new NameValuePair("seatType", sl) //
									, new NameValuePair("fromStationTelecode", fromStationTelecode) //
									, new NameValuePair("toStationTelecode", toStationTelecode) //
									, new NameValuePair("leftTicket", leftTicket) //
									, new NameValuePair("purpose_codes", purposeCodes) //
									, new NameValuePair("train_location", trainLocation) //
									, new NameValuePair("_json_att", StringUtils.EMPTY) //
									, new NameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken) //
							});
					doReq(Train12306Urls.CONFIRM_SINGLE_FOR_QUEUE_URL, "post", new HttpResponseHandler<Integer>() {
						@Override
						public Integer process(int responseCode, HttpMethod httpMethod) throws IOException {
							if (200 == responseCode) {
								String responseBodyAsString = httpMethod.getResponseBodyAsString();
								// 正确{"validateMessagesShowId":"_validatorMessage","status":true,"httpstatus":200,"data":{"count":"0","ticket":"689","op_2":"false","countT":"0","op_1":"false"},"messages":[],"validateMessages":{}}
								// 错误{"validateMessagesShowId":"_validatorMessage","url":"/leftTicket/init","status":false,"httpstatus":200,"messages":["系统忙，请稍后重试"],"validateMessages":{}}
								JSONObject jsonObject = JsonUtils.parseObject(responseBodyAsString);
								if (!jsonObject.getBooleanValue("status")) {
									throw new ServiceException(jsonObject.getString("messages"));
								}
								return jsonObject.getJSONObject("data").getInteger("ticket");

							}
							LOG.error("the response code is not expected, " + responseCode);
							throw new ServiceException("错误的状态码" + responseCode);
						}
						// train_date=Sat+Feb+10+2018+00%3A00%3A00+GMT%2B0800&train_no=57000Z438205&stationTrainCode=Z4382
						// &seatType=3&fromStationTelecode=HGH&toStationTelecode=SNH
						// &leftTicket=jrgGYjN3HaXuNluS%252Fe%252FmC4R%252BJtBuCq%252BA%252BbFpZjMk8XV2Ich0%252F82ZT6x7rEs%253D
						// &purpose_codes=00&train_location=G2&_json_att=&REPEAT_SUBMIT_TOKEN=1a6fb71819f76bf63c89038aa56246e0
					}, new NameValuePair[] { //
							new NameValuePair("choose_seats", StringUtils.EMPTY) //
							, new NameValuePair("dwAll", "N") //
							, new NameValuePair("key_check_isChange", keyCheckIsChange) //
							, new NameValuePair("leftTicketStr", leftTicket) //
							, new NameValuePair("oldPassengerStr", oldPassengerStr) //
							, new NameValuePair("passengerTicketStr", passengerTicketStr) //
							, new NameValuePair("purpose_codes", purposeCodes) //
							, new NameValuePair("randCode", StringUtils.EMPTY) //
							, new NameValuePair("REPEAT_SUBMIT_TOKEN", repeatSubmitToken) //

							, new NameValuePair("fromStationTelecode", fromStationTelecode) //
							, new NameValuePair("toStationTelecode", toStationTelecode) //
							, new NameValuePair("train_location", trainLocation) //
							, new NameValuePair("_json_att", StringUtils.EMPTY) //
					});

					LOG.info("【" + trainTicket.getF3() + "】【" + seatLevel.getName() + "】 ticket count:" + ticketCount);
					break;
				} catch (Exception e) {
					LOG.error(">>> 【" + trainTicket.getF3() + "】【" + seatLevel.getName() + "】失败", e);
				}
			}

			break;
		}
	}

	public static void main(String[] args) throws Exception {
		Main.do11();
		buyTickets(new ArrayList<>(Arrays.asList("李明会", "张三", "李四")), "2018-02-10", "杭州", "上海",
				new ArrayList<>(Arrays.asList("Z4382", "K528", "Z248", "K1500")),
				new ArrayList<>(Arrays.asList("5", "4", "3", "2", "1")));
	}

}
