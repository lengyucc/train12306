package com.antbean.train12306;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class Test {
	public static void main(String[] args) throws Exception {
		// @bjb|北京北|VAP|beijingbei|bjb|0@bjd|北京东|BOP|beijingdong|bjd|1@bji|北京|BJP|beijing|bj|
		String stations = FileUtils.readFileToString(
				new File(ClassLoader.getSystemClassLoader().getResource("12306/stations.txt").getFile()), "utf-8");
		System.out.println(stations);
		Pattern pattern = Pattern.compile("@([a-z]+)\\|([\u4E00-\u9FA5]+)\\|([A-Z]+)\\|([a-z]+)\\|([a-z]+)\\|(\\d+)");
		Matcher matcher = pattern.matcher(stations);
		int idx = 0;
		while (matcher.find()) {
			// System.out.println(idx++ + ":" + matcher.group());
			String g1 = matcher.group(1); // 站点唯一标识
			String g2 = matcher.group(2); // 站点中文
			String g3 = matcher.group(3); // 站点检索字符
			String g4 = matcher.group(4); // 站点拼音全拼
			String g5 = matcher.group(5); // 站点拼音首字母
			String g6 = matcher.group(6); // 站点序号
//			if (!g1.equals(g5)) {
//				System.out.println(matcher.group());
//			}
			if(!g1.equalsIgnoreCase(g3)) {
				System.out.println(matcher.group());
			}
		}

		// Pattern pattern = Pattern.compile("@\\d+");
		// Matcher matcher = pattern.matcher("@12312@abc@123@233");
		// while (matcher.find()) {
		// System.out.println(matcher.group());
		// }
		//
	}
}
