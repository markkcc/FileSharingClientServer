/*
 * Author: Mark El-Khoury
 * Project: File sharing system
 * Date: January 2013
 */
package clientserverwithgui;


public class Protocol {
    
    // COMMAND MUST ALWAYS BE 3 DIGITS LONG!!!
    public static final int PROTOCOL_DIGITS_LENGTH = 3;
    
        public static final int CHAT_CLIENT_TO_SERVER = 100;
	public static final int CHAT_SERVER_TO_CLIENT = 100;
        public static final int CHAT = 100;
        public static final String CHAT_MESSAGE = "100";
	public static final int AUTHENTICATE = 102;
        public static final int FILE_REQUEST = 103;
	
	public static final int RESPONSE_OK = 200;
	public static final int RESPONSE_REJECTED = 201;
        
        public static final int HELP = 300;
        public static final int QUIT = 301;
        
        public static final int REQUEST_COCLLIST = 444;
        public static final int LIST_ELEMENT = 465;
        public static final int END_OF_LIST = 499;
        
        public static final int FILE = 514;
        
        public static final int KILLCHAT = 600;
        
        public static final int LISTFILES = 755;
        
}
