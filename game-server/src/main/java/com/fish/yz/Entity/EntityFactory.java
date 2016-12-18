package com.fish.yz.Entity;

import com.fish.yz.Center.HomeCell;
import com.fish.yz.Center.RoomCenter;
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
		if (id == null)
			id = ObjectId.get();

		System.out.println("create entity " + entityName + " with id : " + id);

		if ("ServerBoot".equals(entityName)){
			return new ServerBoot(id);
		}
		if ("ServerAvatar".equals(entityName)){
			return new ServerAvatar(id);
		}
		if ("RoomCenter".equals(entityName)){
		    return new RoomCenter(id);
        }
        if ("HomeCell".equals(entityName)){
            return new HomeCell(id);
        }

		return null;
	}
}
