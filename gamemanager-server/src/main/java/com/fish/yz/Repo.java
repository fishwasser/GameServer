package com.fish.yz;

import com.fish.yz.protobuf.Protocol;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fishman on 4/12/2016.
 * 资源存放类
 */
public class Repo {
    public static AttributeKey<ServerInfoHolder> serverInfoKey = AttributeKey.valueOf("serverInfo");

    public Map<ServerInfoHolder, Channel> games = new ConcurrentHashMap<ServerInfoHolder, Channel>();
    public Map<ServerInfoHolder, Channel> gates = new ConcurrentHashMap<ServerInfoHolder, Channel>();
    public Map<ServerInfoHolder, Channel> dbs = new ConcurrentHashMap<ServerInfoHolder, Channel>();
    public Map<String, Protocol.EntityMailbox> entities = new ConcurrentHashMap<String, Protocol.EntityMailbox>();

    private static Repo ins = null;

    public static Repo instance(){
        if(ins == null)
            ins = new Repo();
        return ins;
    }

    private Repo(){}

	public synchronized void removeGame(ServerInfoHolder holder){
		this.games.remove(holder);
		Iterator<Map.Entry<String, Protocol.EntityMailbox>> it = this.entities.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry<String, Protocol.EntityMailbox> entry = it.next();
			if (entry.getValue().getServerinfo().getIp().toStringUtf8().equals(holder.si.getIp().toStringUtf8()) && entry.getValue().getServerinfo().getPort() == holder.si.getPort()){
				it.remove();
			}
		}
	}
}
