package com.fish.yz.Center;

import com.fish.yz.Entity.ServerEntity;
import com.fish.yz.protobuf.Protocol;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishman on 17/12/2016.
 * Center 的基类
 */
public class Center extends ServerEntity {
    private List<Protocol.EntityMailbox> cells = new ArrayList<Protocol.EntityMailbox>();

    public Center(){
        this(ObjectId.get());
    }

    public Center(ObjectId entityId){
        super(entityId);
    }

    public void regCell(Protocol.EntityMailbox mailbox){
        if (!this.cells.contains(mailbox)){
            this.cells.add(mailbox);
            System.out.println("register cell " + mailbox);
        }
    }


}
