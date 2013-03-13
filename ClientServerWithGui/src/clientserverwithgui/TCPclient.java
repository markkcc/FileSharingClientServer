/*
 * Author: Mark El-Khoury
 * Project: File sharing system
 * Date: January 2013
 */
package clientserverwithgui;

import java.io.*; 
import java.net.*; 
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;

public class TCPclient {
    
    public Socket clientSocket;
    private DataInputStream inFromServer;
    private DataOutputStream outToServer;
    private String name;
    private String password;
    private chatListener chatListen;
    
    public TCPclient() throws Exception { 

        clientSocket = new Socket("localhost", 1992);
        clientSocket.setSoTimeout(100);
        inFromServer = new DataInputStream(clientSocket.getInputStream()); 
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        name = "InvalidUsername";   
    } 
    
    public TCPclient(String username, char[] pass, int port) throws Exception { 

        clientSocket = new Socket("localhost", 1992);
        inFromServer = new DataInputStream(clientSocket.getInputStream()); 
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeUTF(Protocol.RESPONSE_REJECTED + "");
        name = username;   
        password = "";
        for(int i = 0; i < pass.length; i++)
            password += pass[i];
        outToServer.writeUTF("" + Protocol.AUTHENTICATE + "" + name.length() + "" + password.length() + "" + name + password);
        //should read first 3 chars for protocol
        //then digit1
        //then digit2
        //then substring(6, 6+digit1); <-- username
        //then substring(6+digit1, 6+digit1+digit2); <-- password
        if(Integer.parseInt(
            inFromServer.readUTF().substring(0, 3)) != Protocol.RESPONSE_OK) {
            clientSocket.close(); //if auth failed
        }
        ServerThread serve = new ServerThread(this);
                    serve.start();
        ///////////////////////////////////////////////////////CHAT
                    ///////////////////////////////////////////////////
        chatListen = new chatListener();
        chatListen.start();
    }
    
    
    public class chatListener extends Thread {
        boolean runme;
        public chatListener() {
            runme=true;
        }
        public void run() {
            try {
                while(runme) {
                    if(!runme) {
                        System.out.println("DEBUG: RETARDED CHAT");
                        break;
                    }
                    else {
                    String msg = inFromServer.readUTF();
                    if(Integer.parseInt(msg.substring(0, 3)) == Protocol.CHAT)
                        ClientWindow.updateChat(msg.substring(3));
                    else if(Integer.parseInt(msg.substring(0, 3)) == Protocol.KILLCHAT) {
                        try {
                            runme = false;
                        } catch (Throwable ex) {
                            Logger.getLogger(TCPclient.class.getName()).log(Level.SEVERE, null, ex);
                            runme = false;
                        }
                        runme = false;
                    }
                    else {
                        runme = false;
                    }
                    }
                }
        }
        catch(Exception e) {
            System.out.println("DEBUG: SOCKET FAILED");
        }
        }
        public void kill() {
            runme = false;
        }
    }
    
    public TCPclient(Socket socket) {
        try {
            clientSocket = socket;
            inFromServer = new DataInputStream(clientSocket.getInputStream());
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(TCPclient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public DefaultListModel requestCoClList(DefaultListModel list) {
        try {
            /*chatListen.interrupt();
            chatListen.stop(); //to avoid conflict
            chatListen.suspend();*/
           // chatListen.kill();
            outToServer.writeUTF(Protocol.REQUEST_COCLLIST + "....");
            //chatListen.kill();
            String cmd = inFromServer.readUTF();
            if(cmd.substring(0, 3).equals(Protocol.KILLCHAT + "")) {
                cmd = inFromServer.readUTF();
            }
            while(cmd.substring(0, 3).equals(Protocol.LIST_ELEMENT + "")) {
                     list.addElement(cmd.substring(3));
                     if(cmd.substring(0, 3).equals(Protocol.END_OF_LIST + ""))
                         break;
                     cmd = inFromServer.readUTF();
            }
            //chatListen = new chatListener();
            //chatListen.start();
            return list;
        } catch (IOException ex) {
        Logger.getLogger(TCPclient.class.getName()).log(Level.SEVERE, null, ex);
        return list;
        }
    }
    
    //below are methods for server-related handling
    
    public void getString(String msg) {
        try {
            System.out.println("DEBUG: Writing message: " + msg);
            outToServer.writeUTF(msg);
        } catch (IOException ex) {
            Logger.getLogger(TCPclient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String readString() {
        try {
            String message = inFromServer.readUTF();
            return message;
        } catch (IOException ex) {
            MainWindow.dcClient(this);
            this.close();
            return "-10";
            //Logger.getLogger(TCPclient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int readInteger() {
	try {
            int read = inFromServer.read();
            return read;
	} catch (IOException e) {
		
	}
	return -1;
	}
    
    
    public void close() {
        try {
            inFromServer.close();
            outToServer.close();
            clientSocket.close();
            System.out.println("Client closed.");
        } catch (IOException ex) {
            System.out.println("Error closing client.");
        }
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String str) {
        name = str;
    }
    
    public DefaultListModel getfiles(String path, DefaultListModel list) {
        
        try {
            outToServer.writeUTF(Protocol.LISTFILES + "....");
            //chatListen.kill();
            String cmd = inFromServer.readUTF();
            if(cmd.substring(0, 3).equals(Protocol.KILLCHAT + "")) {
                cmd = inFromServer.readUTF();
            }
            while(cmd.substring(0, 3).equals(Protocol.LIST_ELEMENT + "")) {
                     list.addElement(cmd.substring(3));
                     if(cmd.substring(0, 3).equals(Protocol.END_OF_LIST + ""))
                         break;
                     cmd = inFromServer.readUTF();
            }
            /////////////////////////////////////////////CHAT
            /////////////////////////////////////////////
            //chatListen = new chatListener();
            //chatListen.start();
            return list;
        } catch (IOException ex) {
        Logger.getLogger(TCPclient.class.getName()).log(Level.SEVERE, null, ex);
        return list;
        }
    }
    
    public void downloadfile(String path) {
        System.out.println(path);
    }
    
    public class ServerThread extends Thread {
        
        private TCPclient user;
        public ServerThread(TCPclient tcpuser) {
            user = tcpuser;
        }
        
        public void run() {
            
            while(true) {
                System.out.println("DEBUG: CONNECTION ACCEPTED\n" +
                  "IP: " + user.clientSocket.getInetAddress().getHostAddress() +
                  "\nHOST: " + 
                        user.clientSocket.getInetAddress().getHostName());
                String msg = user.readString();
                System.out.println("DEBUG: CMD RCVD");
                int request = Integer.parseInt(msg.substring(0, 3));
                //first 3 chars are protocol
                msg = msg.substring(3);
                if(request == -10)
                    break;
                
                switch (request) {
                    case Protocol.CHAT_CLIENT_TO_SERVER:
                        postMessage(msg);
                        break;
                    case Protocol.FILE_REQUEST:
                        fetchFile(msg);
                        break;
                    case Protocol.AUTHENTICATE:
                        auth(msg);
                        break;
                    case Protocol.REQUEST_COCLLIST:
                        cocllist();
                        break;
                    case Protocol.LISTFILES:
                        filelist();
                        break;
                    default:
                        break;
                }
            }
            
            //client has disconnected
            
            user.close();
        }
        
        public void postMessage(String message) {
            MainWindow.postChatMessage(message);
        }
        
        public void fetchFile(String msg) {
            //actually server doesn't do anything
            //should implement miniServer from the client
            //it will open a new connection to the destination client
        }
        
        public void cocllist() {
            user.getString(Protocol.KILLCHAT + "...");
            for(int i = 0; i < MainWindow.clientListElements(); i++) {
                System.out.println((i+1) + " elements.");
               user.getString(Protocol.LIST_ELEMENT + MainWindow.clientList(i));
            }
            user.getString(Protocol.END_OF_LIST + "....");
        }
        
        public void filelist() {
            user.getString(Protocol.KILLCHAT + "...");
            for(int i = 0; i < MainWindow.fileListElements(); i++) {
                System.out.println((i+1) + " elements.");
               user.getString(Protocol.LIST_ELEMENT + MainWindow.fileList(i));
            }
            user.getString(Protocol.END_OF_LIST + "....");
        }

        
        public void auth(String msg) {
        //should read first 3 chars for protocol
        //then digit1
        //then digit2
        //then substring(6, 6+digit1); <-- username
        //then substring(6+digit1, 6+digit1+digit2); <-- password
            user.getString(Protocol.RESPONSE_OK + "");
           user.setName(msg.substring(2, 2+Integer.parseInt(msg.charAt(0)+"")));
           MainWindow.addClient(user);
        }
    }
    
}
