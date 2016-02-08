package com.khalid.VideoStreaming;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SwithHandler implements Runnable {

	protected Socket clientSocket;
	protected String stream;
	public SwithHandler(Socket clientSocket, String stream)
	{
		this.clientSocket = clientSocket;
		this.stream = stream;
	}
	public void run() {
		
		//Send new stream to client socket
		try {
			
			System.out.println("Writing new stream to client socket");
			DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
			outputStream.writeBytes(stream + "\n");
			
			System.out.println("Sent new stream to client");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
