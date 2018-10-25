
package netchat.chat;
import java.util.*;
import java.io.*;
import java.net.*;

public class TextMsgpClient extends Thread implements MsgpClient{	//a translator of clients' request into msgp format

 	private DataOutputStream out;   //stream for sending commands
 	private DataInputStream in;   //server response stream
 	private String command;   //msgp format
 	private String response;
 	private String buffer;     //store replies
 	CLIUserAgent userAgent;   //user that has the client as an attribute

 	public TextMsgpClient(CLIUserAgent userAgent,Socket clieSocket) throws Exception{
 		this.out = new DataOutputStream(clieSocket.getOutputStream());
 		this.in = new DataInputStream(clieSocket.getInputStream());
 		this.command = "";
 		this.response = "";
 		this.userAgent = userAgent;
		this.start();   //thread for messages
 	}

    public int join(String user, String group){
    	command = "msgp join " + user + " " + group;
    	try{
    		out.writeUTF(command);
    		this.getResponse();
    	}
    	catch(IOException e){
    		e.printStackTrace();
    	}
    	finally{
    		return Integer.parseInt(response.split(" ",3)[1]);
    	}
    }

    public int leave(String user, String group){
    	command = "msgp leave " + user + " " + group;
    	try{
    		out.writeUTF(command);
			this.getResponse();
    	}
    	catch(IOException e){
    		e.printStackTrace();
    	}
    	finally{
    		return Integer.parseInt(response.split(" ",3)[1]);
    	}
	}

    public ArrayList<String> groups(){
    	ArrayList<String> groups = null;
    	command = "msgp groups";
    	try{
    		out.writeUTF(command);
    		this.getResponse();
    	}
    	catch (IOException e){
    		e.printStackTrace();
    	}
    	finally{
    		if(response.startsWith("msgp 200 OK")){
				groups = new ArrayList<String>(Arrays.asList(response.split("\n")));  //makes a list of group names
				groups.remove(0);                 //remove msgp send line
			}
  			return groups;
    	}
    }

    public ArrayList<String> users(String group){
    	ArrayList<String> usrs = null;
    	command = "msgp users " + group;
    	try{	
    		out.writeUTF(command);
    		this.getResponse();
    	}
    	catch(IOException e){
    		e.printStackTrace();
    	}
    	finally{
    		if(response.startsWith("msgp 201")){    //do nothing if no users in the group
    			;
    		}
    		else if(response.startsWith("msgp 400")){
    			System.out.println("#" + group + " doesn't exist");
    		}
    		else{
				usrs = new ArrayList<String>(Arrays.asList(response.split("\n")));   //list os users' names
				usrs.remove(0);                  //remove msgp users line
			}
    		return usrs;
    	}
    }
    
    public int send(String sender, ArrayList<String> receivers, String message){
    	command = "msgp send \nfrom: " + sender + "\n";
    	for(String r: receivers){
    		command += ("to: " + r + "\n");
    	}
    	command += "\n" + message + "\n";
    	try{
    		out.writeUTF(command);
    		this.getResponse();
    	}
   		catch(IOException e){
    		e.printStackTrace();
    	}
    	finally{
    		return Integer.parseInt(response.split(" ",3)[1]);
    	} 
    }

    public ArrayList<String> history(String group){
    	ArrayList<String> history = new ArrayList<String>();
    	command = "msgp history " + group;
    	try{
    		out.writeUTF(command);
    		this.getResponse();
    	}
    	catch (IOException e){
    		e.printStackTrace();
    	}
    	finally{
    		if(response.startsWith("msgp 201")){
    			;
			}
			else if(response.startsWith("msgp 400")){
				System.out.println("#" + group + " doesn't exist");
			}
    		else{
				String[] msgs = response.split("\n\nmsgp send");
				for(String msg: msgs){
					history.add(sender_msg(msg));
				}
    		}	
    		return history;
    	}
    }
	
	//extra methods
	public String groupSize(String group){   //for retrieving the group size
    	command = "msgp size " + group;
    	try{	
    		out.writeUTF(command);
    		this.getResponse();
    	}
    	catch(IOException e){
    		e.printStackTrace();
    	}
    	finally{
    		return response;
    	}	
	}
	
    private void getResponse(){   //method for msgp response from the thread
    	do{
			try{
				Thread.sleep(25);
			}
			catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		while(buffer.equals(""));
		response = buffer;
		buffer = "";
    }

    private String sender_msg(String msg){   //gives sender and message partitioned by a space from the msgp response
        msg = msg.substring(msg.indexOf("from"));  //starting from from line
		String sender = (msg.split("\n",2)[0]).split(" ")[1]; //sender name
		String text = msg.split("\n\n",2)[1]; //the actual message
		if(text.contains("\n")){
			text = text.substring(0, text.indexOf("\n")); //removing extra new lines
		}
		return sender + " " + text;
    }	
  
	public void run(){    //runs constantly to listen for input (pre-defined method for thread)
 		String inmsg;
 		try{
			while(true){
				inmsg = in.readUTF();
				if(inmsg.startsWith("msgp send")){  //process messages to send
					userAgent.sendToUser(sender_msg(inmsg));
				}
				else{         //save messages for commands other than "send"
					buffer = inmsg;
				}
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}	
	}
	
	public void createClient(String client){            //creating connected client
		command = "msgp create " + client;
    	try{
    		out.writeUTF(command);
    		this.getResponse();
    	}
    	catch(IOException e){
    		e.printStackTrace();
    	}
    	finally{
    		return;
    	}
	}
}
