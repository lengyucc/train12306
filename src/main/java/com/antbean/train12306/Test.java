package com.antbean.train12306;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

public class Test {
	public static void main(String[] args) throws Exception {
		String str = "2018-02-10";
		Date date = DateUtils.parseDate(str, "yyyy-MM-dd");
		date = DateUtils.addHours(date, 8);
		
		// Sat+Feb+10+2018+00:00:00+GMT+0800
		// Sat+Feb+10+2018+08:00:00+GMT+0800
		System.out.println(DateFormatUtils.format(date, "EEE+MMM+dd+yyyy+HH:mm:ss+'GMT'+0800", Locale.ENGLISH));
	}
}
