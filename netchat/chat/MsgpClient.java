
package netchat.chat;

import java.util.*;

//interface that defines the operations of Client

public interface MsgpClient{  
    
    public int join(String user, String group);
    
    public int leave(String user, String group);
    
    public int send(String sender, ArrayList<String> recipients, String message);
    
    public ArrayList<String> groups();
    
    public ArrayList<String> users(String group);
 	
 	public ArrayList<String> history(String group);
}
