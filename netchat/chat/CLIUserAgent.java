package netchat.chat;

import java.util.*;
import java.io.*;
import java.net.*;

public class CLIUserAgent implements UserAgent{         //clients that will be able to chat with each other
	
	private ArrayList<String> receivers;  //users' list
	private String user; 	//this user
	private String message;  //carries the message to be sent
	private static int port;
	private TextMsgpClient protocol;
	
	public CLIUserAgent(String user, String server, int port) throws Exception{
		
		this.user = user;
		message = "";
		receivers = new ArrayList<String>();
		this.port = port;
		
		Socket clieSocket = new Socket(server, port);
		System.out.println(user + " connected with server");
		protocol = new TextMsgpClient(this, clieSocket);  

		Scanner input = new Scanner(System.in);   //command
		String request = "";
		String[] reqWords = null; 
		int reply = 0;                        //reply code
		
		protocol.createClient(user);    //creating user
		
		while(true){
			System.out.print("@"+ user + " >> ");
			request = input.nextLine();
			reqWords = request.split(" ");
			
			if(reqWords[0].equals("join")){   //join group
				reply = protocol.join(user, reqWords[1]);
				if(reply == 201){
					System.out.println("@" + user + " is already a member of " + reqWords[1]);
				}
				else{
					int numMember = Integer.parseInt(protocol.groupSize(reqWords[1]));
					String member = (numMember > 1) ? " members":" member"; 
					System.out.println("Joined #" + reqWords[1] + " with " + numMember + member);					 
				}
			}
			else if(reqWords[0].equals("leave")){  //leave group
				reply = protocol.leave(user, reqWords[1]);	
				if(reply==201){
					System.out.println("Not a member of #" + reqWords[1]);
				}
				else if(reply==400){
					System.out.println("#" + reqWords[1] + " does not exist");
				}
			}
			else if(reqWords[0].equals("groups")){ //groups
				ArrayList<String> groups = (ArrayList<String>)protocol.groups();
				if(groups != null){
					for(String group: groups){
						int numMember = Integer.parseInt(protocol.groupSize(group));
						String member = (numMember != 1) ? " members":" member";
						System.out.println("#" + group + " has " + numMember + member);
					}
				}
			}
			else if(reqWords[0].equals("users")){ //users group
				ArrayList<String> usrs = (ArrayList<String>)protocol.users(reqWords[1]);
				if(usrs != null){
					for(String usr: usrs){
						System.out.println("@" + usr);
					}
				}
			}
			else if(reqWords[0].equals("send")){  //send group user ... message
				receivers.clear();
				String[] recvrs_msgs = request.split(" ", 2);          //removes "send"
				while(recvrs_msgs[1].charAt(0)=='@' || recvrs_msgs[1].charAt(0)=='#'){
					recvrs_msgs = recvrs_msgs[1].split(" ", 2);
					receivers.add(recvrs_msgs[0]);
				}  //continue until message is reached
				message = recvrs_msgs[1];	
				reply = protocol.send(user, receivers, message);
				if(reply == 400){
					System.out.println("One or more non-existent recipients");
				}
			}
			else if(reqWords[0].equals("history")){  //history group
				ArrayList<String> history = (ArrayList<String>)protocol.history(reqWords[1]);
				this.displayMsgs(history);
			}
		}
	}

	public static void main(String args[]) throws Exception{
		
		Scanner in = new Scanner(System.in);
		String username = "";
		String servername = "";
		
		if(args.length == 0){
  			System.out.println("No arguments");
  		}
  		else if(args.length > 3){
  			System.out.println("Excessive arguments provided");
  		}
  		else{
  			//arguments #1 user, #2 server. #3 port
  			username = args[0];
  			servername = (args.length >= 2) ? args[1]:"localhost";
  			port = (args.length == 3) ? Integer.parseInt(args[2]):4311;		
  		}
		CLIUserAgent userAgent = new CLIUserAgent(username, servername, port);
	}
	
	public void displayMsgs(ArrayList<String> h){    //display the history in the required manner in the client
		
		for(String sender_msg: h){
			String[] parts = sender_msg.split(" ", 2);
			System.out.println("[" + parts[0] + "] " + parts[1]);
		} 
	}
	
	public void sendToUser(String msg){    //sending message to client
		
		String[] parts = msg.split(" ", 2);
		System.out.println("[" + parts[0] + "] " + parts[1]);

		if(!parts[0].equals(user)){   //printing prompt after sending message to other clients' streams
			System.out.print("@" + user + " >> ");
		}
	}  
}
