package com.antbean.train12306.entity;

public class LoginResult {
	private String result_message;
	private Integer result_code;
	private String uamtk;

	public String getResult_message() {
		return result_message;
	}

	public void setResult_message(String result_message) {
		this.result_message = result_message;
	}

	public Integer getResult_code() {
		return result_code;
	}

	public void setResult_code(Integer result_code) {
		this.result_code = result_code;
	}

	public String getUamtk() {
		return uamtk;
	}

	public void setUamtk(String uamtk) {
		this.uamtk = uamtk;
	}

}
