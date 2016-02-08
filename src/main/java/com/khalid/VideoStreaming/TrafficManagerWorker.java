package com.khalid.VideoStreaming;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class TrafficManagerWorker implements Runnable {

	protected Socket clientSocket = null;
	protected String streamingServer = null;
	protected String stream = null;
	protected TrafficManager manager = null;
	protected boolean isRunning = false;
	public TrafficManagerWorker(Socket clientSocket, String server, String stream, TrafficManager manager)
	{
		this.clientSocket = clientSocket;
		this.streamingServer = server;
		this.stream = stream;
		this.manager = manager;
	}
	
	public void run() {
		
		InetAddress clientIp = clientSocket.getInetAddress();
		System.out.println("Connected to client: " + clientIp.getHostAddress());
		try {
			
			DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
			outputStream.writeBytes(stream+"\n");
			System.out.println("Stream sent to client is: " + stream);
			//Waiting for this client to shutdown/terminate
			BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String clientMessage = br.readLine();
			System.out.println("Message recevied from client: " + clientMessage);
			if(clientMessage != null && clientMessage.equals("shutdown"))
			{
				System.out.println("Client " + clientSocket.getInetAddress()+" disconnected "+
							"from "+ streamingServer);
				manager.workerNotification(clientSocket, streamingServer);
				System.out.println("Came back from worker notification in TrafficManager...yay!");
			}
			
		} catch (IOException e) {
			System.out.println("\nError @ thread run..TrafficManagerWorker\n");
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("\nError @ thread run..TrafficManagerWorker\n");
			e.printStackTrace();
		}
		
	}
	
	public boolean isAlive()
	{
		return Thread.currentThread().isAlive();
	}

	public Socket getClientSocket()
	{
		return clientSocket;
	}
	
	public String getStreamingServer()
	{
		return streamingServer;
	}
	
	public void setStreamingServer(String stream)
	{
		streamingServer = stream;
	}
}




