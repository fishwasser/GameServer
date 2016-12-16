package com.fish.yz.Entity;

import org.bson.types.ObjectId;

/**
 * Created by xscn2426 on 2016/12/9.
 *
 */
public class EntityFactory {


	public static ServerEntity createEntity(String entityName){
		return createEntity(entityName, ObjectId.get());
	}

	public static ServerEntity createEntity(String entityName, ObjectId id){
		if ("ServerBoot".equals(entityName)){
			return new ServerBoot(id);
		}
		if ("ServerAvatar".equals(entityName)){
			return new ServerAvatar(id);
		}

		return null;
	}
}
