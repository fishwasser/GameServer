package com.fish.yz.util;

import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.ByteString;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Test{

	public void hello(ObjectId id, List<ObjectId> list, Map<String, ObjectId> mapId){
		System.out.println("hello " + id + " , " + list + " , " + mapId);
	}

	public void hello2(Protocol.EntityMailbox si, boolean result){
		System.out.println("hello2 " + si + " , " + result);
	}

}

/**
 * Created by xscn2426 on 2016/12/20.
 * 反射工具类
 */
public class ReflectUtil {

	/**
	 * 循环向上转型, 获取对象的 DeclaredMethod
	 * @param object : 子类对象
	 * @param methodName : 父类中的方法名
	 * @param parameterTypes : 父类中的方法参数类型
	 * @return 父类中的方法对象
	 */
	public static Method getDeclaredMethod(Object object, String methodName, Class<?> ... parameterTypes){
		Method method = null ;
		for(Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
			try {
				method = clazz.getDeclaredMethod(methodName, parameterTypes) ;
				return method ;
			} catch (Exception e) {
			}
		}

		return null;
	}

	/**
	 * 直接调用对象方法, 而忽略修饰符(private, protected, default)
	 * @param object : 子类对象
	 * @param methodName : 父类中的方法名
	 * @param parameterTypes : 父类中的方法参数类型
	 * @param parameters : 父类中的方法参数
	 * @return 父类中方法的执行结果
	 */
	public static Object invokeMethod(Object object, String methodName, Class<?> [] parameterTypes,
	                                  Object [] parameters) {
		Method method = getDeclaredMethod(object, methodName, parameterTypes);
		try {
			if (null != method) {
				method.setAccessible(true);
				return method.invoke(object, parameters) ;
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 使用document解析函数参数, 不支持重载函数, 支持基本类型, 支持泛型等，特殊的参数需要自己写解析器
	 * document 使用 "p": [] 格式
	 * 特殊类型 支持 serverinfo、entitymailbox
	 * @param object
	 * @param methodName
	 * @param doc
	 * @param preDefined 预先设置的参数，不进入自动参数匹配流程
	 * @return
	 */
	public static void invokeParamMethod(Object object, String methodName, Document doc, Object... preDefined){
		List<Object> pList = (List<Object>)doc.get("p");

		//TODO: 可以对ServerEntity子类的所有方法进行cache
		Map<String, Method> methods = new HashMap<String, Method>();
		for(Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
			try {
				Method[] mth = clazz.getDeclaredMethods();
				for (Method m : mth){
					if (!methods.containsKey(m.getName()))
						methods.put(m.getName(), m);
				}
			} catch (Exception e) {
			}
		}

		if (methods.containsKey(methodName)){
			Method method = methods.get(methodName);
			Type[] paramTypeList = method.getGenericParameterTypes();
			Object[] param = new Object[paramTypeList.length];
			int i = 0;
			for (Object obj : preDefined){
				param[i] = obj;
				i++;
			}
			boolean skip = false;
			// 复杂的参数不容易解释的直接传document
			if (paramTypeList.length == preDefined.length + 1 && pList == null){
				param[i] = doc;
				skip = true;
			}
			if (!skip)
				for (; i < paramTypeList.length; i++){
					Type paramType = paramTypeList[i];
					if (pList.size() > i-preDefined.length)
						param[i] = decode(paramType, pList.get(i-preDefined.length));
					else
						param[i] = null;
				}
			try {
				method.invoke(object, param);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("not contain this method " + methodName);
		}
	}

	private static Object decode(Type paramType, Object p){
		if (paramType instanceof ParameterizedType) {       //如果是泛型类型
			ParameterizedType ptype = ((ParameterizedType) paramType);
			Type[] types = ptype.getActualTypeArguments();
			if (ptype.getRawType() == List.class && types.length == 1){
				List<Object> pList = (List<Object>)p;
				List<Object> ret = new ArrayList<Object>();
				int i = 0;
				for (Object obj : pList){
					ret.add(decode(types[0], obj));
					i++;
				}
				return ret;
			} else if (ptype.getRawType() == Map.class && types.length == 2) {
				Map<Object, Object> pMap = (Map<Object, Object>)p;
				Map<Object, Object> mret = new HashMap<Object, Object>();
				for (Map.Entry<Object, Object> entry : pMap.entrySet()){
					mret.put(decode(types[0], entry.getKey()), decode(types[1], entry.getValue()));
				}
				return mret;
			} else {
				System.out.println("暂时不支持这中类型参数, " + ptype.getRawType());
				return null;
			}
		}

		if (paramType == Protocol.ServerInfo.class){
			Document d = Document.parse((String)p);
			return DocumentCodecs.decode(d);
		} else if (paramType == Protocol.EntityMailbox.class){
			Document d = Document.parse((String)p);
			return DocumentCodecs.decode(d);
		} else if (paramType == int.class || paramType == Integer.class) {
			return p;
		} else if (paramType == float.class || paramType == Float.class){
			return p;
		} else if (paramType == short.class || paramType == Short.class){
			return p;
		} else if (paramType == long.class || paramType == Long.class){
			return p;
		} else if (paramType == float.class || paramType == Float.class){
			return p;
		} else if (paramType == double.class || paramType == Double.class){
			return p;
		} else if (paramType == byte.class || paramType == Byte.class){
			return p;
		} else if (paramType == boolean.class || paramType == Boolean.class){
			return p;
		} else if (paramType == String.class){
			return p;
		} else {
			return p;
		}
	}


	public static void main(String[] args){

		ObjectId ob1 = ObjectId.get();
		ObjectId ob2 = new ObjectId(ob1.toString());


		String p = "{\r\n  \"ops\": [\r\n    {\r\n      \"forward\": [\r\n        0.0,\r\n        0.0,\r\n        1.0\r\n      ],\r\n      \"op\": [\r\n        0.0,\r\n        0.0\r\n      ],\r\n      \"seq\": [\r\n        52.0\r\n      ],\r\n      \"key\": {\r\n        \"w\": false,\r\n        \"a\": false,\r\n        \"d\": false\r\n      },\r\n      \"force\": [\r\n        0.0\r\n      ],\r\n      \"skill\": [\r\n        0.0\r\n      ]\r\n    },\r\n    {\r\n      \"forward\": [\r\n        0.0,\r\n        0.0,\r\n        1.0\r\n      ],\r\n      \"op\": [\r\n        0.0,\r\n        0.0\r\n      ],\r\n      \"seq\": [\r\n        53.0\r\n      ],\r\n      \"key\": {\r\n        \"w\": false,\r\n        \"a\": false,\r\n        \"d\": false\r\n      },\r\n      \"force\": [\r\n        0.0\r\n      ],\r\n      \"skill\": [\r\n        0.0\r\n      ]\r\n    },\r\n    {\r\n      \"forward\": [\r\n        0.0,\r\n        0.0,\r\n        1.0\r\n      ],\r\n      \"op\": [\r\n        0.0,\r\n        0.0\r\n      ],\r\n      \"seq\": [\r\n        54.0\r\n      ],\r\n      \"key\": {\r\n        \"w\": false,\r\n        \"a\": false,\r\n        \"d\": false\r\n      },\r\n      \"force\": [\r\n        0.0\r\n      ],\r\n      \"skill\": [\r\n        0.0\r\n      ]\r\n    },\r\n    {\r\n      \"forward\": [\r\n        0.0,\r\n        0.0,\r\n        1.0\r\n      ],\r\n      \"op\": [\r\n        0.0,\r\n        0.0\r\n      ],\r\n      \"seq\": [\r\n        55.0\r\n      ],\r\n      \"key\": {\r\n        \"w\": false,\r\n        \"a\": false,\r\n        \"d\": false\r\n      },\r\n      \"force\": [\r\n        0.0\r\n      ],\r\n      \"skill\": [\r\n        0.0\r\n      ]\r\n    },\r\n    {\r\n      \"forward\": [\r\n        0.0,\r\n        0.0,\r\n        1.0\r\n      ],\r\n      \"op\": [\r\n        0.0,\r\n        0.0\r\n      ],\r\n      \"seq\": [\r\n        56.0\r\n      ],\r\n      \"key\": {\r\n        \"w\": false,\r\n        \"a\": false,\r\n        \"d\": false\r\n      },\r\n      \"force\": [\r\n        0.0\r\n      ],\r\n      \"skill\": [\r\n        0.0\r\n      ]\r\n    },\r\n    {\r\n      \"forward\": [\r\n        0.0,\r\n        0.0,\r\n        1.0\r\n      ],\r\n      \"op\": [\r\n        0.0,\r\n        0.0\r\n      ],\r\n      \"seq\": [\r\n        57.0\r\n      ],\r\n      \"key\": {\r\n        \"w\": false,\r\n        \"a\": false,\r\n        \"d\": false\r\n      },\r\n      \"force\": [\r\n        0.0\r\n      ],\r\n      \"skill\": [\r\n        0.0\r\n      ]\r\n    },\r\n    {\r\n      \"forward\": [\r\n        0.0,\r\n        0.0,\r\n        1.0\r\n      ],\r\n      \"op\": [\r\n        0.0,\r\n        0.0\r\n      ],\r\n      \"seq\": [\r\n        58.0\r\n      ],\r\n      \"key\": {\r\n        \"w\": false,\r\n        \"a\": false,\r\n        \"d\": false\r\n      },\r\n      \"force\": [\r\n        0.0\r\n      ],\r\n      \"skill\": [\r\n        0.0\r\n      ]\r\n    },\r\n    {\r\n      \"forward\": [\r\n        0.0,\r\n        0.0,\r\n        1.0\r\n      ],\r\n      \"op\": [\r\n        0.0,\r\n        0.0\r\n      ],\r\n      \"seq\": [\r\n        59.0\r\n      ],\r\n      \"key\": {\r\n        \"w\": false,\r\n        \"a\": false,\r\n        \"d\": false\r\n      },\r\n      \"force\": [\r\n        0.0\r\n      ],\r\n      \"skill\": [\r\n        0.0\r\n      ]\r\n    },\r\n    {\r\n      \"forward\": [\r\n        0.0,\r\n        0.0,\r\n        1.0\r\n      ],\r\n      \"op\": [\r\n        0.0,\r\n        0.0\r\n      ],\r\n      \"seq\": [\r\n        60.0\r\n      ],\r\n      \"key\": {\r\n        \"w\": false,\r\n        \"a\": false,\r\n        \"d\": false\r\n      },\r\n      \"force\": [\r\n        0.0\r\n      ],\r\n      \"skill\": [\r\n        0.0\r\n      ]\r\n    },\r\n    {\r\n      \"forward\": [\r\n        0.0,\r\n        0.0,\r\n        1.0\r\n      ],\r\n      \"op\": [\r\n        0.0,\r\n        0.0\r\n      ],\r\n      \"seq\": [\r\n        61.0\r\n      ],\r\n      \"key\": {\r\n        \"w\": false,\r\n        \"a\": false,\r\n        \"d\": false\r\n      },\r\n      \"force\": [\r\n        0.0\r\n      ],\r\n      \"skill\": [\r\n        0.0\r\n      ]\r\n    },\r\n    {\r\n      \"forward\": [\r\n        0.0,\r\n        0.0,\r\n        1.0\r\n      ],\r\n      \"op\": [\r\n        0.0,\r\n        0.0\r\n      ],\r\n      \"seq\": [\r\n        62.0\r\n      ],\r\n      \"key\": {\r\n        \"w\": false,\r\n        \"a\": false,\r\n        \"d\": false\r\n      },\r\n      \"force\": [\r\n        0.0\r\n      ],\r\n      \"skill\": [\r\n        0.0\r\n      ]\r\n    },\r\n    {\r\n      \"forward\": [\r\n        0.0,\r\n        0.0,\r\n        1.0\r\n      ],\r\n      \"op\": [\r\n        0.0,\r\n        0.0\r\n      ],\r\n      \"seq\": [\r\n        63.0\r\n      ],\r\n      \"key\": {\r\n        \"w\": false,\r\n        \"a\": false,\r\n        \"d\": false\r\n      },\r\n      \"force\": [\r\n        0.0\r\n      ],\r\n      \"skill\": [\r\n        0.0\r\n      ]\r\n    }\r\n  ]\r\n}";
		Document d = Document.parse(p);

		List<Object> list1 = new ArrayList<Object>();
		list1.add(ObjectId.get());
		list1.add(ObjectId.get());
		list1.add(ObjectId.get());
		list1.add(ObjectId.get());

		Map<String, ObjectId> map = new HashMap<String, ObjectId>();
		map.put("1", ObjectId.get());
		map.put("2", ObjectId.get());
		map.put("3", ObjectId.get());
		map.put("4", ObjectId.get());

		Protocol.ServerInfo.Builder sib = Protocol.ServerInfo.newBuilder();
		sib.setIp(ByteString.copyFromUtf8("123"));
		sib.setPort(1000);
		Protocol.ServerInfo si = sib.build();

		Protocol.EntityMailbox.Builder embb = Protocol.EntityMailbox.newBuilder();
		embb.setEntityid(ByteString.copyFromUtf8(ObjectId.get().toString()));
		//embb.setServerinfo(si);
		Protocol.EntityMailbox emb = embb.build();

		List<Object> list = new ArrayList<Object>();
		//list.add(DocumentCodecs.encoder(emb));
		list.add(true);

		Document doc = new Document("p", list);
		Document doc2 = Document.parse(doc.toJson());
		System.out.println(doc2.toJson());

		Test t = new Test();

		invokeParamMethod(t, "hello2", doc2, emb);
	}


}
