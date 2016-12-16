package com.fish.yz;

import com.fish.yz.protobuf.Protocol;
import io.netty.util.AttributeKey;

/**
 * Created by xscn2426 on 2016/11/24.
 * 资源类
 */
public class Repo {
	public static AttributeKey<Protocol.ClientInfo> clientInfoKey = AttributeKey.valueOf("clientInfo");
	public static AttributeKey<GameOiOClient> clientKey = AttributeKey.valueOf("clientKey");
}
