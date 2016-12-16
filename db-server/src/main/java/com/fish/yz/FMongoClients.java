package com.fish.yz;

import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.Block;
import com.mongodb.MongoCommandException;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.*;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.netty.channel.ChannelHandlerContext;
import org.bson.*;

import java.util.ArrayList;

/**
 * Created by xscn2426 on 2016/12/7.
 *
 */
public class FMongoClients {
	public MongoClient client;
	private static FMongoClients ins;

	public static FMongoClients instance(){
		if(ins == null)
			ins = new FMongoClients();
		return ins;
	}

	private FMongoClients(){
	}

	public void connectMongo(){
		client = MongoClients.create("mongodb://localhost/?streamType=netty");
	}

	public void handleOp(Protocol.DBMessage request, ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
		switch (request.getOp()){
			case CreateCollectionRequest:
				createCollection(request, ctx);
				break;
			case CountDocRequest:
				countDoc(request, ctx);
				break;
			case InsertDocRequest:
				insertDoc(request, ctx);
				break;
			case DeleteDocRequest:
				deleteDoc(request, ctx);
				break;
			case UpdateDocRequest:
				updateDoc(request, ctx);
				break;
			case FindAndModifyDocRequest:
				findAndModifyDoc(request, ctx);
				break;
			case FindDocRequest:
				findDoc(request, ctx);
				break;
		}
	}

	private void sendCallback(final Protocol.DBMessage reply, ChannelHandlerContext ctx) {
		System.out.println("return callback to the requester " + reply);
		Protocol.Request.Builder rb = Protocol.Request.newBuilder();
		rb.setCmdId(Protocol.Request.CmdIdType.DBMessage);
		rb.setExtension(Protocol.DBMessage.request, reply);
		ctx.channel().writeAndFlush(rb.build());
		try{
			switch (reply.getOp()){
				case CreateCollectionReply:
					System.out.println("callback " + Protocol.CreateCollectionReply.parseFrom(reply.getParameters()));
					break;
				case CountDocReply:
					System.out.println("callback " + Protocol.CountDocReply.parseFrom(reply.getParameters()));
					break;
				case InsertDocReply:
					System.out.println("callback " + Protocol.InsertDocReply.parseFrom(reply.getParameters()));
					break;
				case UpdateDocReply:
					System.out.println("callback " + Protocol.UpdateDocReply.parseFrom(reply.getParameters()));
					break;
				case DeleteDocReply:
					System.out.println("callback " + Protocol.DeleteDocReply.parseFrom(reply.getParameters()));
					break;
				case FindAndModifyDocReply:
					System.out.println("callback " + Protocol.FindAndModifyDocReply.parseFrom(reply.getParameters()));
					break;
				case FindDocReply:
					System.out.println("callback " + Protocol.FindDocReply.parseFrom(reply.getParameters()));
					break;
			}
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
	}

	private void createCollection(final Protocol.DBMessage request, final ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
		final Protocol.CreateCollectionRequest ccr = Protocol.CreateCollectionRequest.parseFrom(request.getParameters());
		String db_name = ccr.getDb().toStringUtf8();
		String collection_name = ccr.getCollection().toStringUtf8();
		client.getDatabase(db_name).createCollection(collection_name, new SingleResultCallback<Void>() {
			public void onResult(Void result, Throwable t) {
				System.out.println("createCollection Operation Finished!");
				System.out.println("result: " + t);
				Protocol.CreateCollectionReply.CrateCollectionStatus status = Protocol.CreateCollectionReply.CrateCollectionStatus.CREATE_SUCC;
				if (t != null){
					if (t instanceof MongoCommandException && ((MongoCommandException) t).getErrorCode() == 48) {
						System.out.println("code: " + ((MongoCommandException) t).getErrorCode());
						status = Protocol.CreateCollectionReply.CrateCollectionStatus.ALREADY_EXISTED;
					} else {
						status = Protocol.CreateCollectionReply.CrateCollectionStatus.CREATE_FAILED;
					}
				}
				if (ccr.hasCallbackId()){
					Protocol.CreateCollectionReply.Builder ccb = Protocol.CreateCollectionReply.newBuilder();
					ccb.setCallbackId(ccr.getCallbackId());
					ccb.setStatus(status);
					Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
					dbb.setOp(Protocol.DBMessage.OpType.CreateCollectionReply);
					dbb.setParameters(ccb.build().toByteString());
					sendCallback(dbb.build(), ctx);
				}
			}
		});
	}

	private void countDoc(final Protocol.DBMessage request, final ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
		final Protocol.CountDocRequest ccr = Protocol.CountDocRequest.parseFrom(request.getParameters());
		String db_name = ccr.getDb().toStringUtf8();
		String collection_name = ccr.getCollection().toStringUtf8();
		String query = ccr.getQuery().toStringUtf8();
		Document queryDocument = Document.parse(query);
		client.getDatabase(db_name).getCollection(collection_name).count(queryDocument, new SingleResultCallback<Long>() {
			public void onResult(Long result, Throwable t) {
				if (ccr.hasCallbackId()){
					Protocol.CountDocReply.Builder ccb = Protocol.CountDocReply.newBuilder();
					ccb.setCallbackId(ccr.getCallbackId());
					if (t != null) {
						ccb.setStatus(false);
						ccb.setCount(0);
					}
					else {
						ccb.setStatus(true);
						ccb.setCount(result.intValue());
					}
					Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
					dbb.setOp(Protocol.DBMessage.OpType.CountDocReply);
					dbb.setParameters(ccb.build().toByteString());
					sendCallback(dbb.build(), ctx);
				}
			}
		});
	}

	private void findDoc(final Protocol.DBMessage request, final ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
		final Protocol.FindDocRequest ccr = Protocol.FindDocRequest.parseFrom(request.getParameters());
		String db_name = ccr.getDb().toStringUtf8();
		String collection_name = ccr.getCollection().toStringUtf8();
		String query = ccr.getQuery().toStringUtf8();
		Document queryDocument = Document.parse(query);

		SingleResultCallback<ArrayList<Document>> callback = new SingleResultCallback<ArrayList<Document>>() {
			public void onResult(ArrayList<Document> result, Throwable t) {
				if (ccr.hasCallbackId()) {
					Protocol.FindDocReply.Builder ccb = Protocol.FindDocReply.newBuilder();
					ccb.setCallbackId(ccr.getCallbackId());
					if (t != null || result.size() == 0) {
						ccb.setStatus(false);
						ccb.addDocs(ByteString.copyFromUtf8(""));
					} else {
						ccb.setStatus(true);
						for (int i = 0; i < result.size(); i++){
							ccb.addDocs(ByteString.copyFromUtf8(result.get(i).toJson()));
						}
					}
					Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
					dbb.setOp(Protocol.DBMessage.OpType.FindDocReply);
					dbb.setParameters(ccb.build().toByteString());
					sendCallback(dbb.build(), ctx);
				}
			}
		};

		if (ccr.hasFields()){
			String proj = ccr.getFields().toStringUtf8();
			Document projDocument = Document.parse(proj);
			if (ccr.hasLimit()){
				int limit = ccr.getLimit();
				client.getDatabase(db_name).getCollection(collection_name).find(queryDocument).projection(projDocument).limit(limit).into(new ArrayList<Document>(), callback);
			} else {
				client.getDatabase(db_name).getCollection(collection_name).find(queryDocument).projection(projDocument).into(new ArrayList<Document>(), callback);
			}
		} else {
			client.getDatabase(db_name).getCollection(collection_name).find(queryDocument).into(new ArrayList<Document>(), callback);
		}
	}

	private void updateDoc(final Protocol.DBMessage request, final ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
		final Protocol.UpdateDocRequest ccr = Protocol.UpdateDocRequest.parseFrom(request.getParameters());
		String db_name = ccr.getDb().toStringUtf8();
		String collection_name = ccr.getCollection().toStringUtf8();
		String query = ccr.getQuery().toStringUtf8();
		Document queryDocument = Document.parse(query);
		String update = ccr.getDoc().toStringUtf8();
		Document updateDocument = Document.parse(update);

		UpdateOptions option = new UpdateOptions().upsert(false);
		if (ccr.hasUpset()){
			boolean upsert = ccr.getUpset();
			option = new UpdateOptions().upsert(upsert);
		}

		SingleResultCallback<UpdateResult> callback = new SingleResultCallback<UpdateResult>() {
			public void onResult(UpdateResult result, Throwable t) {
				if (ccr.hasCallbackId()){
					Protocol.UpdateDocReply.Builder ccb = Protocol.UpdateDocReply.newBuilder();
					ccb.setCallbackId(ccr.getCallbackId());
					if (t != null) {
						ccb.setStatus(false);
					}
					else {
						ccb.setStatus(true);
					}
					Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
					dbb.setOp(Protocol.DBMessage.OpType.UpdateDocReply);
					dbb.setParameters(ccb.build().toByteString());
					sendCallback(dbb.build(), ctx);
				}
			}
		};

		if (ccr.hasMulti()){
			client.getDatabase(db_name).getCollection(collection_name).updateMany(queryDocument, updateDocument, option, callback);
		} else {
			client.getDatabase(db_name).getCollection(collection_name).updateOne(queryDocument, updateDocument, option, callback);
		}
	}

	private void insertDoc(final Protocol.DBMessage request, final ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
		final Protocol.InsertDocRequest ccr = Protocol.InsertDocRequest.parseFrom(request.getParameters());
		String db_name = ccr.getDb().toStringUtf8();
		String collection_name = ccr.getCollection().toStringUtf8();
		String insert = ccr.getDoc().toStringUtf8();
		Document insertDocument = Document.parse(insert);

		client.getDatabase(db_name).getCollection(collection_name).insertOne(insertDocument, new SingleResultCallback<Void>() {
			public void onResult(Void result, Throwable t) {
				if (ccr.hasCallbackId()){
					Protocol.InsertDocReply.Builder ccb = Protocol.InsertDocReply.newBuilder();
					ccb.setCallbackId(ccr.getCallbackId());
					if (t != null) {
						ccb.setStatus(false);
					}
					else {
						ccb.setStatus(true);
					}
					Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
					dbb.setOp(Protocol.DBMessage.OpType.InsertDocReply);
					dbb.setParameters(ccb.build().toByteString());
					sendCallback(dbb.build(), ctx);
				}
			}
		});
	}

	private void deleteDoc(final Protocol.DBMessage request, final ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
		final Protocol.DeleteDocRequest ccr = Protocol.DeleteDocRequest.parseFrom(request.getParameters());
		String db_name = ccr.getDb().toStringUtf8();
		String collection_name = ccr.getCollection().toStringUtf8();
		String insert = ccr.getQuery().toStringUtf8();
		Document insertDocument = Document.parse(insert);

		client.getDatabase(db_name).getCollection(collection_name).deleteOne(insertDocument, new SingleResultCallback<DeleteResult>() {
			public void onResult(DeleteResult result, Throwable t) {
				if (ccr.hasCallbackId()){
					Protocol.DeleteDocReply.Builder ccb = Protocol.DeleteDocReply.newBuilder();
					ccb.setCallbackId(ccr.getCallbackId());
					if (t != null) {
						ccb.setStatus(false);
					}
					else {
						ccb.setStatus(true);
					}
					Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
					dbb.setOp(Protocol.DBMessage.OpType.DeleteDocReply);
					dbb.setParameters(ccb.build().toByteString());
					sendCallback(dbb.build(), ctx);
				}
			}
		});
	}

	private void findAndModifyDoc(final Protocol.DBMessage request, final ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
		final Protocol.FindAndModifyDocRequest ccr = Protocol.FindAndModifyDocRequest.parseFrom(request.getParameters());
		String db_name = ccr.getDb().toStringUtf8();
		String collection_name = ccr.getCollection().toStringUtf8();
		String query = ccr.getQuery().toStringUtf8();
		Document queryDocument = Document.parse(query);

		SingleResultCallback<Document> callback = new SingleResultCallback<Document>() {
			public void onResult(Document result, Throwable t) {
				if (ccr.hasCallbackId()){
					Protocol.FindAndModifyDocReply.Builder ccb = Protocol.FindAndModifyDocReply.newBuilder();
					ccb.setCallbackId(ccr.getCallbackId());
					if (t != null || result == null) {
						ccb.setStatus(false);
						ccb.setDoc(ByteString.copyFromUtf8(""));
					}
					else {
						ccb.setStatus(true);
						ccb.setDoc(ByteString.copyFromUtf8(result.toJson()));
					}
					Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
					dbb.setOp(Protocol.DBMessage.OpType.FindAndModifyDocReply);
					dbb.setParameters(ccb.build().toByteString());
					sendCallback(dbb.build(), ctx);
				}
			}
		};

		if (ccr.hasUpdate()){
			FindOneAndUpdateOptions option = new FindOneAndUpdateOptions().upsert(false);
			if (ccr.hasUpset()){
				boolean upsert = ccr.getUpset();
				option = new FindOneAndUpdateOptions().upsert(upsert);
			}
			if (ccr.getRettype())
				option.returnDocument(ReturnDocument.AFTER);
			else
				option.returnDocument(ReturnDocument.BEFORE);
			String update = ccr.getUpdate().toStringUtf8();
			Document updateDocument = Document.parse(update);
			client.getDatabase(db_name).getCollection(collection_name).findOneAndUpdate(queryDocument, updateDocument, option, callback);
		} else if (ccr.hasReplace()){
			FindOneAndReplaceOptions option = new FindOneAndReplaceOptions().upsert(false);
			if (ccr.hasUpset()){
				boolean upsert = ccr.getUpset();
				option = new FindOneAndReplaceOptions().upsert(upsert);
			}
			if (ccr.getRettype())
				option.returnDocument(ReturnDocument.AFTER);
			else
				option.returnDocument(ReturnDocument.BEFORE);
			String replace = ccr.getReplace().toStringUtf8();
			Document replaceDocument = Document.parse(replace);
			client.getDatabase(db_name).getCollection(collection_name).findOneAndReplace(queryDocument, replaceDocument, option, callback);
		} else if (ccr.hasRemove()){
			client.getDatabase(db_name).getCollection(collection_name).findOneAndDelete(queryDocument, callback);
		}
	}

	public void close(){
		client.close();
	}
}
