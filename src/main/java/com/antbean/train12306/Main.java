package com.antbean.train12306;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Scanner;

import com.antbean.train12306.entity.LoginedUserInfo;
import com.antbean.train12306.utils.CookieUtils;
import com.antbean.train12306.utils.OSExecute;
import com.antbean.train12306.utils.Train12306HttpUtils;

public class Main {
	static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		start();
	}

	public static void start() {
		while (true) {
			showTitle();
			int code = inputCode();
			try {
				Method method = Main.class.getDeclaredMethod("do" + code);
				method.invoke(null);
			} catch (Exception e) {
				System.err.println("功能尚未实现");
			}
		}
	}

	public static void showTitle() {
		String loginStatus = Train12306HttpUtils.checkLoginStatus() != null ? "已登录" : "未登录";
		System.out.println("************* " + loginStatus + " *************");
		System.out.println("0 退出程序");
		System.out.println("11 登录12306");
		System.out.println("12 退出12306");
		System.out.println("21 查看个人信息");
		System.out.println("3 ");
		System.out.println("4 ");
		System.out.println("5 ");
		System.out.println("6 ");
		System.out.println("******************************");
		System.out.print("选择操作：");
	}

	public static void do0() {
		System.exit(0);
	}

	public static void do6() {
	}

	/**
	 * 登录
	 */
	public static void do11() {
		// System.out.print("12306账号：");
		// String username = inputString();
		String username = "1902328305@qq.com";
		// String username = inputString();
		System.out.print("12306密码：");
		String password = inputString();
		// String password = "adfdf1902328305";
		// 下载验证码
		String captchaPath = ClassLoader.getSystemClassLoader().getResource("12306/captcha_0.jpg").getFile();
		File outFile = null;
		try {
			outFile = new File(captchaPath);
			Train12306HttpUtils.writeCaptcha(outFile);
		} catch (Exception e) {
			throw new RuntimeException("获取验证码失败", e);
		}
		// 打开验证码
		String captchaPage = ClassLoader.getSystemClassLoader().getResource("12306/index.html").getFile();
		OSExecute.command(captchaPage.substring(1));
		// 登录
		System.out.print("验证码（坐标）：");
		String captcha = inputString();
		Train12306HttpUtils.checkCaptcha(captcha);
		Train12306HttpUtils.login(username, password);
	}

	/**
	 * 退出登录
	 */
	public static void do12() {
		CookieUtils.removeAllCookies();
		System.out.println("已退出！");
	}

	/**
	 * 查看个人信息
	 */
	public static void do21() {
		LoginedUserInfo loginedUserInfo = Train12306HttpUtils.checkLoginStatus();
		System.out.println("========================== 个人信息 ==========================");
		System.out.println(loginedUserInfo);
	}

	public static int inputCode() {
		int op;
		try {
			String input = scanner.nextLine();
			op = Integer.parseInt(input);
		} catch (Exception e) {
			op = -1;
		}
		return op;
	}

	public static String inputString() {
		return scanner.nextLine();
	}
}
