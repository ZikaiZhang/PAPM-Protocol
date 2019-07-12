package client;

import java.io.*;
import java.net.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Client extends Application 
{
  // Set data stream between client and server
  DataOutputStream toServer = null;
  
  DataInputStream fromServer = null;

  @Override
  public void start (Stage initialStage) 
  {
    // Set panel for holding the label and command line input field
    BorderPane textField = new BorderPane();
    
    textField.setPadding(new Insets(8, 8, 8, 8)); 
    
    textField.setStyle("-fx-border-color: green");
    
    textField.setLeft(new Label("Enter a command: "));
    
    TextField newTextField = new TextField();
    
    textField.setCenter(newTextField);

    newTextField.setAlignment(Pos.BOTTOM_RIGHT);
    
    
    BorderPane pane = new BorderPane();
    // set text area for displaying scripts
    TextArea newTextArea = new TextArea();
    
    pane.setCenter(new ScrollPane(newTextArea));
    
    pane.setTop(textField);
    
    // Set a scene
    Scene scene = new Scene(pane, 480, 240);
    
    initialStage.setTitle("Client");
    
    initialStage.setScene(scene);
    
    initialStage.show();
    
    newTextField.setOnAction(e -> 
    {
      try {
        // Get the client side command
        String clientMessage = newTextField.getText();
  
        // Send the command to the server
        toServer.writeUTF(clientMessage);
        toServer.flush();
  
        // Get response from the server
        String serverMessage = fromServer.readUTF();
  
        // Display
        newTextArea.appendText("C:" + clientMessage + "\n");
        newTextArea.appendText("S:" + serverMessage + "\n");
      }
      catch (Exception ex) 
      {
        System.err.println(ex);
      }
    });
  
    try {
      // Create a socket for connecting to the server
      Socket socket = new Socket("localhost", 49153);

      // Create an input stream for getting response from the server
      fromServer = new DataInputStream(socket.getInputStream());

      // Create an output stream for sending command to the server
      toServer = new DataOutputStream(socket.getOutputStream());
    }
    catch (IOException ex) {
      newTextArea.appendText(ex.toString() + '\n');
    }
  }

  
  //main method
  public static void main(String[] args) {
    launch(args);
  }
}
