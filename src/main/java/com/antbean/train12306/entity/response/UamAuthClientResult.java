package com.antbean.train12306.entity.response;

public class UamAuthClientResult {
	// {"apptk":"3hYbeze0RDb-XELynhG0uFo-awwsgpUZdEGVOIdWhTXwISp7rw1210","result_code":0,"result_message":"验证通过","username":"李明会"}
	private String result_message;
	private Integer result_code;
	private String apptk;
	private String username;

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

	public String getApptk() {
		return apptk;
	}

	public void setApptk(String apptk) {
		this.apptk = apptk;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
