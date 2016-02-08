package com.khalid.VideoStreaming;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

public class Client {

	private final JFrame frame;
	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

	private static String tmIP;
	private static short tmPort;
	private static Socket clientSocket;
	private Thread clientThread;

	public static void main(final String[] args) {
		
		if(args.length <= 1 || args[0].toLowerCase().equals("help"))
		{
			System.out.println("\nUsage: ");
			System.out.println("Client.jar traffic_manager_ip traffic_manager_port\n Example: ");
			System.out.println("java -jar Client.jar 192.168.1.101 6000\n");
			System.exit(1);
		}
		
		new NativeDiscovery().discover();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					new Client(args);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Client(String[] args) throws IOException {

		tmIP = args[0];
		tmPort = Short.parseShort(args[1]);

		clientSocket = new Socket(tmIP, tmPort);
		BufferedReader tmReader = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));

		// get stream from traffic manager
		System.out.println("Waiting for stream URL from traffic manager...");
		String stream = tmReader.readLine();
		System.out.println("Received stream: " + stream);
		if(stream == null || stream == "")
		{
			System.out.println("\nNo stream url receieved. Closing...");
			exit(1);
		}

		frame = new JFrame("Streaming from: " + stream);
		frame.setBounds(100, 100, 600, 400);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			
			@Override 
			public void windowClosing(WindowEvent e)
			{
				//System.out.println(e);
				exit(0);
			}
		});
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		frame.setContentPane(mediaPlayerComponent);
		frame.setVisible(true);

		mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(
				new MediaPlayerEventAdapter() {

					public void stopped(MediaPlayer player) {
						exit(0);
					}

					public void finished(MediaPlayer player) {
						exit(0);
					}

					public void error(MediaPlayer player) {
						exit(1);
					}

				});
		
		//Start the thread if this client belongs to the local server initially
		ClientWorker worker = new ClientWorker(clientSocket, this);
		clientThread = new Thread(worker);
		clientThread.start();
		
		//Start playing the media
		start(stream);
	}

	private void start(String mrl) {
		mediaPlayerComponent.getMediaPlayer().playMedia(mrl);
	}

	public void ChangeStreamingServer(String url)
	{
		System.out.println("Changing to primary server...");
		frame.setTitle(frame.getTitle() +" - Switching... to another server.");
		mediaPlayerComponent.getMediaPlayer().playMedia(url);
		frame.setTitle("Switched to "+ url);
	}
	
	private void exit(int status) {
		mediaPlayerComponent.release();
		try {
			
			System.out.println("Shutting down...sending message.");
			DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
			outputStream.writeBytes("shutdown");
			System.out.println("Sent shutdown message successfully!");
			clientSocket.close();
		} catch (IOException e) {
			System.out.println("ERROR: cannot close socket.");
			e.printStackTrace();
		}

		System.exit(status);
	}
}
