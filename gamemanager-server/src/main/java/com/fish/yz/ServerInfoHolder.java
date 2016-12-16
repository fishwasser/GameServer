package com.fish.yz;


import com.fish.yz.protobuf.Protocol;

/**
 * Created by xscn2426 on 2016/11/23.
 * serer 信息的持有者
 */
public class ServerInfoHolder {
	public enum ServerType{
		Gate,
		Game,
		Db,
	}

	public String ip;
	public int port;
	public ServerType type;
	public Protocol.ServerInfo si;

	public ServerInfoHolder(Protocol.ServerInfo si){
		this.si = si;
		this.ip = si.getIp().toStringUtf8();
		this.port = si.getPort();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (getClass() != obj.getClass())
			return false;
		ServerInfoHolder o = (ServerInfoHolder)obj;
		return o.ip.equals(this.ip) && o.port == this.port;
	}

	@Override
	public int hashCode() {
		return this.ip.hashCode() + this.port;
	}
}
