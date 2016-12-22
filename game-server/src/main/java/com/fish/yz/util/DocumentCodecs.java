package com.fish.yz.util;

import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.ByteString;
import org.bson.BsonReader;
import org.bson.Document;
import org.bson.codecs.Decoder;
import org.bson.codecs.DecoderContext;

import java.util.Map;

/**
 * Created by xscn2426 on 2016/12/20.
 * 把特定类参数 改成 document 格式
 */
public class DocumentCodecs {

	public static String encoder(Object obj){
		if (obj instanceof Protocol.ServerInfo){
			Protocol.ServerInfo si = (Protocol.ServerInfo)obj;
			return String.format("{\"$si\" : {'ip': '%s', 'port': %d}}", si.getIp().toStringUtf8(), si.getPort());
		}
		return "{}";
	}

	public static Object decode(Document doc) {
		// ServerInfo 解码
		Map<Object, Object> m = (Map<Object, Object>)doc.get("$si");
		if (m != null){
			Protocol.ServerInfo.Builder sibb = Protocol.ServerInfo.newBuilder();
			sibb.setIp(ByteString.copyFromUtf8((String)m.get("ip")));
			sibb.setPort((Integer) m.get("port"));
			return sibb.build();
		}
		return null;
	}

}
