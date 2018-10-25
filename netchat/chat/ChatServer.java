/*The Server class that spwans thread to client's request*/
package netchat.chat;

import java.util.*;
import java.io.*;
import java.net.*;

public class ChatServer{    //server class which will display all the msgp protocol messages
	
	private HashMap<String, ArrayList<String>> groups;   //groupname and its users
	private HashMap<String, ArrayList<String>> groupHistory; //groupname and its history
	private HashMap<String, DataOutputStream> users; //username and ouput streams
	
	//managing recipients for "send"
	
	ArrayList<String> recipients;  //list of users
	ArrayList<String> groupRecipients; //list of groups
	ArrayList<DataOutputStream> streams; //list of streams
 
	public ChatServer(ServerSocket myServerSocket) throws Exception{
		
		Socket servSocket = null;
		System.out.println("Server listening for clients ...");
		groups = new HashMap<String, ArrayList<String>>();
		groupHistory = new HashMap<String, ArrayList<String>>();
		users = new HashMap<String, DataOutputStream>();
		
		recipients = new ArrayList<String>();
		groupRecipients = new ArrayList<String>();
		streams = new ArrayList<DataOutputStream>();
			
    	while(true){ //constantly listen for clients
			servSocket = myServerSocket.accept(); //listen for connections
			ResponseHandler replyThread = new ResponseHandler(this, servSocket);  //for procesing requests
			replyThread.start();
    	}
	}

	public static void main(String args[]) throws Exception{
		int port = (args.length>0) ? Integer.parseInt(args[0]):4311; //default port 4311 if not provided 
   		ServerSocket myServerSocket = new ServerSocket(port, 0);
		ChatServer server = new ChatServer(myServerSocket);	
	}

	//getters
	public HashMap<String, ArrayList<String>> getgroups(){
		return groups;
	}
	public HashMap<String, ArrayList<String>> getgroupHistory(){
		return groupHistory;
	}
	public HashMap<String, DataOutputStream> getUsers(){
		return users;
	}
	
	//existence
	public boolean isUser(String user){
		return users.containsKey(user);
	}
	public boolean isGroup(String group){
		return groups.containsKey(group);
	}
	public boolean isUserofGroup(String user, String group){
		for(String g : groups.get(group)){
			if(g.equals(user)){
				return true;
			}
		}
		return false;
	}

	//commands
	public void createGroup(String group){
		groups.put(group, new ArrayList<String>());
		groupHistory.put(group, new ArrayList<String>());
		System.out.println("New group #" + group + " is created");
	}
	public void createUser(String user, DataOutputStream stream){ // adds a user and its stream
		users.put(user, stream);
	}
	public void addUsertoGroup(String user, String group){  // adds a user to a group
		(groups.get(group)).add(user);
		System.out.println("User " + user + " is added to group #" + group);
	} 
	public void leaveGroup(String user, String group){     // removes a user from a group
		(groups.get(group)).remove(user); 
		System.out.println("User " + user + " is removed from the group " + group);
	}
	public int send(String[] receivers, String message){
		recipients.clear();
		groupRecipients.clear();
		streams.clear();
		for(String r: receivers){   //identify users & groups in recipients
			if (r.charAt(0)=='@'){
				recipients.add(r.substring(1));
			}
			else{
				groupRecipients.add(r.substring(1));
			}
		}
		
		for(String r: recipients){  //error for non-existent users & groups
			if(!this.isUser(r)){
				return 400;
			}
		}
		for(String g: groupRecipients){
			if (!this.isGroup(g)){
				return 400;
			}
			else{	
				(groupHistory.get(g)).add(message);   //add message to group's history
				for(String r: groups.get(g)){        //all users in group added to recipients
					if(!recipients.contains(r)){
						recipients.add(r);
					}
				}
			}
		}
		for(String u: recipients){           //streams of users
			streams.add(users.get(u));
		}
		for(DataOutputStream strm: streams){  //print messages to users' streams
			try{
				strm.writeUTF(message); 
			}
			catch(IOException e){
				e.printStackTrace();
			} 
		}
		return 200;
	}
}
