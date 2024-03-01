package com.github.catvod.spider;

import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Filter;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Njav extends Spider {

    private final String url = "https://njav.tv/";

    @Override
    public String homeContent(boolean filter) throws Exception {
        List<Vod> list = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        LinkedHashMap<String, List<Filter>> filters = new LinkedHashMap<>();
        Document doc = Jsoup.parse(OkHttp.string(url));
        for (Element a : doc.select("#nav > li > a")) {
            String typeId = a.attr("href").replace(url, "");
            if (typeId.contains("VR")) {
                classes.add(new Class(typeId, a.text()));
                filters.put(typeId, Arrays.asList(new Filter("filter", "過濾", Arrays.asList(new Filter.Value("全部", ""), new Filter.Value("单身女演员", "single_actress")))));
            }
        }
        for (Element div : doc.select("div.box-item")) {
            String id = div.select(".detail >a").attr("href").replace(url, "");
            String name = div.select(".detail >a").text();
            String pic = div.select("img").attr("data-src");
            if (pic.isEmpty()) pic = div.select("img").attr("src");
            if (TextUtils.isEmpty(name)) continue;
            list.add(new Vod(id, name, pic));
        }
        return Result.string(classes, list, filters);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        List<Vod> list = new ArrayList<>();
        String target = url + tid;
        String filters = extend.get("filter");
        if (TextUtils.isEmpty(filters)) target += "?page=" + pg;
        else target += "?filter=" + extend.get("filter") + "&page=" + pg;
        Document doc = Jsoup.parse(OkHttp.string(target));
        for (Element div : doc.select("div.box-item")) {
            String id = div.select(".detail >a").attr("href").replace(url, "");
            String name = div.select(".detail >a").text();
            String pic = div.select("img").attr("data-src");
            if (pic.isEmpty()) pic = div.select("img").attr("src");
            if (TextUtils.isEmpty(name)) continue;
            list.add(new Vod(id, name, pic));
        }
        return Result.string(list);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        Document doc = Jsoup.parse(OkHttp.string(url + ids.get(0)));
        String name = doc.select("meta[property=og:title]").attr("content");
        String pic = doc.select("meta[property=og:image]").attr("content");
        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodPic(pic);
        vod.setVodName(name);
        vod.setVodPlayFrom("Njav");
        vod.setVodPlayUrl("播放$" + ids.get(0));
        return Result.string(vod);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        return searchContent(key, "1");
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        return searchContent(key, pg);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        return Result.get().parse().url(url + id).string();
    }

    private String searchContent(String key, String pg) {
        List<Vod> list = new ArrayList<>();
        Document doc = Jsoup.parse(OkHttp.string(url + "search?keyword=" + key + "&page=" + pg));
        for (Element div : doc.select("div.box-item")) {
            String id = div.select(".detail >a").attr("href").replace(url, "");
            String name = div.select(".detail >a").text();
            String pic = div.select("img").attr("data-src");
            if (pic.isEmpty()) pic = div.select("img").attr("src");
            if (TextUtils.isEmpty(name)) continue;
            list.add(new Vod(id, name, pic));
        }
        return Result.string(list);
    }
}
