package com.antbean.train12306.entity;

import com.antbean.train12306.utils.View;

public class LoginedUserInfo {
	@View(name = "用户名")
	private String username;
	@View(name = "姓名")
	private String realname;
	@View(name = "性别")
	private String gender;
	@View(name = "国家/地区")
	private String region;
	@View(name = "证件类型")
	private String identificationType;
	@View(name = "证件号码")
	private String identificationNo;
	@View(name = "出生日期")
	private String dateOfBirth;
	@View(name = "核验状态")
	private String validateStatus;
	@View(name = "手机号码")
	private String mobile;
	@View(name = "固定电话")
	private String telephone;
	@View(name = "电子邮件")
	private String email;
	@View(name = "地址")
	private String address;
	@View(name = "邮编")
	private String postcode;
	@View(name = "旅客类型")
	private String travellerType;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getIdentificationType() {
		return identificationType;
	}

	public void setIdentificationType(String identificationType) {
		this.identificationType = identificationType;
	}

	public String getIdentificationNo() {
		return identificationNo;
	}

	public void setIdentificationNo(String identificationNo) {
		this.identificationNo = identificationNo;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getValidateStatus() {
		return validateStatus;
	}

	public void setValidateStatus(String validateStatus) {
		this.validateStatus = validateStatus;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getTravellerType() {
		return travellerType;
	}

	public void setTravellerType(String travellerType) {
		this.travellerType = travellerType;
	}

	@Override
	public String toString() {
		return "LoginedUserInfo [username=" + username + ", realname=" + realname + ", gender=" + gender + ", region="
				+ region + ", identificationType=" + identificationType + ", identificationNo=" + identificationNo
				+ ", dateOfBirth=" + dateOfBirth + ", validateStatus=" + validateStatus + ", mobile=" + mobile
				+ ", telephone=" + telephone + ", email=" + email + ", address=" + address + ", postcode=" + postcode
				+ ", travellerType=" + travellerType + "]";
	}

}
