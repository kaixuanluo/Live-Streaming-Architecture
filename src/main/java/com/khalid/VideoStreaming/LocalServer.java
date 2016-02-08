package com.khalid.VideoStreaming;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

public class LocalServer {

	private static String secondaryStream;
	private static String primaryServerStream;
	private static String secondaryIp;
	private static String tmIP;
	private static short tmPort;

	public static void main(String[] args) throws Exception {

		if (args.length <= 1 || args[0].toLowerCase().equals("help")) {
			System.out.println("\nUsage: ");
			System.out
					.println("LocalServer.jar secondary_ip traffic_manager_ip traffic_manager_port\n Example: ");
			System.out
					.println("java -jar LocalServer.jar 192.168.1.102 192.168.1.101 6000\n");
			System.exit(1);
		}

		secondaryIp = args[0];
		tmIP = args[1];
		tmPort = Short.parseShort(args[2]);

		Socket localSocket = new Socket(tmIP, tmPort);
		BufferedReader localReader = new BufferedReader(new InputStreamReader(
				localSocket.getInputStream()));
		DataOutputStream localWriter = new DataOutputStream(
				localSocket.getOutputStream());

		//receive primary server stream from traffic
		primaryServerStream = localReader.readLine();
		String options = formatHttpStream(secondaryIp, 8080);
		System.out.println("Streaming '" + primaryServerStream + "' to '" + options + "'");
	
		//send secondary stream URL to the traffic manager
		secondaryStream = "http://"+secondaryIp+":8080/";
		localWriter.writeBytes(secondaryStream + "\n");
		System.out.println("Secondary stream sent to traffic manager: " + secondaryStream);
		// wait for switch signal from traffic manager
		String signal = localReader.readLine();
		if (signal == null) {
			System.out.println("NO SIGNAL RECEIEVED!!...CLOSING CONNECTION");
			localSocket.close();
			System.exit(1);
		}
		
		//stream media after signal received from traffic manager
		MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(args);
		HeadlessMediaPlayer mediaPlayer = mediaPlayerFactory
				.newHeadlessMediaPlayer();
		
		mediaPlayer.playMedia(primaryServerStream, options);
		Thread.currentThread().join();
	}

	private static String formatHttpStream(String serverAddress, int serverPort)
	{
		 StringBuilder sb = new StringBuilder(60);
	        sb.append(":sout=#duplicate{dst=std{access=http,mux=ts,");
	        sb.append("dst=");
	        sb.append(serverAddress);
	        sb.append(':');
	        sb.append(serverPort);
	        sb.append("}}");
	        return sb.toString();
	}
}
