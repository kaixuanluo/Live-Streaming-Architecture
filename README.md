#Live Video Streaming Architecture 

This is the implementation of a live video streaming architecture. It is the prototype of a research that I have done on video streaming
distributed architectures. The novel idea in this research is a unique way of doing load balancing and a new way of using HTTP Live Streaming. 

This architecture addresses one thing mainly: the efficient usage of resources during high traffic peak periods. 
For example, if you are a video content provider how are you going to address the huge amount of traffic that will come from a specific region 
during the new president's inauguration?

The load balancing mechanims works by using a temporary secondary server. That server will only be used when during high traffic period in
a specific region. Unlike other architectures, the secondary server is not a duplicate of the original server. Howerver, it streams from 
the original server and re-streams to its own base of clients, thus reducing load on the original server. 

The architecture also makes use of Apple's HLS protocol. However, instead of doing bitrate adaptation from the client side, we are doing 
bitrate adaption from the server side. What does this mean? This will allow the server to stream good quality video when the its bandwidth is high, 
and server low quality video when the traffic increases and its bandwidth drops. 

#Implementation details
Operating System: Ubuntu 14.04 <br/>
Web server:  Apache2 <br/>
Programming Language: Java <br/>
Video segmentation script: HLS-Stream-Creator by Ben Tasker (https://github.com/bentasker/HLS-Stream-Creator). <br/>

#Execution order of the programs 
There are three programs in this project: PrimaryServer, TrafficManager, LocalServer and Client <br/>
Run the primary server </br>
Run the Traffic Manager </br>
Run the local server </br>
Run the client </br>

Primary Server Usage
$ java -jar PrimaryServer.java ip_address m3u8_url
</br>
Traffic Manager Usage
$java -jar TrafficManager.java primary_server_stream_url threshold
</br>
Local Server Usage
$java -jar LocalServer.java local_ip traffic_manager_ip traffic_manager_port
</br>
Client usage
$java -jar Client.java traffic_manager_ip traffic_manager_port

#Screenshots
![Screenshot](Images/client-3.png)
![Screenshot](Images/client-synchronization.png)
![Screenshot](Images/trafficmanager-3-tracking.png)
