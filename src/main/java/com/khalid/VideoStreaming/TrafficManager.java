package com.khalid.VideoStreaming;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TrafficManager {

	private static String primaryStream;
	private static int THRESHOLD;
	private static ServerSocket tmClientSocket;
	private static ServerSocket tmLocalServerSocket;
	private static ArrayList<Socket> primaryClients;
	private static ArrayList<Socket> secondaryClients;
	private static ArrayList<TrafficManagerWorker> workers;
	private static boolean isLocalReady = false;
	private SwithHandler handler = null;
	public static void main(String[] args) throws Exception {

		if (args.length <= 1 || args[0].toLowerCase().equals("help")) {
			System.out.println("\nUsage: ");
			System.out.println("TrafficManager.jar primary_stream_url threshold\n Example: ");
			System.out.println("java -jar TrafficManager.jar http://192.168.1.101:8080/ 3");
			System.exit(1);
		}

		new TrafficManager(args);
		
	}
	
	public TrafficManager(final String[] args) throws IOException, InterruptedException
	{
		primaryStream = args[0];
		THRESHOLD = Integer.parseInt(args[1]);

		String secondaryStream = "";
		short tmLocalPort = 6000;
		short tmClientPort = 6001;

		// Connect to local server
		System.out.println("Waiting for connection with LSS...");

		tmLocalServerSocket = new ServerSocket(tmLocalPort);

		Socket localServerSocket = tmLocalServerSocket.accept(); 
		System.out.println("Established connection with LSS.");

		BufferedReader localServerReader = new BufferedReader(
				new InputStreamReader(localServerSocket.getInputStream()));
		DataOutputStream localServerWriter = new DataOutputStream(
				localServerSocket.getOutputStream());
		localServerWriter.writeBytes(primaryStream + "\n"); 
		
		// Open connection with clients
		//ArrayList<Socket> clientsList = new ArrayList<Socket>();
		primaryClients = new ArrayList<Socket>();
		secondaryClients = new ArrayList<Socket>();
		workers = new ArrayList<TrafficManagerWorker>();
		
		tmClientSocket = new ServerSocket(tmClientPort);
		Socket clientSocket = null;
		
		// add shutdown hook
		shutdownHook();
		TrafficManagerWorker worker = null;
		Thread workerThread = null;
		while (true) {
			System.out.println("\nWaiting for connection from client....");
			clientSocket = tmClientSocket.accept();

			// check if any client disconnected from the TM
			if (primaryClients.size() >= THRESHOLD) {
				
				
				//Read stream from local server
				if(!isLocalReady)
				{
					localServerWriter.writeBytes("SWITCH" + "\n");
					System.out.println("\nSwitched to local server...");
					secondaryStream = localServerReader.readLine(); 
					isLocalReady = true;
					System.out.println("Secondary stream received: " + secondaryStream);
					Thread.sleep(1000);
				}
				
				//Create client thread
				worker = new TrafficManagerWorker(clientSocket, "Local", secondaryStream,this);
				secondaryClients.add(clientSocket);
			} else {
				worker = new TrafficManagerWorker(clientSocket, "Primary", primaryStream,this);
				primaryClients.add(clientSocket);
			}
			workers.add(worker);
			workerThread = new Thread(worker);
			workerThread.start();
			
			updateConsole();
			//make sure client doesn't start before secondary server
		}
	}

	private static void updateConsole() {
		//print total number of clients
		System.out.println();
		System.out.println("Current total clients on Primary: " + primaryClients.size());
		System.out.println("Current total clients on Secondary: " + secondaryClients.size());
		System.out.println();
	}
	
	public void workerNotification(Socket disconnectedClient, String streamingServer) throws InterruptedException
	{
		System.out.println();
		if(streamingServer.equals("Primary"))
		{
			primaryClients.remove(disconnectedClient);
			System.out.println("Removed "+ disconnectedClient.getInetAddress()+ " from Primary list");
		}
		else
		{
			secondaryClients.remove(disconnectedClient);
			System.out.println("Removed "+disconnectedClient.getInetAddress()+ " from Local list");
		}
		updateConsole();
		
		if(streamingServer.endsWith("Local") || secondaryClients.isEmpty())
			return;
		
		//Choose and remove last connected client from local server
		Socket chosenSocket = secondaryClients.remove(0);
		System.out.println("\nSelected client from local server: " + chosenSocket.getInetAddress());
		//Update lists
		primaryClients.add(chosenSocket);
		updateConsole();
		
		//Change the streaming server of the selected socket
		for(TrafficManagerWorker worker : workers)
		{
			if(worker.getClientSocket() == chosenSocket)
			{
				System.out.println("------------------------------");
				System.out.println("\nChanging chosen client's streaming server label...");
				System.out.println("Previous: " + worker.getStreamingServer());
				worker.setStreamingServer("Primary");
				System.out.println("Current: " + worker.getStreamingServer());
				System.out.println("------------------------------");
				break;
			}
		}
		
		//Spawn SwitchHanlder thread to send stream to client
		System.out.println("Switch handling thread started");
	    handler = new SwithHandler(chosenSocket, primaryStream);
		Thread th = new Thread(handler);
		th.start();
	}
	
	private static void shutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Close the client socket
				try {
					tmClientSocket.close();
				} catch (IOException e) {
					System.out.println("\nERROR: Cannot close client socket in TM Server.");
					e.printStackTrace();
				}

				// close the local server socket
				try {
					tmLocalServerSocket.close();
				} catch (IOException e) {
					System.out.println("\nERROR: Cannot close local server socket in TM Server.");
					e.printStackTrace();
				}
				
				
			}
		});
	}

}