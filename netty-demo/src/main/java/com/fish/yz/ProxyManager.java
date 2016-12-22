package com.fish.yz;

import io.netty.channel.Channel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xscn2426 on 2016/11/23.
 * 代理器管理类
 */
public class ProxyManager {
	// client id : GameOiO
	public Map<String, GameOiOClient> clientGameMap = new ConcurrentHashMap<String, GameOiOClient>();
	// client id : channel
	public Map<String, Channel> clientChannelMap = new ConcurrentHashMap<String, Channel>();

	private static ProxyManager ins;

	public static ProxyManager instance(){
		if (ins == null){
			ins = new ProxyManager();
		}
		return ins;
	}

	private ProxyManager(){
	}

	public Channel getChannelByClientId(String clientId){
		return clientChannelMap.get(clientId);
	}

	public synchronized void removeChannelByClientId(String clientId){
		if (clientChannelMap.containsKey(clientId))
			clientChannelMap.remove(clientId);
	}

	public synchronized void setChannelClientId(String clientId, Channel channel){
		System.out.println("set channel for " + clientId);
		clientChannelMap.put(clientId, channel);
	}

	public GameOiOClient getGameByClientId(String clientId){
		return clientGameMap.get(clientId);
	}

	public synchronized void setGameClientId(String clientId, GameOiOClient client){
		clientGameMap.put(clientId, client);
	}

	public synchronized void removeGameByClientId(String clientId){
		if (clientGameMap.containsKey(clientId))
			clientGameMap.remove(clientId);
	}

	/**
	 * 申请一个 GameOiOClient
	 * @param clientId
	 * @return GameOiOClient
	 */
	public GameOiOClient applyGameClient(String clientId){
		return GameOiOClientsMgr.instance().applyOneClient();
	}

}
