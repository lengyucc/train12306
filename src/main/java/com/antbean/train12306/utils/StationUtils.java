package com.antbean.train12306.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.antbean.train12306.entity.Station;

public final class StationUtils {
	public static final String REGEX_STATION = "@([a-z]+)\\|([\\u4E00-\\u9FA5]+)\\|([A-Z]+)\\|([a-z]+)\\|([a-z]+)\\|(\\d+)";

	public static List<Station> parseStations(String resource) {
		List<Station> stations = new ArrayList<Station>();
		Pattern pattern = Pattern.compile(REGEX_STATION);
		Matcher matcher = pattern.matcher(resource);
		while (matcher.find()) {
			String g1 = matcher.group(1); // 站点唯一标识
			String g2 = matcher.group(2); // 站点中文
			String g3 = matcher.group(3); // 站点检索字符
			String g4 = matcher.group(4); // 站点拼音全拼
			String g5 = matcher.group(5); // 站点拼音首字母
			String g6 = matcher.group(6); // 站点序号
			stations.add(new Station(g1, g2, g3, g4, g5, g6));
		}
		return stations;
	}

	public static List<Station> findStationsByKeywords(List<Station> stations, String keywords) {
		boolean f1 = keywords.matches("^[a-zA-z]+$");
		boolean f2 = keywords.matches("^[\\u4E00-\\u9FA5]+$");
		List<Station> findResults = new ArrayList<Station>();
		if (!f1 && !f2)
			return findResults;
		for (Station station : stations) {
			boolean b = (f2 && station.getG2().contains(keywords)) //
					|| (f1 && (station.getG5().toUpperCase().contains(keywords.toUpperCase()) //
							|| station.getG5().toUpperCase().contains(keywords.toUpperCase()) //
							|| station.getG4().toUpperCase().contains(keywords.toUpperCase()) //
					));
			if (b) {
				findResults.add(station);
			}
		}
		return findResults;
	}

	public static void main(String[] args) throws IOException {
		String resource = FileUtils.readFileToString(
				new File(ClassLoader.getSystemClassLoader().getResource("12306/stations.txt").getFile()), "utf-8");
		List<Station> stations = parseStations(resource);
		System.out.println(stations.size());
		System.out.println(findStationsByKeywords(stations, "zk"));
	}
}
