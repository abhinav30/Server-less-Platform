package com.lifecycle;

import org.json.simple.JSONObject;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.message.Message;

public class ServiceManagerMain 
{
	static Message messageObject;
	
	public static void main(String[] args) throws Exception 
	{
		messageObject=new Message(args[0],args[1],args[2],args[3]);
		messageObject.recieveMessage();
	}
	
	/**
	 * Method to process Request in separate thread
	 * @param message
	 */
	public void processRequest(JSONObject message)
	{
		/*if(message!=null)
    	{
    		new Thread(new Runnable() 
    	    {
    	         public void run() 
    	         {
    	            JSONObject response;
    				try 
    				{
    					response = parseMessage(message);
    					if(response!=null)
    		            {
    						messageObject.sendMessage(response);
    		            }
    				} 
    				catch (Exception e) 
    				{
    					e.printStackTrace();
    				}
    	                
    	         }
    	    }).start();
    	}*/
		JSONObject response;
		try 
		{
			response = parseMessage(message);
			if(response!=null)
            {
				messageObject.sendMessage(response);
            }
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to parse the message and return response message for a request
	 * @throws JSchException 
	 */
	public JSONObject parseMessage(JSONObject message) throws Exception
	{
		System.out.println("Message Recieved");
		JSONObject response=null;
		String type=(String)message.get("type");
		switch(type)
		{
			case "create_service" :  	createService(message);
										break;
			case "stop_service" :    	stopService(message);
										break;
		}
		return response;
	}
	
	@SuppressWarnings("unchecked")
	public void createService(JSONObject message) throws Exception
	{
		ServiceUtils obj=new ServiceUtils();
		JSONObject serverDetails=null;
		Session session=null;
		String serviceName=(String)message.get("service_name");
		messageObject.logMessage("INFO", "Calling VM manager for VM details");
		if(message.containsKey("server"))
		{
			String IP=(String)((JSONObject)message.get("server")).get("IP");
			serverDetails=messageObject.callServiceURL("http://"+Message.getGateWayAddr()+"/Serverless/UserServlet?service_name=vm_manager&&type=vm_info&&ip="+IP);
		}
		else
		{
			serverDetails=messageObject.callServiceURL("http://"+Message.getGateWayAddr()+"/Serverless/UserServlet?service_name=vm_manager&&type=start_vm");
			updateServer(serverDetails);
		}
		messageObject.logMessage("INFO", "Fetched details from VM");
		JSONObject destination=new JSONObject();
		destination.put("ip", serverDetails.get("ip"));
		destination.put("username", serverDetails.get("username"));
		destination.put("password", serverDetails.get("password"));
		if(!message.containsKey("server"))
		{
			obj.deployJar(session, "agent", destination,messageObject);
			messageObject.logMessage("INFO", "Deployed agent on VM");
		}
		obj.deployJar(session, serviceName, destination,messageObject);
		messageObject.logMessage("INFO", "Deployed "+serviceName+" on VM");
		message.put("queue", message.get("service_name"));
		message.put("parameters", message.get("parameters"));
		updateService(serverDetails, serviceName);
		messageObject.sendMessage(message);
	}
	
	public void stopService(JSONObject message)
	{
		//String IP=(String)((JSONObject)message.get("serverdetails")).get("IP");
		//JSONObject serverDetails=messageObject.callServiceURL("http://"+Message.getGateWayAddr()+"/Serverless/Userservlet?servicename=server_manager&&type=server_request&&ip="+IP);
		//if no other VM is running on that machine update the servers.xml
	}
	
	@SuppressWarnings("unchecked")
	public void updateServer(JSONObject serverDetails)
	{
		String ip=(String)serverDetails.get("ip");
		JSONObject message=new JSONObject();
		message.put("queue", "loadbalancer");
		message.put("type", "update_server");
		message.put("ip", ip);
		messageObject.sendMessage(message);
	}
	
	@SuppressWarnings("unchecked")
	public void updateService(JSONObject serverDetails,String serviceName)
	{
		String ip=(String)serverDetails.get("ip");
		JSONObject message=new JSONObject();
		message.put("queue", "loadbalancer");
		message.put("type", "update_service");
		message.put("ip", ip);
		message.put("service", serviceName);
		messageObject.sendMessage(message);
	}
}
