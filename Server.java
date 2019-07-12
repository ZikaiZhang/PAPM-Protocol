package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javax.mail.*;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Server extends Application {
		
  // String for storing user information
  private String userPush;
  private String userPull;
  private String passPush;
  private String passPull;
  
  // String for storing destination and source address
  private String toPush;
  private String fromPush;
  
  // String for storing host for push action
  private String hostPush = "smtp.163.com";
  private String hostPull = "pop.163.com";
  
  // ArrayList for storing CLI input
  ArrayList<String> cliPush = new ArrayList<String>();
  ArrayList<String> cliPull = new ArrayList<String>();
  
  private String subjPush;
  private String contPush = "";
  private DataInputStream inputFromClient;
  private DataOutputStream outputToClient;

@Override
  public void start(Stage initialStage) {
    // Text area for displaying contents
    TextArea NewTextArea = new TextArea();

    // Set a scene
    Scene scene = new Scene(new ScrollPane(NewTextArea), 0, 0);
    
    initialStage.setTitle("Server");
    
    initialStage.setScene(scene);
    
    initialStage.show();
    
    new Thread( () -> 
    {
      try {
        // Create a server socket
        ServerSocket serverSocket = new ServerSocket(49153);
        
        Platform.runLater(() -> NewTextArea.appendText("Server started at " + new Date() + '\n'));
  
        // Listening for a connection request
        Socket socket = serverSocket.accept();
  
        // Create input and output streams
        inputFromClient = new DataInputStream(socket.getInputStream());
        
        outputToClient = new DataOutputStream(socket.getOutputStream());
        
        // Two strings storing temporary client and server message
        String clientMessage;
        
        String serverMessage;
        
        // main algorithm for server
        while (true) 
        {
          
          clientMessage = inputFromClient.readUTF();
          
          serverMessage = "";
          
          if (((String) clientMessage).equals("HELO"))
          {
        	  	cliPush.add(clientMessage);
          	serverMessage = helo();
          }
          else if (((String) clientMessage).equals("QUIT"))
          {
        	    cliPush.add(clientMessage);
          	serverMessage = quit();
          	outputToClient.writeUTF(serverMessage);
          	inputFromClient.close();
          	outputToClient.close();
          }
          else if (((String) clientMessage).equals("STAT"))
          {
        	    cliPull.add(clientMessage);
          	serverMessage = stat();
          }
          else if (((String) clientMessage).startsWith("LIST"))
          {
        	    cliPull.add(clientMessage);
          	serverMessage = list(clientMessage);
          }
          else if (((String) clientMessage).startsWith("READ"))
          {
        	    cliPull.add(clientMessage);
        	    serverMessage = read(clientMessage);
          }
          else if (((String) clientMessage).startsWith("DELE"))
          {
        	  	cliPull.add(clientMessage);
        	    serverMessage = dele(clientMessage);
          }
          else if (((String) clientMessage).equals("NOOP"))
          {
        	    cliPull.add(clientMessage);
        	    serverMessage = noop();
          }
          else if (((String) clientMessage).startsWith("USER"))
          {
        	    cliPull.add(clientMessage);
        	    serverMessage = user((String) clientMessage);
          }
          else if (((String) clientMessage).startsWith("USRE"))
          {
        	    cliPush.add(clientMessage);
        	    serverMessage = usre((String) clientMessage);
          }
          else if (((String) clientMessage).startsWith("PASS"))
          {
        	    cliPull.add(clientMessage);	
        	    serverMessage = pass((String) clientMessage);
          }
          else if (((String) clientMessage).startsWith("PASA"))
          {
        	    cliPush.add(clientMessage);
        	    serverMessage = pasa((String) clientMessage);
          }
          else if (((String) clientMessage).startsWith("MAIL FROM:"))
          {
        	    cliPush.add(clientMessage);
        	    serverMessage = mail_from(clientMessage);
          }
          else if (((String) clientMessage).startsWith("RCPT TO:"))
          {
        	    cliPush.add(clientMessage);
        	    serverMessage = rcpt_to(clientMessage);
          }
          else if (((String) clientMessage).equals("DATA"))
          {
        	    cliPush.add(clientMessage);
        	    serverMessage = data();
          }
          else if (((String) clientMessage).equals("RSTE"))
          {
        	    cliPush.add(clientMessage);
        	    serverMessage = rste();
          }
          else if (((String) clientMessage).equals("NOPO"))
          {
        	    cliPush.add(clientMessage);
        	    serverMessage = nopo();
          }
          else if (((String) clientMessage).equals("QUTI"))
          {
        	    cliPush.add(clientMessage);
        	    serverMessage = quti();
        	    outputToClient.writeUTF(serverMessage);
        		inputFromClient.close();
        		outputToClient.close();
          }
          else if ((dataCmdEnd((String) clientMessage) == false) && (dataCmdIncldPrev() == true))
          {
        	    cliPush.add(clientMessage);
        	    
        	    dataExtended(clientMessage);
          }
          else if (((String) clientMessage).equals("."))
          {
        	    cliPush.add(clientMessage);
        	    sendMessage();
          }
          else
          {
        	    serverMessage = "Command invalid";
          }
          // Send server response to client
          outputToClient.writeUTF(serverMessage);
        }
        
      }
      catch(IOException ex) 
      {
        ex.printStackTrace();
      } catch (AddressException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  } catch (MessagingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
      
    }).start();
  }

private String pasa(String clientMessage) {
	// Set the passPush variable to that typed by clientMessage
	passPush = clientMessage.substring(5);
		
	// Test
	System.out.println(passPush);
		
	return "+OK";
}

private String usre(String clientMessage) {
	// Set the userPush variable to that typed by clientMessage
	userPush = clientMessage.substring(5);
		
	//test
	System.out.println(userPush);
		
	return "+OK";
}

private void sendMessage() throws AddressException, MessagingException {
	// 
	Properties prop = new Properties();
    prop.put("mail.smtp.auth", "true");
    prop.put("mail.smtp.port", "465");
    prop.put("mail.smtp.host", hostPush);
    prop.put("mail.smtp.starttls.enable", "true");
    prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	
    Session session = Session.getInstance(prop,
            new javax.mail.Authenticator() {
               protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(userPush, passPush);
   	           }
            });
    
    Message mailMessage = new MimeMessage(session);
    mailMessage.setFrom(new InternetAddress(fromPush));
    mailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toPush));
    mailMessage.setSubject(subjPush);
    mailMessage.setText(contPush);
    
    Transport.send(mailMessage);
    
    System.out.println("Sent message successfully....");
  }

  private void dataExtended(String clientMessage) {
	// 
	if (clientMessage.startsWith("Subject:")) {
		subjPush = clientMessage.substring(9);
		
		// Test
		System.out.println(subjPush);
	} else {
		contPush += clientMessage;
	}
  }

// Method for returning if the cmd "DATA" was previously inputed
  private boolean dataCmdIncldPrev() {
	  
	boolean dataCmdIncld = false;
	
	for (int i = 0; i < cliPush.size(); i++)
	{
		if (cliPush.get(i).equals("DATA"))
		{
			dataCmdIncld = true;
		}
	}
	return dataCmdIncld;
  }

  private boolean dataCmdEnd(String clientMessage) {
	
	// Check to see if cli input is "."
	return clientMessage.equals(".") ? true : false;
  }

  private String quti() throws IOException {

	return "+OK";
  }

  private String nopo() {
	// TODO Auto-generated method stub
	return "+OK";
  }

  private String rste() {
	// rest userPush passPush string
	return "+OK";
  }

  private String data() {
	
	return "+OK";
  }

  private String rcpt_to(String clientMessage) {
	// Set toPush variable to the destination address
	toPush = clientMessage.substring(9);
		
	// Test
	System.out.println(toPush);
		
	return "+OK";
  }

  private String mail_from(String clientMessage) {
	// Set fromPush variable to the destination address
	fromPush = clientMessage.substring(11);
		
	// Test
	System.out.println(fromPush);
		
	return "+OK";
  }

  private String pass(String clientMessage) {
	  
	// Set the passPull variable to that typed by clientMessage
	passPull = clientMessage.substring(5);
			
	// Test
	System.out.println(passPull);
			
	return "+OK";
  }

  private String user(String clientMessage) {
	  
	// Set the userPull variable to that typed by clientMessage
	userPull = clientMessage.substring(5);
			
	//test
	System.out.println(userPull);
			
	return "+OK";
  }

  private String noop() {
	// TODO Auto-generated method stub
	return "+OK";
  }

  private String dele(String clientMessage) throws MessagingException {
	
	//create a property field
	Properties property = new Properties();

	property.put("mail.pop3.host", hostPull);
    property.put("mail.pop3.starttls.enable", "true");
    property.put("mail.pop3.port", "995");
    Session emailSession = Session.getDefaultInstance(property);
		    
    Store store = emailSession.getStore("pop3s");
    store.connect(hostPull, userPull, passPull);
		    
    // Create a folder object
    Folder folder = store.getFolder("INBOX");
    folder.open(Folder.READ_WRITE);

    // Create a Message array for storing messages
    Message[] messages = folder.getMessages();
		    
	// Get the Message attributes
    int numMessage = messages.length;
		    
    // Get the 
    int index = Integer.parseInt(clientMessage.substring(5)) - 1;
		  
    // 
    if (index < numMessage) {
	    	  
    	  Message message = messages[index];
    	  
    	  message.setFlag(Flags.Flag.DELETED, true);
    	  
    	  folder.close(true);
    	  store.close();
    	  
    	  return "+OK message deleted";
    	  
    } else {
    	 
    	  return "-ERR no such message";
    }
  }

  private String read(String clientMessage) throws MessagingException, IOException {
	
	//create a property field
	Properties property = new Properties();

    property.put("mail.pop3.host", hostPull);
    property.put("mail.pop3.starttls.enable", "true");
    property.put("mail.pop3.port", "995");
    Session emailSession = Session.getDefaultInstance(property);
	    
    Store store = emailSession.getStore("pop3s");
    store.connect(hostPull, userPull, passPull);
	    
    // Create a folder object
    Folder folder = store.getFolder("INBOX");
    folder.open(Folder.READ_ONLY);

    // Create a Message array for storing messages
    Message[] messages = folder.getMessages();
	    
	// Get the Message attributes
    int numMessage = messages.length;
	    
    // Get the 
    int index = Integer.parseInt(clientMessage.substring(5)) - 1;
	  
    // 
    if (index < numMessage) {
    	  
    	  Message message = messages[index];
    	  
    	  String subject = message.getSubject();
    	  
    	  String content = message.getContent().toString();
    	  
    	  String comb = "Subject: " + subject + "\n" + "C:" + content + "C:.";
    	  
    	  return comb;
    }
    else {
    	  return "-ERR no such message";
    }
    	
  }

  private String quit() throws IOException {
	
    
	return "+OK server signing off";
  }

  private String helo() {
	
	
	
	return "HELO";
  }

  private String list(String clientMessage) throws MessagingException 
  {
	String listString = "+OK";
	
	//create a property field
    Properties property = new Properties();

    property.put("mail.pop3.host", hostPull);
    property.put("mail.pop3.starttls.enable", "true");
    property.put("mail.pop3.port", "995");
    Session emailSession = Session.getDefaultInstance(property);
    
    Store store = emailSession.getStore("pop3s");
    store.connect(hostPull, userPull, passPull);
    
    // Create a folder object
    Folder folder = store.getFolder("INBOX");
    folder.open(Folder.READ_ONLY);

    // Create a Message array for storing messages
    Message[] messages = folder.getMessages();
    
	// Get the Message attributes
    int numMessage = messages.length;
    
    // Get the 
    int index = Integer.parseInt(clientMessage.substring(5)) - 1;
    
    if (index < numMessage)
    {
    
    		Message message = messages[index];
    
    		int sizeMessage = message.getSize();
	
    		return listString + " " + (index + 1) + " " + sizeMessage;
    }
    else
    {
    	    return "-ERR no such message";
    }
  }

private String stat() throws MessagingException 
  {
	//create a property field
    Properties property = new Properties();

    property.put("mail.pop3.host", hostPull);
    property.put("mail.pop3.starttls.enable", "true");
    property.put("mail.pop3.port", "995");
    Session emailSession = Session.getDefaultInstance(property);
    
    Store store = emailSession.getStore("pop3s");
    store.connect(hostPull, userPull, passPull);
    
    // Create a folder object
    Folder folder = store.getFolder("INBOX");
    folder.open(Folder.READ_ONLY);

    // Create a Message array for storing messages
    Message[] messages = folder.getMessages();
    
	// Get the Message attributes
    int numMessage = messages.length;
    
    	int sizeOverall = 0;
    	
    	for (int i = 0; i < numMessage; i++)
    	{
    		Message message = messages[i];
    		
    		sizeOverall += message.getSize();
    	}
	
	return "+OK" + " " + numMessage + " " + sizeOverall;
  }

  // main method
  public static void main(String[] args) {
    launch(args);
  }
}
