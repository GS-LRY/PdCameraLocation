package com.tlkj.pdcameralocation;

import java.io.Serializable;

public class LocalLatLng implements Serializable{
	private static final long serialVersionUID = -6919461967497580385L;  
	private int id;
	private String lat;// 纬度
	private String lng; // 经度
	private String authId;// 认证Id
	private String platformIp;// 平台Ip
	public String getPlatformIp() {
		return platformIp;
	}
	public void setPlatformIp(String platformIp) {
		this.platformIp = platformIp;
	}
	private String deviceAccessId;// 设备接入Id
	private String ballMachineMainIp;// 球机主机Ip
	private String subnetMask;// 子网掩码
	private String gateWay;// 网关
	private String entryNumber;// 录入编号
	private String publicLocus;// 公示点位
	private String installLocation;// 设备安装位置
	private String accessWay;// 接入方式
	private String note;// 备注
	private String pac;// �?
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLng() {
		return lng;
	}
	public void setLng(String lng) {
		this.lng = lng;
	}
	public String getAuthId() {
		return authId;
	}
	public void setAuthId(String authId) {
		this.authId = authId;
	}
	public String getDeviceAccessId() {
		return deviceAccessId;
	}
	public void setDeviceAccessId(String deviceAccessId) {
		this.deviceAccessId = deviceAccessId;
	}
	public String getBallMachineMainIp() {
		return ballMachineMainIp;
	}
	public void setBallMachineMainIp(String ballMachineMainIp) {
		this.ballMachineMainIp = ballMachineMainIp;
	}
	public String getSubnetMask() {
		return subnetMask;
	}
	public void setSubnetMask(String subnetMask) {
		this.subnetMask = subnetMask;
	}
	public String getGateWay() {
		return gateWay;
	}
	public void setGateWay(String gateWay) {
		this.gateWay = gateWay;
	}
	public String getEntryNumber() {
		return entryNumber;
	}
	public void setEntryNumber(String entryNumber) {
		this.entryNumber = entryNumber;
	}
	public String getPublicLocus() {
		return publicLocus;
	}
	public void setPublicLocus(String publicLocus) {
		this.publicLocus = publicLocus;
	}
	public String getInstallLocation() {
		return installLocation;
	}
	public void setInstallLocation(String installLocation) {
		this.installLocation = installLocation;
	}
	public String getAccessWay() {
		return accessWay;
	}
	public void setAccessWay(String accessWay) {
		this.accessWay = accessWay;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getPac() {
		return pac;
	}
	public void setPac(String pac) {
		this.pac = pac;
	}
	
	
}
