package com.antbean.train12306.handler.impl;

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;

import com.antbean.train12306.handler.HttpResponseHandler;

public class StringHttpResponseHandler implements HttpResponseHandler<String> {

	@Override
	public String process(int responseCode, HttpMethod httpMethod) throws IOException {
		if (200 == responseCode) {
			return httpMethod.getResponseBodyAsString();
		}
		throw new RuntimeException("错误的状态码" + responseCode);
	}
}
