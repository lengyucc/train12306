package com.antbean.train12306.handler;

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;

public interface HttpResponseHandler<T> {
	T process(int responseCode, HttpMethod httpMethod) throws IOException;
}
