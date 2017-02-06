import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Iperfer {
	static final int MODE_SERVER = 0;
	static final int MODE_CLIENT = 1;
	
	static final int BUFFER_SIZE = 1000;
	
	static final long NS_TO_SEC = 1000000000;

	public static void main(String[] args) {
		int mode=-1;
		int port=-1, time=-1;
		String hostname = "";
		
		long time_start_ns, time_end_ns;
		
		int ret;
		long total_bytes = 0;
		byte buffer[] = new byte[BUFFER_SIZE]; // initialized as 0 by default
		
		// 1. Parse arguments
		if(args.length == 7){
			if(args[0].equals("-c")){
				mode = MODE_CLIENT;
				if(args[1].equals("-h")){
					hostname = args[2];
					
					if(args[3].equals("-p")){
						try{
							port = Integer.parseInt(args[4]);
						} catch (Exception e){
							e.printStackTrace();
							System.exit(1);
						}
						
						if(port<1024 || port>65535){
							System.out.println("Error: port number must be in the range 1024 to 65535");
							System.exit(1);
						}
						
						if(args[5].equals("-t")){
							try{
								time = Integer.parseInt(args[6]);
							} catch (Exception e){
								e.printStackTrace();
								System.exit(1);
							}
							
							if(time <= 0){
								System.out.println("Error: time must be greater than 0");
								System.exit(1);
							}
						} else {
							System.out.println("Error: Cannot find time argument");
							System.exit(1);
						}
					} else {
						System.out.println("Error: Cannot find port number argument");
						System.exit(1);
					}
				} else {
					System.out.println("Error: Cannot find host name argument");
					System.exit(1);
				}
			}
		} else if (args.length == 3){
			if(args[0].equals("-s")){
				mode = MODE_SERVER;
				if(args[1].equals("-p")){
					try{
						port = Integer.parseInt(args[2]);
					} catch (Exception e){
						e.printStackTrace();
						System.exit(1);
					}
					
					if(port<1024 || port>65535){
						System.out.println("Error: port number must be in the range 1024 to 65535");
						System.exit(1);
					}
				} else {
					System.out.println("Error: Cannot find port number argument");
					System.exit(1);
				}
			}
		} else {
			System.out.println("Error: missing or additional arguments");
			System.exit(1);
		}
		
		
		// 2. Run server/client 
		switch(mode){
		case MODE_SERVER:
			try {
				// 1. Create server socket
				ServerSocket srv_sock = new ServerSocket(port);
				// 2. Block and accept
				Socket cli_sock = srv_sock.accept();
				// 3. Read data from input stream
				InputStream data_in = cli_sock.getInputStream();
				
				time_start_ns = System.nanoTime();
				while(true){
					ret = data_in.read(buffer);
					if(ret < 0) break;
					total_bytes += ret;
				}
				time_end_ns = System.nanoTime();
				
				cli_sock.close();
				srv_sock.close();
				
				// 4. Print results
				double time_elapse_sec = (double)(time_end_ns - time_start_ns)/NS_TO_SEC;
				long rcv_kb = total_bytes/1000; // 1000B as 1KB
				double rate = (double)total_bytes/(1000*1000/*Mega*/)*8/*Bytes to bit*//time_elapse_sec; 
				System.out.println("received=" + rcv_kb + "KB rate=" + rate + "Mbps");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			break;
		case MODE_CLIENT:
			try {
				// 1. Create/connect socket
				Socket sock = new Socket(hostname, port);
				// 2. Write data for <time> seconds
				OutputStream data_out = sock.getOutputStream();
				
				time_start_ns = System.nanoTime();
				while(true){
					data_out.write(buffer);
					total_bytes += BUFFER_SIZE;
					time_end_ns = System.nanoTime();
					if(time_end_ns - time_start_ns > time*NS_TO_SEC){
						break;
					}
				}
				sock.close();
				
				// 3. Print results
				double time_elapse_sec = (double)(time_end_ns - time_start_ns)/NS_TO_SEC;
				long rcv_kb = total_bytes/1000; // 1000B as 1KB
				double rate = (double)total_bytes/(1000*1000/*Mega*/)*8/*Bytes to bit*//time_elapse_sec; 
				System.out.println("sent=" + rcv_kb + "KB rate=" + rate + "Mbps");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			break;
		default:
			// cannot reach here
		}
	}
}
