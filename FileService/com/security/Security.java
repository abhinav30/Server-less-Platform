package com.security;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.message.Message;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
/*import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
*/

/*import java.io.File;
import java.io.FileReader;
import java.io.IOException;*/

/*import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;*/


public class Security {
	
	static HashMap <String , String> Token_Hash = new HashMap <>();
	static int hash_value = 0;
	
	@SuppressWarnings({ "unchecked", "unused" })
	public JSONObject Sd_logIn(JSONObject message, Message messageObject) throws Exception
	{
		
		JSONObject response=new JSONObject();
		JSONObject serverDetails=null;
	    int user_flag = 0;
	    JSONArray DataArray = null;
	    String user_type = "";
	    String username=(String)message.get("username");
	    String password=(String)message.get("password");
	    
	    JSONObject getDataService=messageObject.callServiceURL("http://"+Message.getGateWayAddr()+"/Serverless/Userservlet?servicename=data_service&&type=sdlogin&&username="+username+"&&password="+password);
	    
	    if((String)getDataService.get("status")== "1")
	    {
	    	DataArray = (JSONArray)getDataService.get("result");
	    	
	    	if(DataArray.size() != 0)
				user_flag = 1;
	    	else
		    	user_flag = 0;
	    }
	    else
	    	response.put("status", "0");
	    
	    	
	    if(user_flag == 1)
	    {
	    	for (int i = 0; i < DataArray.size(); i++) 
	    	{
	    	    JSONObject jo = (JSONObject)DataArray.get(i);
	    	    if((String)jo.get("user_type") != null);
	    	    {
	    	    	user_type = (String)jo.get("user_type");
	    	    	break;
	    	    }
	    	}
	    	response.put("status","1");
	    	String token  = token_creation(username);
	    	response.put("token", token);
	    	response.put("user_type", user_type);
	    	
	    }
        
	    else
	    	response.put("status","0");
	    
	    return response;
	
	}
	
	
	
	
	@SuppressWarnings({"unchecked" })
	public JSONObject Authorization(JSONObject message,Message messageObject)
	{
		JSONObject response=new JSONObject();
		
	    String Token=(String)message.get("token");
	    String service_name=(String)message.get("service_name");
	    String username = Token_Hash.get(Token);
		if(Token_Hash.get(Token) == null)
			response.put("status","0");
		else if(Token_Hash.get(Token) != null)
		{
			JSONObject getDataService=messageObject.callServiceURL("http://"+Message.getGateWayAddr()+"/Serverless/Userservlet?servicename=data_service&&type=select&&db_name=admin&&attributes=*&&conditions=service~"+service_name+"&&tbl_name="+username);; 
		    
		    if((String)getDataService.get("status")== "1")
		    	response.put("status","1");
		    
		    else
		    	response.put("status","0");
			
		}
	return response;
	}
	
	
	
	public synchronized static int incrementHash() 
    {
        hash_value++;
        if(hash_value==Integer.MAX_VALUE)
        	hash_value=0;
        return hash_value;
    }
	
	
	
	public String token_creation(String input) throws Exception
	{
		MessageDigest msg;
		String token = null;
		int x = incrementHash();
		try 
		{
			msg = MessageDigest.getInstance("MD5");
			msg.reset();
			String temp = String.valueOf(x);
			msg.update(temp.getBytes());
			byte[] digest = msg.digest();
			BigInteger bigInt = new BigInteger(1,digest);
			String hashtext = bigInt.toString(16);
			//padding is here to get 32 bit token
			while(hashtext.length() < 32 )
			  hashtext = "0"+hashtext;
			
			token = hashtext;
			
		}
		catch (java.security.NoSuchAlgorithmException e)
		{
			throw e;
		}
		Token_Hash.put(token,input);
		return token;
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	public JSONObject logout(JSONObject message)
	{
		JSONObject response=new JSONObject();
	    String Token=(String)message.get("token");
	    
	    if(Token_Hash.get(Token) == null)
			response.put("status","1");
		else if(Token_Hash.get(Token) != null)
		{
			Token_Hash.remove(Token);
			response.put("status","1");
		}	
	    
		return response;
	}


}
