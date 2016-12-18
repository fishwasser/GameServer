package com.fish.yz;

import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.ByteString;
import org.bson.types.ObjectId;

/**
 * Created by xscn2426 on 2016/12/13.
 * 一个自动处理callback封装的类，如果自己重写的话要自己处理callback的加载与释放
 */
public abstract class DefaultCallBack implements CallBack {
	public ObjectId id;
	public ByteString bsid;
	public Protocol.Request.CmdIdType careType = Protocol.Request.CmdIdType.DBMessage;

    public DefaultCallBack(){
        this(ObjectId.get(), Protocol.Request.CmdIdType.DBMessage);
    }

	public DefaultCallBack(Protocol.Request.CmdIdType careType){
	    this(ObjectId.get(), careType);
	}

	public DefaultCallBack(ObjectId id, Protocol.Request.CmdIdType careType){
	    this.careType = careType;
		this.id = id;
		this.bsid = ByteString.copyFromUtf8(this.id.toString());
		Repo.instance().callbacks.put(this.bsid, this);
	}

	public void onResult(Protocol.Request request) {
		Repo.instance().callbacks.remove(this.bsid);
		switch (careType){
            case DBMessage:
                onCompleted(request.getExtension(Protocol.DBMessage.request));
            case FunctionalMessage:
                onCompleted(request.getExtension(Protocol.FunctionalMessage.request));
        }
	}

	public void onCompleted(Protocol.DBMessage msg){
    }

    public void onCompleted(Protocol.FunctionalMessage msg){
    }

    public void onCompleted(Protocol.EntityMessage msg){
    }

	public ByteString getId(){
		return this.bsid;
	}
}
