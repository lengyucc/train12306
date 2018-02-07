package com.antbean.train12306.utils;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.alibaba.fastjson.JSONObject;

public class ImageRecognitionUtils {
	private static HttpClient httpClient = new HttpClient();
	private static final String URL_CHARSET = "utf-8";
	private static final String IMAGE_UP_URL = "http://pic.sogou.com/ris_upload";
	private static final String RECOGNITION_IMG_URL = "http://pic.sogou.com/ris";

	private static String upImg(File img) {
		try {
			MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(
					new Part[] { new FilePart("pic_path", img) }, new HttpMethodParams());
			PostMethod method = new PostMethod(IMAGE_UP_URL);
			method.setRequestEntity(multipartRequestEntity);

			int code = httpClient.executeMethod(method);
			if (code != 302) {
				throw new RuntimeException("不是期望的状态码" + 302);
			}
			Header[] headers = method.getResponseHeaders("Location");
			for (Header header : headers) {
				String text = header.getValue();
				// http://pic.sogou.com/ris?query=http%3A%2F%2Fimg02.sogoucdn.com%2Fapp%2Fa%2F100520146%2FC9489AAA16F887779E85398599A69545&oname=TIM%3F%3F20180206175116.png&flag=1
				Pattern pattern = Pattern.compile("query\\=(.+)&");
				Matcher matcher = pattern.matcher(text);
				while (matcher.find()) {
					String imgUrl = matcher.group(1);
					return URLDecoder.decode(imgUrl, URL_CHARSET);
				}
			}
			throw new RuntimeException("没有找到图片服务器地址");
		} catch (IOException e) {
			throw new RuntimeException("上传图片失败", e);
		}
	}

	private static String recognitionImg(String imgUrl) {
		try {
			String queryString = "query=" + URLEncoder.encode(imgUrl, URL_CHARSET)
					+ "&flag=1&reqType=ajax&reqFrom=result";
			GetMethod method = new GetMethod(RECOGNITION_IMG_URL);
			method.setQueryString(queryString);
			int code = httpClient.executeMethod(method);
			if (code != 200) {
				throw new RuntimeException("不是期望的状态码" + 302);
			}
			String responseBodyAsString = method.getResponseBodyAsString();
			JSONObject jsonObject = JSONObject.parseObject(responseBodyAsString);
			String result = null;
			if (!jsonObject.containsKey("entity") || "".equals((result = jsonObject.getString("entity").trim()))) {
				throw new RuntimeException("没有识别出结果");
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException("识别图片出错", e);
		}
	}

	public static String recognitionImg(File img) {
		return recognitionImg(upImg(img));
	}

	public static void main(String[] args) throws HttpException, IOException {
		File file = new File("C:\\Users\\work0401\\Desktop\\12306验证码\\TIM截图20180206175116.png");
		// String imgUrl = upImg(file);
		// String result = recognitionImg(imgUrl);
		// System.out.println(result);

		Map<String, String> map = new LinkedHashMap<>();
		for (File f : file.getParentFile().listFiles()) {
			try {
				String result = recognitionImg(f);
				System.out.println(f.getName() + " : " + result);
				map.put(f.getName(), result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(JSONObject.toJSONString(map));

	}

}
