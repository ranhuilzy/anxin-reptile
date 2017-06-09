/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: conf-cloud-server
 * @Title: WebSpiderAppMain
 * @Package PACKAGE_NAME
 * @ClassName: WebSpiderAppMain
 * @Description: ${TODO}(用一句话描述该文件做什么)
 * @date 2017/6/1 16:24
 */

import com.anxin.replile.web.base.BootAppMainStrap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.IOException;

public class WebSpiderAppMain {
//    private static Map<Integer, String> cssMap = new HashMap<Integer, String>();
//    private static BufferedWriter bufferedWriter = null;
//    static
//    {
//        cssMap.put(1, "addlist");// 省
//        cssMap.put(2, "h3");// 市
//        cssMap.put(3, "table");// 县
////        cssMap.put(, "towntr");// 镇
////        cssMap.put(, "villagetr");// 村
//    }
    public static void main(String[] args) throws IOException
    {
        SpringApplicationBuilder appBuilder=new SpringApplicationBuilder();
        appBuilder.sources(BootAppMainStrap.class);
        appBuilder.profiles("local");
        appBuilder.bannerMode(Banner.Mode.OFF);
        appBuilder.run(args);
//        int level =1;
//        initFile();
//        // 获取全国各个省级信息
//        Document connect = connect("http://www.ccb.com/cn/OtherResource/bankroll/html/code_help.html");
//        Document connect = connect("http://www.eoeit.cn/lhh/index.php?bank=&key=&province=&city=&page=2");
//        Elements rowProvince = connect.select("div.tbdata");
//        String html= HttpRequestHandle.sendGet("http://www.eoeit.cn/lhh/index.php?bank=&key=&province=&city=&page=2",null,null);
//        Document document=Jsoup.parseBodyFragment(html);
//        System.out.println("返回数据:"+document.toString());
//        int i=1;
//        List<Map<String,Object>> datas=new ArrayList<Map<String,Object>>();
//        for (Element provinceElement : rowProvince)// 遍历每一行的省份城市
//        {
//            Map<String,Object> provDatas=new HashMap();
//            Element h3Elemt = provinceElement.select(cssMap.get(level+1)).first();
//            Element tabElemt = provinceElement.select(cssMap.get(level+2)).get(0);
//            provDatas.put(h3Elemt.text(),getProvDatas(tabElemt));
//            //System.out.println("序列号:"+i+" 省名称:"+h3Elemt.text());
//            i++;
//            datas.add(provDatas);
//        }
//        printInfo(JSON.toJSON(datas).toString());
//        closeStream();
    }
//    private static List<Map<String,Object>> getProvDatas(Element tabElemt){
//        int level =1;
//        List<Map<String,Object>> provDatas=new ArrayList<Map<String,Object>>();
//        tabElemt.select("tr").first().remove();
//        Elements trElemts = tabElemt.select("tr");
//        for (Element trElemt : trElemts)// 遍历每一行的省份城市
//        {
//            Map<String,Object> tmpMap=new HashMap<String,Object>();
//            Elements tdElemts=trElemt.select("td");
//            tmpMap.put(tdElemts.select("td").first().text(),tdElemts.select("td").last().text());
//            provDatas.add(tmpMap);
//        }
//        return provDatas;
//    }
//

}
