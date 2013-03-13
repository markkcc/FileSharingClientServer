/*
 * Author: Mark El-Khoury
 * Project: File sharing system
 * Date: January 2013
 */
package clientserverwithgui;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPserver {
    
    public static List<TCPclient> users = 
            Collections.synchronizedList(new ArrayList<TCPclient>());
    
    private ServerSocket servSocket;
    private DataOutputStream outToClient;
    private InetAddress address;
    private Socket connectionSocket;
    private DataInputStream inFromClient;
    private int numUsers = 0;
    
    public TCPserver(int port) throws IOException {
        
        servSocket = new ServerSocket(port); //create a new ServerSocket
        address = InetAddress.getLocalHost(); //get local ip address
       //outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        System.out.println("DEBUG: TCPSERVER CLASS OK");
        //start listening to connections
        listenerThread listen = new listenerThread(port);
        listen.start();
    }
    
    public void sendMessage(String msg) throws IOException {
        if(!users.isEmpty()){
            for(int i = 0; i < numUsers; i++)
                users.get(i).getString(msg);
        }
    }
    
    public String getClientChat() {
        String line = "";
        
        return line;
    }
    
    public class listenerThread extends Thread {
        
        public int port = 1992;
        public listenerThread(int port) {
            this.port = port;
        }
        public void run() {
            try {
                while(true) {
                    Socket clientSocket = servSocket.accept();
                    
                    inFromClient = new DataInputStream(clientSocket.getInputStream());
            outToClient = new DataOutputStream(clientSocket.getOutputStream());
            String name = inFromClient.readUTF();
            if(!name.equals(Protocol.RESPONSE_REJECTED + "")) {

                    File file = new File(name);
            FileInputStream input = new FileInputStream(file);
            int offset = 0;
            
            
            byte[] buffer = new byte[4096];
            while((offset = input.read(buffer)) != -1) {
                    outToClient.write(buffer,0,offset);
            }
            /*
            byte[] data = new byte[1048576];
            input.read(data);
            outToClient.write(data);*/
            
            outToClient.flush();
            clientSocket.close();

                      
                      
                      
            }
            else {
                TCPclient user = new TCPclient(clientSocket);
                    users.add(user);
                    numUsers++;
                    ServerThread serve = new ServerThread(user);
                    serve.start();
            }
          }
        }
        catch(Exception e) {
            System.out.println("DEBUG: SOCKET FAILED");
            Logger.getLogger(ClientWindow.class.getName()).log(Level.SEVERE, null, e);
        }
        }
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
            users.remove(user);
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
