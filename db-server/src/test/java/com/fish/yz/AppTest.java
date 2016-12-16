package com.fish.yz;

import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.types.ObjectId;

/**
 * Entity test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
        FMongoClients client = FMongoClients.instance();


        Protocol.Request.Builder cb = Protocol.Request.newBuilder();
        cb.setCmdId(Protocol.Request.CmdIdType.ConnectServerReply);
        Protocol.ConnectServerRequest.Builder csb = Protocol.ConnectServerRequest.newBuilder();
        csb.setType(Protocol.ConnectServerRequest.RequestType.NEW_CONNECTION);
        BsonDocument doc = new BsonDocument().append("a", new BsonString("hello"));
        csb.setDeviceid(ByteString.copyFromUtf8(doc.toJson()));
        cb.setExtension(Protocol.ConnectServerRequest.request, csb.build());
        Protocol.Request r = cb.build();

        BsonDocument document = BsonDocument.parse(csb.build().getDeviceid().toStringUtf8());
        System.out.println(document);

        System.out.println(r);

        System.out.println("=====================Junit======================");
        // 创建集合
		/*Protocol.CreateCollectionRequest.Builder ccb = Protocol.CreateCollectionRequest.newBuilder();
	    ccb.setCallbackId(ByteString.copyFromUtf8(ObjectId.get().toString()));
	    ccb.setDb(ByteString.copyFromUtf8("fish"));
	    ccb.setCollection(ByteString.copyFromUtf8("test"));
	    Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
	    dbb.setOp(Protocol.DBMessage.OpType.CreateCollectionRequest);
	    dbb.setParameters(ccb.build().toByteString());*/

        // 插入数据
	    /*Protocol.InsertDocRequest.Builder ccb = Protocol.InsertDocRequest.newBuilder();
	    ccb.setCallbackId(ByteString.copyFromUtf8(ObjectId.get().toString()));
	    ccb.setDb(ByteString.copyFromUtf8("fish"));
	    ccb.setCollection(ByteString.copyFromUtf8("test"));
	    ccb.setDoc(ByteString.copyFromUtf8("{'entity_class': 'avatar', 'account':'fish', 'password':'nimei', 'coin_a':1000}"));
	    Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
	    dbb.setOp(Protocol.DBMessage.OpType.InsertDocRequest);
	    dbb.setParameters(ccb.build().toByteString());*/

        // 删除数据
	    /*Protocol.DeleteDocRequest.Builder ccb = Protocol.DeleteDocRequest.newBuilder();
	    ccb.setCallbackId(ByteString.copyFromUtf8(ObjectId.get().toString()));
	    ccb.setDb(ByteString.copyFromUtf8("fish"));
	    ccb.setCollection(ByteString.copyFromUtf8("test"));
	    ccb.setQuery(ByteString.copyFromUtf8("{'entity_class': 'avatar', 'account':'fish', 'password':'nimei', 'coin_a':1000}"));
	    Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
	    dbb.setOp(Protocol.DBMessage.OpType.DeleteDocRequest);
	    dbb.setParameters(ccb.build().toByteString());*/

        // 更新数据
	    /*Protocol.UpdateDocRequest.Builder ccb = Protocol.UpdateDocRequest.newBuilder();
	    ccb.setCallbackId(ByteString.copyFromUtf8(ObjectId.get().toString()));
	    ccb.setDb(ByteString.copyFromUtf8("fish"));
	    ccb.setCollection(ByteString.copyFromUtf8("test"));
	    ccb.setQuery(ByteString.copyFromUtf8("{'entity_class': 'avatar', 'account':'fish', 'password':'nimei', 'coin_a':1000}"));
	    ccb.setDoc(ByteString.copyFromUtf8("{$inc: {'coin_a':2000} }"));
	    Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
	    dbb.setOp(Protocol.DBMessage.OpType.UpdateDocRequest);
	    dbb.setParameters(ccb.build().toByteString());*/

        // 查找数据
	    /*Protocol.FindDocRequest.Builder ccb = Protocol.FindDocRequest.newBuilder();
	    ccb.setCallbackId(ByteString.copyFromUtf8(ObjectId.get().toString()));
	    ccb.setDb(ByteString.copyFromUtf8("fish"));
	    ccb.setCollection(ByteString.copyFromUtf8("test"));
	    ccb.setQuery(ByteString.copyFromUtf8("{'entity_class': 'avatar', 'account':'fish', 'password':'nimei', 'coin_a':3000}"));
	    ccb.setFields(ByteString.copyFromUtf8("{'entity_class': 1, 'account':1}"));
	    Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
	    dbb.setOp(Protocol.DBMessage.OpType.FindDocRequest);
	    dbb.setParameters(ccb.build().toByteString());*/

        // 计数数据
	    /*Protocol.CountDocRequest.Builder ccb = Protocol.CountDocRequest.newBuilder();
	    ccb.setCallbackId(ByteString.copyFromUtf8(ObjectId.get().toString()));
	    ccb.setDb(ByteString.copyFromUtf8("fish"));
	    ccb.setCollection(ByteString.copyFromUtf8("test"));
	    ccb.setQuery(ByteString.copyFromUtf8("{'entity_class': 'avatar', 'account':'fish', 'password':'nimei', 'coin_a':3000}"));
	    Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
	    dbb.setOp(Protocol.DBMessage.OpType.CountDocRequest);
	    dbb.setParameters(ccb.build().toByteString());*/

        // 查找并更新数据
        Protocol.FindAndModifyDocRequest.Builder ccb = Protocol.FindAndModifyDocRequest.newBuilder();
        ccb.setCallbackId(ByteString.copyFromUtf8(ObjectId.get().toString()));
        ccb.setDb(ByteString.copyFromUtf8("fish"));
        ccb.setCollection(ByteString.copyFromUtf8("test"));
        ccb.setQuery(ByteString.copyFromUtf8("{'entity_class': 'avatar', 'account':'fish', 'password':'nimei', 'coin_a':2000}"));
        ccb.setRemove(false);
        ccb.setUpset(true);
        ccb.setRettype(true);
        Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
        dbb.setOp(Protocol.DBMessage.OpType.FindAndModifyDocRequest);
        dbb.setParameters(ccb.build().toByteString());

        try {
            client.handleOp(dbb.build(), null);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        while (true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //client.close();
    }
}
