package com.fish.yz.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by xscn2426 on 2016/12/12.
 * 读取配置文件
 */
public class Config {
	String prop_path = "../db-server/src/main/java/com/fish/yz/config.properties";
	Properties prop = new Properties();
	InputStream input = null;

	private static Config ins;

	public static Config instance(){
		if (ins == null)
			ins = new Config();
		return ins;
	}

	private Config() {
		try {
			input = new FileInputStream(prop_path);
			prop.load(input);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String get(String key){
		return prop.getProperty(key);
	}

}
