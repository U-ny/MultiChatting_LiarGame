package application;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MyDB {
	private static Map<String, UserConnectInfo> users = new HashMap<String, UserConnectInfo>();

	 static int theSize;
	 static String message;
	
	


	public static void addUser(String nickName, UserConnectInfo user) {
		System.out.println("user : " + user);
		users.put(nickName, user);
		int now=users.size();
		theSize=now;
		
		String str="";
		Iterator<String> keys = users.keySet().iterator();
        while (keys.hasNext()){
            String key = keys.next();
            str=str+key+"_";
        }
        message=str;
	}
	
	
	public static void deleteUser(String nickName)
	{
		users.remove(nickName);
	}

	public boolean findByUserId(String nickName) {
		return !users.containsKey(nickName);
		//���� ��� true, �ִ� ��� false �ش�.
	}

}
