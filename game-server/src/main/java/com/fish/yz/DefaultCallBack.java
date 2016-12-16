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

	public DefaultCallBack(){
		this(ObjectId.get());
	}

	public DefaultCallBack(ObjectId id){
		this.id = id;
		this.bsid = ByteString.copyFromUtf8(this.id.toString());
		Repo.instance().callbacks.put(this.bsid, this);
	}

	public void onResult(Protocol.Request request) {
		Repo.instance().callbacks.remove(this.bsid);
		Protocol.DBMessage msg = request.getExtension(Protocol.DBMessage.request);
		onCompleted(msg);
	}

	public abstract void onCompleted(Protocol.DBMessage msg);



	public ByteString getId(){
		return this.bsid;
	}
}
