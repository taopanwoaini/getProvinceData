package com.fd.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 抓取国家统计局官网的省市区最新数据
 * 
 * @author 符冬
 *
 */
public class App {
	public static void main(String[] args) {
		try {
			List<RegionInfo> list = getregionlist(
					"http://www.stats.gov.cn/tjsj/tjbz/xzqhdm/201703/t20170310_1471429.html");
			SSQ ssq = analizeArea("江苏省南通市港闸区保利香槟国际", list);
			System.out.println(ssq.getProvince());
			System.out.println(ssq.getCity());
			System.out.println(ssq.getArea());
			System.out.println(ssq.getAddr());

			// list.forEach(o -> {
			// System.out.println(o);
			//
			// });

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析最新省市区数据库
	 * 
	 * @param urlstr
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static List<RegionInfo> getregionlist(String urlstr) throws MalformedURLException, IOException {
		List<RegionInfo> rlist = new ArrayList<>(3400);
		URL url = new URL(urlstr);
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			String string = sb.toString();

			Pattern p = Pattern.compile("<p class=\"MsoNormal\">.+?</p>");
			Pattern id = Pattern.compile("<span lang=\"EN-US\">\\d+?<span>");
			Pattern name = Pattern.compile("<span style=\"font-family: 宋体\">\\S{3,}?</span>");
			Matcher m = p.matcher(string);
			Long parentid = 0L;
			Long cityId = 0L;
			while (m.find()) {
				String v = m.group();
				if (v.contains("<b>")) {
					Matcher matcher = id.matcher(v);
					Matcher matcher2 = name.matcher(v);
					if (matcher.find() && matcher2.find()) {
						String rid = matcher.group().replaceAll("</?span.*?>", "");
						String rname = matcher2.group().replaceAll("</?span.*?>", "").replaceAll("[\\s\\p{Zs}]+", "");
						rlist.add(new RegionInfo(Long.valueOf(rid), rname, 0L, 1));
						parentid = Long.valueOf(rid);
					}
				} else if (v.contains("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")) {
					Matcher matcher = id.matcher(v);
					Matcher matcher2 = name.matcher(v);
					if (matcher.find() && matcher2.find()) {
						String rname = matcher2.group().replaceAll("</?span.*?>", "").trim().replaceAll("[\\s\\p{Zs}]+",
								"");
						String rid = matcher.group().replaceAll("</?span.*?>", "");
						rlist.add(new RegionInfo(Long.valueOf(rid), rname, parentid, 2));
						cityId = Long.valueOf(rid);
					}
				} else {
					Matcher matcher = id.matcher(v);
					Matcher matcher2 = name.matcher(v);
					if (matcher.find() && matcher2.find()) {
						String rname = matcher2.group().replaceAll("</?span.*?>", "").trim().replaceAll("[\\s\\p{Zs}]+",
								"");
						String rid = matcher.group().replaceAll("</?span.*?>", "");
						rlist.add(new RegionInfo(Long.valueOf(rid), rname, cityId, 3));
					}
				}
			}
		}
		return rlist;
	}

	/**
	 * 自动获取省市区
	 * 
	 * @param tarstr
	 * @param ps
	 * @return
	 */
	public static SSQ analizeArea(String tarstr, List<RegionInfo> ps) {
		SSQ ssq = new SSQ();
		if (tarstr != null && tarstr.trim().length() > 2) {
			try {
				tarstr = tarstr.replaceAll("[\\s\\p{Zs}]+", "");
				/**
				 * 省
				 */
				for (RegionInfo p : ps) {
					if (p.getLevel() == 1) {
						if (tarstr.startsWith(p.getName().substring(0, 2))) {
							ssq.setPid(p.getId());
							ssq.setProvince(p.getName());
							if (p.getName().contains("市")) {
								ssq.setTp(1);
							} else if (p.getName().contains("区")) {
								ssq.setTp(2);
							} else {
								ssq.setTp(0);
							}
							break;
						}

					}
				}
				if (ssq.getPid() == null || ssq.getPid() == 0) {
					a: for (RegionInfo p : ps) {
						if (p.getLevel() == 2) {
							if (tarstr.startsWith(p.getName())) {
								ssq.setCid(p.getId());
								ssq.setCity(p.getName());
								ssq.setPid(p.getParentId());
								for (RegionInfo pp : ps) {
									if (pp.getId().equals(ssq.getPid())) {
										ssq.setProvince(pp.getName());
										break a;
									}
								}
							}
						}
					}
					if (ssq.getCid() == null || ssq.getCid() == 0) {
						a: for (RegionInfo p : ps) {
							if (p.getLevel() == 2) {
								if (tarstr.startsWith(p.getName().substring(0, p.getName().length() - 1))) {
									ssq.setCid(p.getId());
									ssq.setCity(p.getName());
									ssq.setPid(p.getParentId());
									for (RegionInfo pp : ps) {
										if (pp.getId().equals(ssq.getPid())) {
											ssq.setProvince(pp.getName());
											break a;
										}
									}
								}
							}
						}
					}
				} else {
					/**
					 * 市
					 */
					a: for (RegionInfo p : ps) {
						if (p.getLevel() == 2 && p.getParentId().equals(ssq.getPid())) {
							switch (ssq.getTp()) {
							case 1:
								ssq.setCid(p.getId());
								ssq.setCity(p.getName());
								break a;
							case 2:
								boolean q = false;
								for (RegionInfo pp : ps) {
									if (pp.getParentId() == 0) {
										if (tarstr.startsWith(pp.getName())) {
											q = true;
											break;
										}
									}
								}
								String cstr = tarstr.split(ssq.getProvince().substring(0, 2) + "[古]?")[1];
								if (q) {
									cstr = tarstr.split(
											ssq.getProvince().substring(0, ssq.getProvince().length() - 1) + "[区]?")[1];
								}
								if (cstr.startsWith("省")) {
									cstr = cstr.substring(1, cstr.length());
								}
								if (cstr.startsWith(p.getName())
										|| cstr.startsWith(p.getName().substring(0, p.getName().length() - 1))) {
									ssq.setCid(p.getId());
									ssq.setCity(p.getName());
									break a;
								}
								break;
							default:
								cstr = tarstr.split(
										ssq.getProvince().substring(0, ssq.getProvince().length() - 1) + "[省]?")[1];
								if (cstr.startsWith(p.getName())
										|| cstr.startsWith(p.getName().substring(0, p.getName().length() - 1))) {
									ssq.setCid(p.getId());
									ssq.setCity(p.getName());
									break a;
								}
								break;
							}
						}
					}
					if (ssq.getCid() == null || ssq.getCid() == 0) {

						a: for (RegionInfo p : ps) {
							if (p.getLevel() == 2 && p.getParentId().equals(ssq.getPid())) {
								if (tarstr.startsWith(p.getName())) {
									ssq.setCid(p.getId());
									ssq.setCity(p.getName());
									ssq.setPid(p.getParentId());
									for (RegionInfo pp : ps) {
										if (pp.getId().equals(ssq.getPid())) {
											ssq.setProvince(pp.getName());
											break a;
										}
									}
								}
							}
						}
						if (ssq.getCid() == null || ssq.getCid() == 0) {
							a: for (RegionInfo p : ps) {
								if (p.getLevel() == 2 && p.getParentId().equals(ssq.getPid())) {
									if (tarstr.startsWith(p.getName().substring(0, p.getName().length() - 1))) {
										ssq.setCid(p.getId());
										ssq.setCity(p.getName());
										ssq.setPid(p.getParentId());
										for (RegionInfo pp : ps) {
											if (pp.getId().equals(ssq.getPid())) {
												ssq.setProvince(pp.getName());
												break a;
											}
										}
									}
								}
							}
						}

					}
				}
				if (ssq.getCid() == null || ssq.getCid() == 0) {
					for (RegionInfo p : ps) {
						if (p.getLevel() == 2 && p.getParentId().equals(ssq.getPid())) {
							if (tarstr.split(p.getName().substring(0, 2) + "["
									+ p.getName().charAt(p.getName().length() - 1) + "]?").length > 1) {
								ssq.setCid(p.getId());
								ssq.setCity(p.getName());
								break;
							}
						}
					}
					if (ssq.getCid() == null || ssq.getCid() == 0) {
						for (RegionInfo p : ps) {
							if (p.getLevel() == 2 && p.getParentId().equals(ssq.getPid())) {
								if (p.getName().equals("省直辖")) {
									ssq.setCid(p.getId());
									ssq.setCity(p.getName());
									break;
								}
							}
						}
					}
				}
				/**
				 * 区县
				 */
				for (RegionInfo p : ps) {
					if (p.getLevel() == 3 && p.getParentId().equals(ssq.getCid())) {
						switch (ssq.getTp()) {
						case 1:
							for (RegionInfo ar : ps) {
								if (ar.getParentId().equals(ssq.getCid())) {
									String[] aas = tarstr.split(ar.getName());
									if (aas.length == 2) {
										ssq.setAid(ar.getId());
										ssq.setArea(ar.getName());
										break;
									}
								}
							}
							if (ssq.getAid() == 0) {
								for (RegionInfo ar : ps) {
									if (ar.getParentId().equals(ssq.getCid())) {

										String[] aas = tarstr
												.split(ar.getName().substring(0, ar.getName().length() - 1));
										if (aas.length == 2) {
											ssq.setAid(ar.getId());
											ssq.setArea(ar.getName());
											break;
										}

									}
								}
							}

							break;

						default:
							for (RegionInfo ar : ps) {
								if (ar.getParentId().equals(ssq.getCid())) {
									String[] aas = tarstr.split(ar.getName());
									if (aas.length == 2) {
										ssq.setAid(ar.getId());
										ssq.setArea(ar.getName());
										break;
									}
								}
							}
							if (ssq.getAid() == 0) {
								for (RegionInfo ar : ps) {
									if (ar.getParentId().equals(ssq.getCid())) {

										String[] aas = tarstr
												.split(ar.getName().substring(0, ar.getName().length() - 1));
										if (aas.length == 2) {
											ssq.setAid(ar.getId());
											ssq.setArea(ar.getName());
											break;
										}

									}
								}
							}
							if (ssq.getAid() == 0) {
								String arstr = tarstr.split(
										ssq.getCity() + "|" + ssq.getCity().substring(0, ssq.getCity().length() - 1)
												+ "|" + ssq.getProvince())[1];
								for (RegionInfo tpv : ps) {
									if (tpv.getParentId().equals(ssq.getCid())) {
										if (arstr.startsWith(tpv.getName()) || arstr
												.startsWith(tpv.getName().substring(0, tpv.getName().length() - 1))) {
											ssq.setAid(tpv.getId());
											ssq.setArea(tpv.getName());
											break;
										}
									}
								}
								break;
							}

						}

					}

				}
				if (ssq.getCid() > 0 && (ssq.getAid() == null || ssq.getAid() == 0)) {
					int ix = tarstr.indexOf(ssq.getCity());
					if (ix != -1) {
						String ar = tarstr.substring(ix + ssq.getCity().length());
						for (RegionInfo ara : ps) {
							if (ara.getParentId().equals(ssq.getCid())) {
								if (ar.startsWith(ara.getName())) {
									ssq.setAid(ara.getId());
									ssq.setArea(ara.getName());
									break;
								}
							}
						}
					}
				}

				return ssq;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ssq;
	}

}
