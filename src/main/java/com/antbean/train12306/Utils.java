package com.antbean.train12306;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import com.alibaba.fastjson.JSONObject;

public class Utils {
	private static HttpClient HTTP_CLIENT;// 创建一个客户端，类似打开一个浏览器
	public static final String INIT_URL = "https://kyfw.12306.cn/otn/login/init";
	public static final String GET_CAPTCHA_URL = "https://kyfw.12306.cn/passport/captcha/captcha-image";
	public static final String CHECK_CAPTCHA_URL = "https://kyfw.12306.cn/passport/captcha/captcha-check";
	public static final String LOGIN_URL = "https://kyfw.12306.cn/passport/web/login";

	private static String login_uamtk;

	public static void main(String[] args) throws HttpException, IOException {
		start();
		System.out.println("#####################");
//		req("https://kyfw.12306.cn/otn/confirmPassenger/initDc?_json_att", "", "");
	}

	public static void start() {
		HTTP_CLIENT = new HttpClient();
		HTTP_CLIENT.getParams().setCookiePolicy(CookiePolicy.DEFAULT);
		try {
			writeCaptcha(new FileOutputStream(
					ClassLoader.getSystemClassLoader().getResource("12306/captcha_0.jpg").getFile()));
			String answer = inputCaptcha();
			boolean checkCaptchaResult = checkCaptcha(answer);
			if (checkCaptchaResult) {
				System.out.println("校验验证码成功!");
				System.out.println("开始登录……");
				boolean loginResult = login("1902328305@qq.com", "ming2014");
				if (loginResult) {
					System.out.println("登录成功！" + login_uamtk);

					// 检查用户
					
				} else {
					System.out.println("登录失败!");
					System.exit(1);
				}
			} else {
				System.err.println("验证码输入失败!");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean login(String username, String password) throws IOException {
		String queryString = "username=" + username + "&password=" + password + "&appid=otn";
		PostMethod getMethod = new PostMethod(LOGIN_URL);
		getMethod.setQueryString(queryString);
		int statusCode = HTTP_CLIENT.executeMethod(getMethod);
		System.out.println("statusCode=" + statusCode);
		if (statusCode == 200) {
			String body = getMethod.getResponseBodyAsString();
			System.out.println(body);
			boolean loginResult = body.contains("登录成功");
			if (loginResult) {
				JSONObject jsonObject = JSONObject.parseObject(body);
				login_uamtk = jsonObject.getString("uamtk");
				return true;
			}
		}
		return false;
	}

	public static String inputCaptcha() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("请输入验证码:");
		String answer = scanner.next();
		scanner.close();
		return answer;
	}

	public static boolean checkCaptcha(String captcha) throws HttpException, IOException {
		String queryString = "answer=" + captcha + "&login_site=E&rand=sjrand";
		GetMethod getMethod = new GetMethod(CHECK_CAPTCHA_URL);
		getMethod.setQueryString(queryString);
		int statusCode = HTTP_CLIENT.executeMethod(getMethod);
		System.out.println("statusCode=" + statusCode);
		if (statusCode == 200) {
			String body = getMethod.getResponseBodyAsString();
			System.out.println(body);
			return body.contains("成功");
		}
		return false;
	}

	public static void writeCaptcha(OutputStream out) throws IOException {
		GetMethod getMethod = new GetMethod(GET_CAPTCHA_URL);
		int statusCode = HTTP_CLIENT.executeMethod(getMethod);
		System.out.println("statusCode=" + statusCode);
		if (statusCode == 200) {
			int len = 0;
			byte[] buff = new byte[1024 * 10];
			InputStream inputStream = getMethod.getResponseBodyAsStream();
			while ((len = inputStream.read(buff)) > 0) {
				out.write(buff, 0, len);
			}
			System.out.println("获取验证码成功!");
		}
	}

	public static void init() throws IOException {
		req(INIT_URL, "初始化成功!", "初始化失败!");
	}

	public static void req(String url, String success, String failure) throws HttpException, IOException {
		// GetMethod getMethod = new GetMethod(url);
		PostMethod getMethod = new PostMethod(url);
		int statusCode = HTTP_CLIENT.executeMethod(getMethod);
		System.out.println("statusCode=" + statusCode);
		if (statusCode == 200) {
			String body = getMethod.getResponseBodyAsString();
			System.out.println(body);
			System.out.println(success);
		} else {
			System.out.println(failure);
		}
	}
}
