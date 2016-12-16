package com.fish.yz.Entity;

import com.fish.yz.DbClient;
import com.fish.yz.DefaultCallBack;
import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.InvalidProtocolBufferException;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.print.Doc;

/**
 * Created by xscn2426 on 2016/12/9.
 * 启动控制类
 */
public class ServerBoot extends ServerEntity{
	private String account;
	private String password;
	private int uid;
	private String nickName;

	public ServerBoot(){
		this(ObjectId.get());
	}

	public ServerBoot(ObjectId entityId){
		super(entityId);

	}

	public void onBecomePlayer(){
		this.proxy.createEntity("ClientBoot", this.id);
		this.proxy.callClientMethod("becomePlayer", "{}", this.id);
	}

	public void login(Document doc){
        System.out.println("login" + doc);
        this.account = doc.getString("account");
        this.password = doc.getString("password");

        doLogin();
    }

    private void doLogin(){
		String query = String.format("{'account': \'%s\', 'password':\'%s\'}", this.account, this.password);
	    String update = String.format("{$set:{'account': \'%s\'}, $set:{'password':\'%s\'}}", this.account, this.password);
	    DbClient.instance().findAndModify("account", query, new DefaultCallBack() {

		    public void onCompleted(Protocol.DBMessage msg) {
			    String query = String.format("{'account': \'%s\', 'password':\'%s\'}", ServerBoot.this.account, ServerBoot.this.password);
			    String update = String.format("{$set:{'account': \'%s\'}, $set:{'password':\'%s\'}}", ServerBoot.this.account, ServerBoot.this.password);
			    DbClient.instance().findAndModify("entities", query, new DefaultCallBack() {
				    @Override
				    public void onCompleted(Protocol.DBMessage msg) {
					    ServerBoot.this.createAvatar(msg);
				    }
			    }, update, null, false, true, true);
		    }
	    }, update, null, false, true, true);
    }

    private void createAvatar(Protocol.DBMessage msg){
	    try {
		    Protocol.FindAndModifyDocReply fdr = Protocol.FindAndModifyDocReply.parseFrom(msg.getParameters());
		    if (!fdr.getStatus()){
			    System.out.println("create avatar, error " + msg);
		    } else {
			    System.out.println("create avatar, success " + msg);
				Document doc = Document.parse(fdr.getDoc().toStringUtf8());
				if (EntityManager.hasEntity(doc.getObjectId("_id"))){
					System.out.println("has entity");
				} else {
					ServerAvatar entity = (ServerAvatar) EntityFactory.createEntity("ServerAvatar");
					entity.account = account;
					entity.password = password;
					this.giveClientTo(entity);
					entity.proxy.destroyEntity(this.id);
					this.destroy();
				}

		    }
	    } catch (InvalidProtocolBufferException e) {
		    e.printStackTrace();
	    }

    }

}
