package com.fish.yz;

import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.ByteString;

/**
 * Created by xscn2426 on 2016/12/13.
 * 所有返回的接口
 */
public interface CallBack {
	void onResult(Protocol.Request request);
	ByteString getId();
}
