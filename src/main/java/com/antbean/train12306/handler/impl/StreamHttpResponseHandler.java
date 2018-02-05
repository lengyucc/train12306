package com.antbean.train12306.handler.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpMethod;

import com.antbean.train12306.handler.HttpResponseHandler;

public class StreamHttpResponseHandler implements HttpResponseHandler<InputStream> {

	@Override
	public InputStream process(int responseCode, HttpMethod httpMethod) throws IOException {
		if (200 == responseCode) {
			return httpMethod.getResponseBodyAsStream();
		}
		throw new RuntimeException("错误的状态码" + responseCode);
	}
}
