package com.antbean.train12306.entity.response;

public class CheckCaptchaResult {
	private String result_message;
	private String result_code;

	public String getResult_message() {
		return result_message;
	}

	public void setResult_message(String result_message) {
		this.result_message = result_message;
	}

	public String getResult_code() {
		return result_code;
	}

	public void setResult_code(String result_code) {
		this.result_code = result_code;
	}

	@Override
	public String toString() {
		return "CheckCaptchaResult [result_message=" + result_message + ", result_code=" + result_code + "]";
	}

}