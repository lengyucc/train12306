package com.antbean.train12306.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.antbean.train12306.entity.TrainTicket;

public final class TicketUtils {
	public static List<TrainTicket> parseTickets(String resource) {
		List<TrainTicket> result = new ArrayList<>();
		JSONObject jsonObject = JSONObject.parseObject(resource);
		JSONArray jsonArray = jsonObject.getJSONArray("result");
		if (jsonArray.size() > 0) {
			for (int i = 0; i < jsonArray.size(); i++) {
				try {
					String text = jsonArray.getString(i);
					String[] arr = text.split("\\|");

					TrainTicket tt = new TrainTicket();
					for (int j = 0; j < arr.length; j++) {
						BeanUtils.setProperty(tt, "f" + j, arr[j]);
					}
					result.add(tt);
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		String[] arr = "dqE%2Bvn4X3iCldZRWzW5eHY9eVPP9P5wJczIVIkriAUewnsFOgku4om%2FPKKkjnL2b0Bf6y7tyjXzf%0AZA7gE4LEvbugpgK5Xqt76BkD5rWnfb7Tu0zGzDIAJPTm2TUmLhuOE5a8fnZoJcBo1ZLb%2FfKRFUwA%0AoP30k3qvttnJ7TDQgLO%2FnPbR5K6j0KFEV5tQLcdg2HlIxddP%2B5yxqyducNUPIUMAxcgsM8pCGgTT%0ABveIcaKoeEPjjg8UcrThTxMWnKp6CM1HufX4v7YQwDCR|预订|57000Z438205|Z4382|NCG|SNH|HGH|SNH|21:09|22:53|01:44|Y|yrE8Y6UzF0XN7SSYTD2PJP2iD7b7CsFzonQ0tPsZl%2Fm3M9eMAG%2BPplKvUj4%3D|20180206|3|G2|06|08|0|0||||有|||有||有|有|||||10411031|1413|0"
				.split("\\|");
		for (int i = 0; i < arr.length; i++) {
			String s = arr[i];
			System.out.println("[" + i + "]" + s);
		}
	}
}
// {
// "data":{
// "flag":"1",
// "map":{
// "FYH":"阜阳",
// "HGH":"杭州东",
// "HZH":"杭州"
// },
// "result":[
// "|预订|26000K12630C|K1263|SJP|HZH|FYH|HZH|01:35|10:51|09:16|N|8tge3Tsc1%2B0miIf9mTe4r3m0wG0ZCyVdzaLeqY29Lg%2BhOoh4rOMMkufogvc%3D|20180220|3|PA|09|15|0|0||||无|||无||无|无|||||10401030|1413|0",
// "|预订|41000011520O|1153|XAY|HZH|FYH|HZH|02:08|11:21|09:13|N|FWzzvasupELsArg6vEyWCM6J80B5zqBAXd1r3FvMETcCVJ48LQ8N0DV6Tu4%3D|20180220|3|Y2|14|21|0|0||||无|||无||无|无|||||10401030|1413|0",
// "|预订|280000K8911C|K891|DTV|HZH|FYH|HZH|03:40|13:03|09:23|N|8tge3Tsc1%2B0miIf9mTe4r3m0wG0ZCyVdzaLeqY29Lg%2BhOoh4rOMMkufogvc%3D|20180220|3|V2|17|25|0|0||||无|||无||无|无|||||10401030|1413|0",
// "|预订|38000K416407|K4161|ZZF|HZH|FYH|HZH|04:36|16:33|11:57|N|y1MSWw%2FnW%2Bz%2FGCUlS0vrA4DAyXt%2FZls1|20180220|3|F2|07|14|0|0|||||||无|||无|||||1010|11|0",
// "|预订|38000K227802|K2275|ZZF|HZH|FYH|HZH|05:05|14:06|09:01|N|qilH4A34PHDvhYGcXSJ0tnJZWdgBDNzuV4MiB576PFuUVsH9eXuxwjdstTo%3D|20180220|3|F1|07|16|0|0||||无|||无||无|无|||||10403010|1431|0",
// "|预订|38000K407302|K4073|XXF|NGH|FYH|HZH|07:12|17:15|10:03|N|y1MSWw%2FnW%2Bz%2FGCUlS0vrA4DAyXt%2FZls1|20180220|3|F1|05|10|0|0|||||||无|||无|||||1010|11|0",
// "|预订|41000K29080D|K2905|XAY|RZH|FYH|HZH|10:05|19:22|09:17|N|8tge3Tsc1%2B0miIf9mTe4r3m0wG0ZCyVdzaLeqY29Lg%2BhOoh4rOMMkufogvc%3D|20180220|3|Y2|09|18|0|0||||无|||无||无|无|||||10401030|1413|0",
// "|预订|27000K139809|K1395|TYV|RZH|FYH|HZH|10:20|20:07|09:47|N|8tge3Tsc1%2B0miIf9mTe4r3m0wG0ZCyVdzaLeqY29Lg%2BhOoh4rOMMkufogvc%3D|20180220|3|V1|13|22|0|0||||无|||无||无|无|||||10401030|1413|0",
// "SQsCuNarEXV%2FgrYZ2e9uFlMqx63egDazvQJHbpa1N3J4VS1xraPNujPDB1BCCf8p%2FkJFQxISzyR%2F%0ABfj0E5i7RIpNG75V9HZFXFlSM%2B0urkGxH24BWdX%2FwR498UWFE7BT0X5%2BThhIudxT7%2B4366CE0MBs%0Ax3dLPJ0dqaai%2FfHVnLs1PsHaHSAT55qw9Bo%2Fm%2B%2BwnWMtsn2t1PPYXrL%2B0q1E%2FYrybU3twtA6jFb8%0A6EdIkJr8XVuu%2BOF9Y4mfLOw%3D|预订|53000K843705|K8437|FYH|HZH|FYH|HZH|10:52|20:15|09:23|Y|fgEBhXBMMcTPZUpBvoX8SgnpTCUoj5%2F2p42aEPJG1r%2B71Cwb|20180221|3|H6|01|09|0|0|||||||有||无|无|||||103010|131|0",
// "|预订|53000K8401C0|K8401|BZH|RZH|FYH|HZH|12:29|02:52|14:23|N|YcXLEyKoj4zNjfnHCihQXvd%2Ftm1r04aUNTdsNtIfL1QUKcIRhBlJL3bvI2o%3D|20180221|3|H2|03|13|0|0||||无|||无||无|无|||||10401030|1413|1",
// "|预订|53000K560440|K5604|FYH|NGH|FYH|HGH|13:27|03:10|13:43|N|wQ%2BpSmPQZ8dgJ0lK4UaHmyKBfXh8YFe%2BcjEMqJvOGWVo%2BoWyE0CkXbx6FhU%3D|20180221|3|H2|01|09|0|0||||无|||无||无|无|||||10401030|1413|0",
// "ZvaS%2FW%2BOl7aVfyCckdYfqrjh8tWQhmwCNUihCDSvAfg1Z%2FnEpKrb3d9b60s7%2B1LSP2GHxgzb7LHj%0Ah86TYiSLPxHm5dCj0vIl2LkmHV31DtMJpkLRIStw0Bl%2FCbW4w4o8oOfYNhEhB6zb1MDyUFWgZX9m%0A49nLkGVu65KDgg535qqO4xIIyqXL8DTCWescHRepJ4l%2BVgRtVhRC9oxUwnGPgRvsk59Tcd8lpNcU%0Aq5XjEQ0UWDZr|预订|53000K563930|K5639|FYH|YWH|FYH|HZH|13:40|23:14|09:34|Y|WPUmqPbiT7aXFRJqew36UsW79VK4BZGi|20180221|0|H1|01|06|0|0|||||||有|||有|||||1010|11|0",
// "e3zRf81rfdQ9hOKN5BaFIsxjI5gUkCH25Y4RmkcpDFzvYj1LVDD0F4rrwNE3PkP1JHM3eSSLZvlx%0A%2BzzCuvACVrGeiRiL7wm%2Bfo%2BagkvpR7xUS4HVLarf1bvZQ3rKeA21G2%2FPtBGUBEFn%2BYxxLxMoSNJX%0AlsCBeX%2FOToQTNWea9LeblwT9jKeUFz7v8NyUbwN9JByiD90RGayCyUQR%2BJb0s2QWZWV7g1dui6o9%0At8vN89pjRjpUBL03moGxbuM%3D|预订|53000K5611C0|K5611|FYH|HZH|FYH|HZH|15:53|01:42|09:49|Y|30UC5NTPp5qpDz5Cx6r98ThGN6RM2corfSd57i4frF1r7i5Z|20180221|0|H3|01|05|0|0|||||无||有|||有|||||101020|112|0",
// "|预订|53000K562180|K5621|FYH|RZH|FYH|HZH|16:40|03:32|10:52|N|1yvcuMJVJBzmB4%2FJUYtJUDwPbowrDnkDAD5L%2Feh1F2gW0Z2t|20180221|3|H2|01|06|0|0|||||无||无|||无|||||101020|112|0",
// "|预订|53000K842170|K8421|HRH|HZH|FYH|HZH|17:07|02:04|08:57|N|1yvcuMJVJBzmB4%2FJUYtJUDwPbowrDnkDAD5L%2Feh1F2gW0Z2t|20180221|3|H1|03|09|0|0|||||无||无|||无|||||101020|112|0",
// "|预订|38000K12400A|K1237|ZZF|RZH|FYH|HZH|17:36|03:11|09:35|N|8tge3Tsc1%2B0miIf9mTe4r3m0wG0ZCyVdzaLeqY29Lg%2BhOoh4rOMMkufogvc%3D|20180221|3|F1|07|13|0|0||||无|||无||无|无|||||10401030|1413|0",
// "Uevn3Mffh3E2e5pDr50cYxmSR0E9e8eiRJq9MogyFAEYUdKYDJRRY7nAik9pZCDcH4jqLTtFlBwg%0A8HEZwLufN9FhpEh0GalyNed95cNvhhle18JFAQPvm%2Fn6gcqVHhKgU%2FB%2F9sam6ZV6k64HGSbGxAcr%0A2hiyA9llG7dMO6W8LWhQyyvotbAy7URQF9K3tCaw65VHWPGNIPx0oWCfXiPvwtfxrn9V3qZJPMr3%0A%2FLsyML9wS5XEyMRTZUeNTcQ%2BQkb3Ijg3qJJoJ1s%3D|预订|53000K8499H0|K8499|FYH|NGH|FYH|HZH|17:45|02:33|08:48|Y|ynueUiXOR4aBYN46MNTpA28NuNerGTDZBQWrlIrsV%2ByPEfCG8gAt3dpy4gc%3D|20180221|3|H2|01|09|0|0||||无|||无||无|有|||||10401030|1413|0",
// "|预订|38000K436405|K4361|ZZF|HZH|FYH|HZH|18:07|05:06|10:59|N|1yvcuMJVJBzmB4%2FJUYtJUDwPbowrDnkDAD5L%2Feh1F2gW0Z2t|20180221|3|F1|06|10|0|0|||||无||无|||无|||||101020|112|0",
// "|预订|53000K560970|K5609|FYH|NGH|FYH|HZH|18:25|04:44|10:19|N|1yvcuMJVJBzmB4%2FJUYtJUDwPbowrDnkDAD5L%2Feh1F2gW0Z2t|20180221|3|H2|01|06|0|0|||||无||无|||无|||||101020|112|0",
// "|预订|53000K8563A3|K8563|BZH|NGH|FYH|HGH|20:33|05:09|08:36|N|%2FO%2BaUbUkY7zPEbXPUo9OVt%2FnfyRcy%2F0uq%2BL0OgB8CkU06D261BSA1G2iB%2FI%3D|20180221|3|H6|02|07|0|0||||无|||无||无|无|||||10401030|1413|0",
// "CfSGsW5xG1WA1CeLFTsgLCzHATGwKFzSNXNGDyWqp2ljwlsGp%2BDoNkFIepQgyr34YcSIUNnCg5a1%0AemMRqR2S%2BYPFSwHinMchR4txgv0NW6xjI8uXUK0l6zJykfLFTdOwSC%2F5UnJHcOi2W%2FDFgDqUwfMU%0AacRtU1Pn3RQoi97I8bomckKhxcZgg3vbpou%2FAiSKHiHS2UOatEd998HjHxXhgwwzOF3Iw1HZnGnc%0AYhC0ykrDHCLbTJ6sCVOEGp3Uwmt8edi7W4CdnDY%3D|预订|53000K560750|K5607|FYH|NGH|FYH|HZH|21:50|06:47|08:57|Y|J8WKGiIuBcJH%2FviEQgILBT6LUjtjmUjRW9XO5i05b3TJskz%2Fy1jMuuBdMxs%3D|20180221|3|H2|01|07|0|0||||无|||4||无|无|||||10401030|1413|0",
// "|预订|380000T32806|T325|ZZF|NGH|FYH|HZH|23:16|08:07|08:51|N|8tge3Tsc1%2B0miIf9mTe4r3m0wG0ZCyVdzaLeqY29Lg%2BhOoh4rOMMkufogvc%3D|20180221|3|F2|05|12|0|0||||无|||无||无|无|||||10401030|1413|0",
// "|预订|49000K105205|K1049|QHK|RZH|FYH|HZH|23:49|08:47|08:58|N|8tge3Tsc1%2B0miIf9mTe4r3m0wG0ZCyVdzaLeqY29Lg%2BhOoh4rOMMkufogvc%3D|20180221|3|KA|14|21|0|0||||无|||无||无|无|||||10401030|1413|0"
// ]
// },
// "httpstatus":200,
// "messages":"",
// "status":true
// }