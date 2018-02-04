package com.antbean.train12306.entity;

import java.io.Serializable;

public class Station implements Serializable {
	private static final long serialVersionUID = 1L;
	protected String g1; // 站点唯一标识
	protected String g2; // 站点中文
	protected String g3; // 站点检索字符
	protected String g4; // 站点拼音全拼
	protected String g5; // 站点拼音首字母
	protected String g6; // 站点序号

	public String getG1() {
		return g1;
	}

	public void setG1(String g1) {
		this.g1 = g1;
	}

	public String getG2() {
		return g2;
	}

	public void setG2(String g2) {
		this.g2 = g2;
	}

	public String getG3() {
		return g3;
	}

	public void setG3(String g3) {
		this.g3 = g3;
	}

	public String getG4() {
		return g4;
	}

	public void setG4(String g4) {
		this.g4 = g4;
	}

	public String getG5() {
		return g5;
	}

	public void setG5(String g5) {
		this.g5 = g5;
	}

	public String getG6() {
		return g6;
	}

	public void setG6(String g6) {
		this.g6 = g6;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "Station [g1=" + g1 + ", g2=" + g2 + ", g3=" + g3 + ", g4=" + g4 + ", g5=" + g5 + ", g6=" + g6 + "]";
	}

	public Station(String g1, String g2, String g3, String g4, String g5, String g6) {
		super();
		this.g1 = g1;
		this.g2 = g2;
		this.g3 = g3;
		this.g4 = g4;
		this.g5 = g5;
		this.g6 = g6;
	}

	public Station() {
		super();
	}

}
