package com.fish.yz;

import com.fish.yz.protobuf.Protocol;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fishman on 4/12/2016.
 * 资源存放类
 */
public class Repo {
    public static AttributeKey<ServerInfoHolder> serverInfoKey = AttributeKey.valueOf("serverInfo");

    public Map<ServerInfoHolder, Channel> games = new HashMap<ServerInfoHolder, Channel>();
    public Map<ServerInfoHolder, Channel> gates = new HashMap<ServerInfoHolder, Channel>();
    public Map<ServerInfoHolder, Channel> dbs = new HashMap<ServerInfoHolder, Channel>();
    public Map<String, Protocol.EntityMailbox> entities = new HashMap<String, Protocol.EntityMailbox>();

    private static Repo ins = null;

    public static Repo instance(){
        if(ins == null)
            ins = new Repo();
        return ins;
    }

    private Repo(){}
}
