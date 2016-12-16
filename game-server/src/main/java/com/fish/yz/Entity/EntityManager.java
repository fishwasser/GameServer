package com.fish.yz.Entity;

import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xscn2426 on 2016/12/9.
 *
 */
public class EntityManager {
	private static Map<ObjectId, ServerEntity> _entities = new HashMap<ObjectId, ServerEntity>();

	public static boolean hasEntity(ObjectId entityId){
		return _entities.containsKey(entityId);
	}

	public static ServerEntity getEntity(ObjectId entityId){
		return _entities.get(entityId);
	}

	public static void delEntity(ObjectId entityId){
		_entities.remove(entityId);
	}

	public static void addEntity(ObjectId entityId, ServerEntity entity){
		if (_entities.containsKey(entityId))
			System.out.println("warning: entity manager already contains, " + entityId);
		_entities.put(entityId, entity);
	}

	public static Map<ObjectId, ServerEntity> allEntities(ObjectId entityId){
		return _entities;
	}

}
