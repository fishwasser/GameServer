package com.fish.yz;

import io.netty.channel.Channel;

import java.util.*;

/**
 * Created by xscn2426 on 2016/11/23.
 * 代理器管理类
 */
public class ProxyManager {
	// client id : GameOiO
	public Map<String, GameOiOClient> clientGameMap = new HashMap<String, GameOiOClient>();
	// client id : channel
	public Map<String, Channel> clientChannelMap = new HashMap<String, Channel>();

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

	public void removeChannelByClientId(String clientId){
		if (clientChannelMap.containsKey(clientId))
			clientChannelMap.remove(clientId);
	}

	public void setChannelClientId(String clientId, Channel channel){
		System.out.println("set channel for " + clientId);
		clientChannelMap.put(clientId, channel);
	}

	public GameOiOClient getGameByClientId(String clientId){
		return clientGameMap.get(clientId);
	}

	public void setGameClientId(String clientId, GameOiOClient client){
		clientGameMap.put(clientId, client);
	}

	public void removeGameByClientId(String clientId){
		if (clientGameMap.containsKey(clientId))
			clientGameMap.remove(clientId);
	}
	/**
	 * 申请一个　GameOiOClient
	 * @param clientId
	 * @return
	 */
	public GameOiOClient applyGameClient(String clientId){
		return GameOiOClientsMgr.instance().applyOneClient();
	}

}
