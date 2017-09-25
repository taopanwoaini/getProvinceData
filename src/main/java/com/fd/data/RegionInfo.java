package com.fd.data;
/**
 * 省市区数据
 * @author 符冬
 *
 */
public class RegionInfo {
	
	private Long id;
	private String name;
	/**
	 * 上一级ID
	 */
	private Long parentId;
	/**
	 * 层结构：1、省，2、市，3、区县
	 */
	private Integer level;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RegionInfo() {
		super();
	}

	@Override
	public String toString() {
		return "RegionInfo [id=" + id + ", name=" + name + ", parentId=" + parentId + ", level=" + level + "]";
	}

	public RegionInfo(Long id, String name, Long parentId, Integer level) {
		super();
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.level = level;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

}
