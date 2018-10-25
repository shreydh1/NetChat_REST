###Type the following to run the program.  

$ javac netchat.chat.*.java  
$ java netchat.chat.ChatServer <portNumber>  
$ java netchat.chat.CLIUserAgent <localhost> <portNumber>

## Protocol Specification:  

From Client Side  
@ join <groupName>             #creates a group
@ leave <groupName>  	       #leaves the group
@ groups 	               #shows groups
@ send <recepient> <message>   #sends the message to @recepient in the group.
@ history <groupName> 	       #shows the history of messages sent to group


