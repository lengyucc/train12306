package com.antbean.train12306.entity;

public class SeatLevel {
	private String level;
	private String name;

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SeatLevel(String level, String name) {
		super();
		this.level = level;
		this.name = name;
	}

	public SeatLevel() {
		super();
	}

	@Override
	public String toString() {
		return "SeatLevel [level=" + level + ", name=" + name + "]";
	}

}
