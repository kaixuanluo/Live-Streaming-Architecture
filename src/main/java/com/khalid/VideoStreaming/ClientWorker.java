package com.khalid.VideoStreaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientWorker implements Runnable {
	
	protected Socket clientSocket = null;
	private Client client = null;
	public ClientWorker(Socket clientSocket, Client client)
	{
		this.clientSocket = clientSocket;
		this.client = client;
	}
	
	public void run()
	{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String signal = br.readLine();
			System.out.println("Received new stream...");
			client.ChangeStreamingServer(signal);
			System.out.println("Came back from changing stream server in Client.");
		} catch (IOException e) {
			System.out.println("Client thread is closed.");
		}
	}

}
