package com.fish.yz.util;

import com.fish.yz.Repo;
import com.fish.yz.protobuf.Protocol;

/**
 * Created by fishman on 17/12/2016.
 * 提供game上的一些方法的访问、或者一些工具函数等功能
 */
public class GameAPI {

    public static Protocol.EntityMailbox getGlobalEntityMailbox(String entityName){
        return Repo.instance().entities.get(entityName);
    }

}
