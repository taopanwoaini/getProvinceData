package com.fd.data;

public class SSQ {
	// 省
	private String province;
	// 市
	private String city;
	// 区
	private String area;
	// 街道
	private String addr;
	// 省ID
	private Long pid = 0L;
	// 市ID
	private Long cid = 0L;
	// 区ID
	private Long aid = 0L;
	// 省，直辖市，自治区：0、1、2
	private Integer tp = 0;

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public Integer getTp() {
		return tp;
	}

	public void setTp(Integer tp) {
		this.tp = tp;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public Long getCid() {
		return cid;
	}

	public void setCid(Long cid) {
		this.cid = cid;
	}

	public Long getAid() {
		return aid;
	}

	public void setAid(Long aid) {
		this.aid = aid;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

}
