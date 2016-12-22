package com.fish.yz.util;

import com.fish.yz.protobuf.Protocol;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class Test{

	public void hello(ObjectId id, List<ObjectId> list, ObjectId oid){
		System.out.println("hello " + id + list + oid);
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
	 * 使用document解析函数参数, 不支持重载函数, 支持基本类型, 但是不支持泛型等，特殊的参数需要自己写解析器
	 * document 使用 "p": [] 格式
	 * @param object
	 * @param methodName
	 * @param doc
	 * @return
	 */
	public static void invokeMethod(Object object, String methodName, Document doc){
		List<Object> pList = (List<Object>)doc.get("p");

		Method[] methods = object.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.getName().equals(methodName)){
				Type[] paramTypeList = method.getGenericParameterTypes();
				Object[] param = new Object[paramTypeList.length];
				int i = 0;
				for (Type paramType : paramTypeList) {
					if (paramType instanceof ParameterizedType) { //如果是泛型类型
						ParameterizedType ptype = ((ParameterizedType) paramType);
						Type[] types = ptype.getActualTypeArguments();// 泛型类型列表
						if (ptype.getRawType() == List.class){
							param[i] = pList.get(i);
							i++;
						}
					} else {
						param[i] = decode(paramType, pList.get(i));
						i++;
					}
				}
				try {
					method.invoke(object, param);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}

	private static Object decode(Type paramType, Object p){
		if (paramType == Protocol.ServerInfo.class){
			Document d = Document.parse((String)p);
			return DocumentCodecs.decode(d);
		} else {
			return p;
		}
	}


	public static void main(String[] args){
		List<Object> list1 = new ArrayList<Object>();
		list1.add(ObjectId.get());
		list1.add(ObjectId.get());
		list1.add(ObjectId.get());
		list1.add(ObjectId.get());

		List<Object> list = new ArrayList<Object>();
		list.add(ObjectId.get());
		list.add(list1);
		list.add(ObjectId.get());

		Document doc = new Document("p", list);
		System.out.println(doc.toJson());
		Document doc2 = Document.parse(doc.toJson());
		System.out.println(doc2.toJson());

		Test t = new Test();

		invokeMethod(t, "hello", doc2);
	}


}
