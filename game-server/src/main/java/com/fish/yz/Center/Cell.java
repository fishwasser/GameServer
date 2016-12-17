package com.fish.yz.Center;

import com.fish.yz.Entity.ServerEntity;
import com.fish.yz.Repo;
import com.fish.yz.protobuf.Protocol;
import com.fish.yz.util.GameAPI;
import org.bson.types.ObjectId;

/**
 * Created by fishman on 17/12/2016.
 * Cell 的基类
 */
public class Cell extends ServerEntity {
    private String centerName;

    public Cell(){
        this(ObjectId.get());
    }

    public Cell(ObjectId entityId){
        super(entityId);
    }

    public void startConnect(String centerName){
        this.centerName = centerName;
        Protocol.EntityMailbox centerMb = GameAPI.getGlobalEntityMailbox(centerName);
        Protocol.EntityMailbox.Builder thisMb = Protocol.EntityMailbox.newBuilder();
        thisMb.setEntityid(this.getId());
        thisMb.setServerinfo(Repo.instance().serverInfo);

        if (centerMb != null){
            this.callServerMethod(centerMb, this, "regCell", thisMb.toString(), null);
        } else {
            //todo: 还未完成注册等等
        }

    }
}
