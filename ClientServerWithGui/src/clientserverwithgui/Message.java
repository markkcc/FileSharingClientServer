package clientserverwithgui;
// Class that represent a single chat message
//MIGHT NOT NEED THIS
public class Message {

	private TCPclient user;		// The user that sent the message
	private String message;		// The actual message
	
	public Message(TCPclient user, String message) {
		this.user = user;
		this.message = message;
	}
	
	public TCPclient getUser() {
		return user;
	}
	
	public String toString() {
		
		return user.getName()+": "+message;
	}
}
