
package netchat.chat;
import java.util.*;
import java.io.*;
import java.net.*;

public class ResponseHandler extends Thread{ 

	private ChatServer server;   //its server
	private Socket servSocket;   //communicating socket 
	private DataInputStream in;    //streams for socket
	private DataOutputStream out;
	private String request;
	private String reply;

	public ResponseHandler(ChatServer server, Socket servSocket){
		this.server = server;
		this.servSocket = servSocket;
		try{
			in = new DataInputStream(servSocket.getInputStream());
			out = new DataOutputStream(servSocket.getOutputStream());
		}
		catch(IOException e){
			e.printStackTrace();
		}
		request = "";
		reply = "";	
	}

	public void run(){   //runs constantly for getting reply codes for the client inputs
		try{
			while(true){
				request = in.readUTF();
				String[] reqWords = request.split(" ");  //words in request
				if(reqWords[1].equals("size")){  //for only the size of the group
					out.writeUTF(String.valueOf(this.numOfUsers(reqWords[2])));
				}
				else if(reqWords[1].equals("create")){   //initiate client creation for server as soon as it connects
					this.clientCreator(reqWords[2]);
				}
				else{    //our actual requests
					System.out.println("\n" + request);
					
					switch(reqWords[1]){
						case "join":
							reply = this.join(reqWords[2], reqWords[3]);
							break;
						case "leave":
							reply = this.leave(reqWords[2], reqWords[3]);
							break;
						case "groups":
							reply = this.groups();
							break;
						case "users":
							reply = this.users(reqWords[2]);
							break;
						case "send":
							reply = this.send(request);
							break;
						case "history":
							reply = this.history(reqWords[2]);
							break;
					}
					

					System.out.println(reply);
					out.writeUTF(reply);           //reply to client
					reply = "";
				}
			} 
		}
		catch(IOException e){
			e.printStackTrace();
		}	
	}
	
	//all request related methods
	private String join(String user, String group){   //figures if user is already in group or not 
		if(!server.isGroup(group)){      //group non-existent
			server.createGroup(group);
		}
		else{
			if(server.isUserofGroup(user, group)){ //already in group
				return "msgp 201 No result";
			}
		}
		server.addUsertoGroup(user, group);
		return "msgp 200 OK";
	}

	private String leave(String user, String group){     //figures if users is in group or not, or the group exists
		if(!server.isGroup(group)){    //group does not exist
			return "msgp 400 Error";
		}	
		else if(!server.getgroups().get(group).contains(user)){   //user not in group already
			return "msgp 201 No result";
		}	
		else{
			server.leaveGroup(user, group);
			return "msgp 200 OK";
		}
	}
	
	private String groups(){       //lists all created groups
		if(server.getgroups().isEmpty()){
			return "msgp 201 No result";
		}
		else{	
			String response = ""; 
			for(String groupname: server.getgroups().keySet()){
				response += "\n" + groupname; 
			}
			return "msgp 200 OK" + response;
		}	
	}
	
	private String users(String group){    //lists users of a group
		if(!server.getgroups().containsKey(group)){  //group doesn't exist
			return "msgp 400 Error";
		}
		if(server.getgroups().get(group).isEmpty()){
			return "msgp 201 No result";
		}
		else{	
			String response = "";
			for(String usr : server.getgroups().get(group)){
				response += usr + "\n"; 
			}
			return "msgp 200 OK\n" + response;
		}
	}
	
	private String send(String command){   //breaks down messages into recipients and the message for sending to a validation method
		String noMessage = command.split("\n\n",2)[0];   //ignore actual message which comes after two new lines 
		String receiversPart = noMessage.substring(noMessage.indexOf("to:"));  //only the recipients' part
		String[] receiverLines = receiversPart.split("\n");   //array with each recipient's line
		String[] recipients = new String[receiverLines.length];  //list of recipients

		for(int i=0; i<receiverLines.length; i++){
			recipients[i] = (receiverLines[i].split(" ", 2))[1];  //taking in the user/group names
		}
		int replyCode = server.send(recipients, command);
		if(replyCode == 200){
			return "msgp 200 OK";
		}
		else{
			return "msgp 400 Error";
		}
	}
	
	private String history(String group){   //lists senders and messages sent in a group, if it exists
		if(!server.isGroup(group)){   //group doesn't exist
	 		return "msgp 400 Error";
	 	}
	 	else if(server.getgroupHistory().get(group).isEmpty()){   //no messages in group yet
	 		return "msgp 201 No result";
	 	}
	 	else{
	 		String response = "";
	 		for(String h: server.getgroupHistory().get(group)){
	 			response += h + "\n";
	 		}
	 		return "msgp 200 OK\n" + response;
	 	} 	
	}
	
	//extra methods
	private int numOfUsers(String group){         //for returning number of members in a group while joining
		return server.getgroups().get(group).size();
	}
	
	private void clientCreator(String client){  //invoking the create user in server
		if(!server.isUser(client)){
			server.createUser(client, out);
		}
		return;
	}
}
