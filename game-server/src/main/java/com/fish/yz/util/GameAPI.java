package com.fish.yz.util;

import com.fish.yz.CallBack;
import com.fish.yz.Entity.EntityFactory;
import com.fish.yz.Entity.ServerEntity;
import com.fish.yz.GameManagerClient;
import com.fish.yz.Repo;
import com.fish.yz.protobuf.Protocol;
import org.bson.types.ObjectId;

/**
 * Created by fishman on 17/12/2016.
 * 提供game上的一些方法的访问、或者一些工具函数等功能
 */
public class GameAPI {

    public static Protocol.EntityMailbox getGlobalEntityMailbox(String entityName){
        return Repo.instance().entities.get(entityName);
    }

    /**
     * 在本地的game server上创建entity
     * @param entityName
     * @param id
     */
    public static ServerEntity createEntityLocally(String entityName, ObjectId id){
        return EntityFactory.createEntity(entityName, id);
    }

    /**
     * 将entity注册到全局
     * @param entityName
     * @param entity
     * @param callback
     */
    public static void registerEntityGlobally(String entityName, ServerEntity entity, CallBack callback){
        GameManagerClient.instance().regEntity(entityName, entity, callback);
    }

}
