package com.antbean.train12306.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.baidu.aip.ocr.AipOcr;

/**
 * 对接百度文字识别
 */
public class WordRecognitionUtils {
	// 设置APPID/AK/SK
	public static final String APP_ID = "10806202";
	public static final String API_KEY = "Ok4HD6822RxTnyNIMlsmdIPk";
	public static final String SECRET_KEY = "yNm2ivP0KfnTcniMrhFaRGO3xyUiNGpQ";

	private static final AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);

	static {
		// 可选：设置网络连接参数
		client.setConnectionTimeoutInMillis(2000);
		client.setSocketTimeoutInMillis(60000);
	}

	// {"words_result":[{"words":"档案袋"}],"words_result_num":1,"log_id":2768531596354836007}
	public static String[] getWordsFromImg(String imgPath) {
		JSONObject res = client.basicAccurateGeneral(imgPath, new HashMap<String, String>());
		if (!res.has("words_result_num") || res.getInt("words_result_num") <= 0) {
			return new String[0];
		}
		JSONArray jsonArray = res.getJSONArray("words_result");
		String[] words = new String[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			words[i] = jsonObject.getString("words");
		}
		return words;
	}

	public static void main(String[] args) throws HttpException, IOException {
		// 调用接口
		String path = "C:\\Users\\work0401\\Desktop\\12306验证码\\captcha_8_t2.jpg";
		System.out.println(Arrays.toString(getWordsFromImg(path)));
	}

}
