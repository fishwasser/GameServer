package com.fish.yz;

import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.ByteString;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fishman on 4/12/2016.
 * 资源存放类
 */
public class Repo {
    public Map<String, ClientProxy> clients = new HashMap<String, ClientProxy>();
    public Map<ByteString, DefaultCallBack> callbacks = new HashMap<ByteString, DefaultCallBack>();
    public Map<String, Protocol.EntityMailbox> entities = new HashMap<String, Protocol.EntityMailbox>();

    public String ip;
    public int port;

    public Protocol.ServerInfo serverInfo;
    private static Repo ins = null;

    public static Repo instance(){
        if(ins == null)
            ins = new Repo();
        return ins;
    }

    private Repo(){}
}
