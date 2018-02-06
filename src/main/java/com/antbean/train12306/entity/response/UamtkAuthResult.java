package com.antbean.train12306.entity.response;

public class UamtkAuthResult {
// {"result_message":"验证通过","result_code":0,"apptk":null,"newapptk":"Cz2EHtYAmjS5slF6SMP5pdvzlzbmro07mN7dW46t_e_ZF9VRty1210"}
	private String result_message;
	private Integer result_code;
	private String newapptk;
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
	public String getNewapptk() {
		return newapptk;
	}
	public void setNewapptk(String newapptk) {
		this.newapptk = newapptk;
	}
	
}
