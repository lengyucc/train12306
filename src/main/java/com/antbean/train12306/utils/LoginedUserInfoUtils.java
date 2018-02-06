package com.antbean.train12306.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.antbean.train12306.entity.LoginedUserInfo;

public final class LoginedUserInfoUtils {

	public static LoginedUserInfo parseLoginedUserInfo(String resource) {
		Map<String, Object> data = new HashMap<String, Object>();
		Document doc = Jsoup.parse(resource);
		Elements infoblocks = doc.getElementsByClass("infoblock");
		for (int i = 0; i < infoblocks.size(); i++) {
			Element infoblock = infoblocks.get(i);
			Elements infoItems = infoblock.getElementsByClass("base-view").first().getElementsByClass("info-item");
			for (int j = 0; j < infoItems.size(); j++) {
				Element infoItem = infoItems.get(j);
				String text = infoItem.text();
				String[] arr = text.split("：");
				if (arr.length == 2) {
					String name = arr[0];
					String value = arr[1].trim();
					data.put(name, value);
				}
			}
		}
		try {
			return BeanUtils.map2BeanWithViewLike(data, LoginedUserInfo.class);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("解析用户信息失败", e);
		}
	}

	public static void main(String[] args) throws Exception {
		String resource = FileUtils.readFileToString(new File("C:\\Users\\work0401\\Desktop\\loginedUserInfo.html"),
				"utf-8");
		parseLoginedUserInfo(resource);
	}

}
