package com.antbean.train12306.utils;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

public class Train12306DateUtils {
	public static String format(String dateStr) {
		Date date;
		try {
			date = DateUtils.parseDate(dateStr, "yyyy-MM-dd");
		} catch (ParseException e) {
			throw new ServiceException("日期格式必须为yyyy-MM-dd");
		}
		date = DateUtils.addHours(date, 8);

		// Sat+Feb+10+2018+00:00:00+GMT+0800
		// Sat+Feb+10+2018+08:00:00+GMT+0800
//		return DateFormatUtils.format(date, "EEE+MMM+dd+yyyy+HH:mm:ss+'GMT'+0800", Locale.ENGLISH);
		return DateFormatUtils.format(date, "EEE MMM dd yyyy HH:mm:ss 'GMT'+0800", Locale.ENGLISH);
	}
	
	public static void main(String[] args) {
		System.out.println(format("2018-02-10"));
	}
}
