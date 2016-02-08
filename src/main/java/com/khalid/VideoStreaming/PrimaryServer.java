package com.khalid.VideoStreaming;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

public class PrimaryServer {

	private static String primaryIp;
	public static void main(String[] args) throws InterruptedException {

		 if(args.length <= 1) {
	            System.out.println("\nUsage:\n");
	            System.out.println("java -jar PrimaryServer primary_ip media_file. E.g.\n");
	            System.out.println("java -jar 192.168.0.100 /var/www/html/live.m3u8\n");
	            System.exit(1);
	        }

		    primaryIp = args[0];
	        String media = args[1];
	        String options = formatHttpStream(primaryIp, 8080);

	        System.out.println("Streaming '" + media + "' to '" + options + "'");

	        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(args);
	        HeadlessMediaPlayer mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
	        mediaPlayer.playMedia(media, options);

	        // Don't exit
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



