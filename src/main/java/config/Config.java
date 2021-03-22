package config;

import org.yaml.snakeyaml.Yaml;
import utils.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Config {
    ArrayList<Resource> resources = new ArrayList<>();
    public void Resolve(){
        FileInputStream fileInputStream = null;
        try {
            Yaml yaml = new Yaml();//实例化解析器
            File file = new File("D://AllProjects//Java//Einux//src//main//java//config//resource.yml");//配置文件地址
            fileInputStream = new FileInputStream(file);
            Map map = yaml.loadAs(fileInputStream, Map.class);//装载的对象，这里使用Map, 当然也可使用自己写的对象
            System.out.println(map.get("resource"));
        }catch(FileNotFoundException e) {
            Log.Error("解析配置文件","找不到配置文件");
            e.printStackTrace();
        }finally {
            try {
                if(fileInputStream!=null)  fileInputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
