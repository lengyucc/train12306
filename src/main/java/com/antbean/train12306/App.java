package com.antbean.train12306;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;

public class App {
	static {
		Protocol easyhttps = new Protocol("https", (ProtocolSocketFactory) new SSLProtocolSocketFactory(), 443);
		Protocol.registerProtocol("https", easyhttps);
	}

	public static void main(String[] args) throws HttpException, IOException {
		System.out.println("Hello World!");
		HttpClient httpclient = new HttpClient();// 创建一个客户端，类似打开一个浏览器
		GetMethod getMethod = new GetMethod("https://kyfw.12306.cn/passport/captcha/captcha-image");// 创建一个get方法，类似在浏览器地址栏中输入一个地址
		int statusCode = httpclient.executeMethod(getMethod);// 回车——出拳！
		System.out.println("statusCode=" + statusCode);
		if (statusCode == 200) {
			Cookie[] cookies = httpclient.getState().getCookies();
			for (int i = 0; i < cookies.length; i++) {
				System.out.println(cookies[i].getName() + ":" + cookies[i].getValue());
			}
		}
		OutputStream outputStream = new FileOutputStream("e:/code.jpg");
		int len = 0;
		byte[] buff = new byte[1024 * 10];
		InputStream inputStream = getMethod.getResponseBodyAsStream();
		while ((len = inputStream.read(buff)) > 0) {
			outputStream.write(buff, 0, len);
		}
		outputStream.close();

		getMethod.releaseConnection();// 释放，记得收拳哦
	}
}
