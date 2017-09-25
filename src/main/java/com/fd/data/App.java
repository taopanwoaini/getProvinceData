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
 * @author 符冬
 *
 */
public class App 
{
    public static void main( String[] args )
    {try {
		List<RegionInfo> list = getregionlist("http://www.stats.gov.cn/tjsj/tjbz/xzqhdm/201703/t20170310_1471429.html");

		list.forEach(o -> {
			System.out.println(o);

		});
	} catch (MalformedURLException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}}

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
}
