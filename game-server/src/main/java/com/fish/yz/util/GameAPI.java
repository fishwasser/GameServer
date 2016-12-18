package com.fish.yz.util;

import com.fish.yz.CallBack;
import com.fish.yz.Entity.EntityFactory;
import com.fish.yz.Entity.ServerEntity;
import com.fish.yz.GameManagerClient;
import com.fish.yz.Repo;
import com.fish.yz.protobuf.Protocol;
import org.bson.types.ObjectId;

import java.lang.reflect.Method;

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

    /**
     * 循环向上转型, 获取对象的 DeclaredMethod
     * @param object : 子类对象
     * @param methodName : 父类中的方法名
     * @return 父类中的方法对象
     */
    public static Method getDeclaredMethod(Object object, String methodName, Class pclazz){
        Method method = null;
        for(Class<?> clazz = object.getClass() ; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName, pclazz) ;
                return method ;
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }

        return null;
    }
}
