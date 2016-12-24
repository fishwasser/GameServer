package com.fish.yz.Center;

import com.fish.yz.Entity.ServerEntity;
import com.fish.yz.Repo;
import com.fish.yz.protobuf.Protocol;
import com.fish.yz.util.DocumentCodecs;
import com.fish.yz.util.GameAPI;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

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

    public void setCenterName(String centerName){
        this.centerName = centerName;
    }

    public void startConnect(){
        if (this.centerName == null || "".equals(this.centerName)){
            System.out.println("please set center name before connect!!");
            return;
        }
        Protocol.EntityMailbox centerMb = GameAPI.getGlobalEntityMailbox(centerName);
        Protocol.EntityMailbox.Builder thisMb = Protocol.EntityMailbox.newBuilder();
        thisMb.setEntityid(this.getId());
        thisMb.setServerinfo(Repo.instance().serverInfo);

        if (centerMb != null){
	        List<Object> list = new ArrayList<Object>();
	        list.add(DocumentCodecs.encoder(thisMb.build()));
            Document doc = new Document("p", list);
            //String param = String.format("{'id': \'%s\', 'ip': \'%s\', 'port': %d}", thisMb.build().getEntityid().toStringUtf8(), Repo.instance().serverInfo.getIp().toStringUtf8(), Repo.instance().serverInfo.getPort());
            this.callServerMethod(centerMb, this, "regCell", doc.toJson(), null);
        } else {
            //todo: 还未完成注册等等
            System.out.println("not get the center!!");
        }

    }
}
