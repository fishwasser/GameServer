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
		return String.format("{%s}", encoderInter(obj));
	}

	/**
	 * 不包含外{}
	 * @param obj
	 * @return
	 */
	private static String encoderInter(Object obj){
		if (obj instanceof Protocol.ServerInfo){
			Protocol.ServerInfo si = (Protocol.ServerInfo)obj;
			return String.format("'$si' : {'ip': '%s', 'port': %d}", si.getIp().toStringUtf8(), si.getPort());
		} else if (obj instanceof Protocol.EntityMailbox){
			Protocol.EntityMailbox emb = (Protocol.EntityMailbox)obj;
			if (emb.hasServerinfo())
				return String.format("'$emb' : {'entityid': '%s', %s}", emb.getEntityid().toStringUtf8(), encoderInter(emb.getServerinfo()));
			else
				return String.format("'$emb' : {'entityid': '%s'}", emb.getEntityid().toStringUtf8());
		}
		return "";
	}

	public static Object decode(Document doc) {
		// ServerInfo 解码
		if (doc.get("$si") != null){
			Document si = (Document)doc.get("$si");
			Protocol.ServerInfo.Builder sibb = Protocol.ServerInfo.newBuilder();
			sibb.setIp(ByteString.copyFromUtf8((String)si.get("ip")));
			sibb.setPort((Integer) si.get("port"));
			return sibb.build();
		}
		if (doc.get("$emb") != null){
			Document emb = (Document)doc.get("$emb");
			Protocol.EntityMailbox.Builder sibb = Protocol.EntityMailbox.newBuilder();
			sibb.setEntityid(ByteString.copyFromUtf8((String)emb.get("entityid")));
			if (emb.get("$si") != null)
				sibb.setServerinfo((Protocol.ServerInfo)decode(emb));
			return sibb.build();
		}
		return null;
	}

}
