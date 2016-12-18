package com.fish.yz.Entity;


import com.fish.yz.DefaultCallBack;
import com.fish.yz.protobuf.Protocol;
import com.fish.yz.util.GameAPI;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xscn2426 on 2016/12/9.
 * 游戏启动负责配置、启动组件的功能
 */
public class ServerStart {
    Map<String, String> gameCenter = new HashMap<String, String>();
    Map<String, String> gameCell = new HashMap<String, String>();
    Map<String, Boolean> entityState = new HashMap<String, Boolean>();

    private boolean started = false;
    private String game;

	public ServerStart(String game){
	    // game1上启动RoomCenter
	    gameCenter.put("game1", "RoomCenter");
	    // game2上启动HomeCell
	    gameCell.put("game2", "HomeCell");

	    this.game = game;
	}

    /**
     * 开启配置功能
     */
	public void startGame(){
	    final DefaultCallBack cb = new DefaultCallBack() {
            @Override
            public void onCompleted(Protocol.FunctionalMessage msg) {
                try {
                    System.out.println("reg entity back : " + msg);

                    Protocol.GmReturnVal ret = Protocol.GmReturnVal.parseFrom(msg.getParameters());
                    if (ret.getReturnStatus()){
                        String name = ret.getReturnVal().toStringUtf8();
                        if (entityState.containsKey(name)) {
                            entityState.put(name, true);
                            for(boolean state : entityState.values()){
                                if (!state) {
                                    started = false;
                                    return;
                                }
                            }
                            started = true;
                        }
                    } else {
                        System.out.println("reg entity error : " + ret.getReturnVal().toStringUtf8());
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        };

	    // 实例化entity并注册到全局
        for (Map.Entry<String, String> entry : gameCenter.entrySet()){
            if (this.game.equals(entry.getKey())){
                ServerEntity entity = GameAPI.createEntityLocally(entry.getValue(), null);
                GameAPI.registerEntityGlobally(entry.getValue(), entity, cb);
                entityState.put(entry.getKey(), false);
            }
        }
        for (Map.Entry<String, String> entry : gameCell.entrySet()){
            if (this.game.equals(entry.getKey())){
                ServerEntity entity = GameAPI.createEntityLocally(entry.getValue(), null);
                GameAPI.registerEntityGlobally(entry.getValue(), entity, cb);
                entityState.put(entry.getKey(), false);
            }
        }
    }

    public boolean hasStarted(){
	    return started;
    }

}
