package com.antbean.train12306;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

public class Test {
	public static void main(String[] args) throws Exception {
		HttpClient client = new HttpClient();
		
		String queryString = "secretStr=LJX77uVGE5NgacIof5pLbpsyIlbf1m%2B2nxXlj0V4Dr3aeO74HAjo5XlWEjbKyNRDfQneQABa2%2BWH%0AcOEylYZTgVzYhUa3EGbxcIGm0j1WDyiOMWLj%2FYLID%2BIveg1GVajqZ5yf8w%2BySyrCDICuXd5ooHjK%0Afc%2FT%2BCdH%2Bta1nz%2F%2Fd7BCUDa2iApnQhGbgXQok0ggd57elFbJpsgM5bFnlwnNmOx9oWasZJ23GWpK%0AJmycWcaCfXbnHbzslgVGdm7sffEqoAHPu1ZS9sM%3D&train_date=2018-02-09&back_train_date=2018-02-09&tour_flag=dc&purpose_codes=ADULT&query_from_station_name=%E6%9D%AD%E5%B7%9E&query_to_station_name=%E4%B8%8A%E6%B5%B7&undefined";
		GetMethod method = new GetMethod("https://kyfw.12306.cn/otn/leftTicket/submitOrderRequest");
		method.setQueryString(queryString);
		int code = client.executeMethod(method);
		System.out.println(code);
		if(code == 200){
			System.out.println(method.getResponseBodyAsString());
		}
	}
}
