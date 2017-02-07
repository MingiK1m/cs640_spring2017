import java.net.*;
import java.io.*;

public class Iperfer{
	public static void arg_err(){
		System.out.println("Error: missing or additional arguments");
	}
	public static void port_err(){
		System.out.println("Error: port number must be in the range 1024 to 65535");
	}
	public static void main(String[] args){
		if(args.length==0){
			arg_err();
			return;
		}

		/* Client */
		if(args[0].equals("-c")){
			// 1. Check for the fixed arguments 
			if(args.length!=7||!args[1].equals("-h")||!args[3].equals("-p")||!args[5].equals("-t")){
				arg_err();
				return;
			}
			// 2. Parse the arguments.
			String host = args[2];
			int portNumber=-1;
			int run_time=-1;
			
			try {
				portNumber = Integer.parseInt(args[4]);
				run_time = Integer.parseInt(args[6]);				
			} catch(Exception e){
				e.printStackTrace();
				return;
			}

			if(portNumber<1024||portNumber>65535){
				port_err();
				return;
			}
			
			if(run_time < 1){
				arg_err();
				return;
			}
			
			try{
				// 3. Create and connect socket, get outputstream obj to write
				Socket clientSoc;
				OutputStream out;
				
				clientSoc = new Socket(host,portNumber);
				out = clientSoc.getOutputStream();

				// 4. Keep writing 1000 bytes during the run_time 
				int total_kb_written = 0;
				
			    byte[] data = new byte[1000]; // auto-init as all 0s
				
				long t1, t2;
				t1 = System.nanoTime();
				
				while(true){
					t2 = System.nanoTime();
					if((t2-t1)/1000000000.0>run_time){
						break;
					}
					out.write(data);
					out.flush();
					total_kb_written++;
				}

				// 5. Close socket
				clientSoc.close();
				
				// 6. Print result
				System.out.println("sent="+ total_kb_written +"KB rate="+ (total_kb_written/run_time/125.0) +"Mbps");
			}
			catch(Exception e){
				e.printStackTrace();
				return;
			}
		}
		/* Server */
		else if(args[0].equals("-s")){
			// 1. Check for the fixed arguments 
			if(args.length!=3||!args[1].equals("-p")){
				arg_err();
				return;
			}

			// 2. Parse the arguments.
			int serverPort = -1;
			
			try{
				serverPort = Integer.parseInt(args[2]);
			} catch (Exception e){
				e.printStackTrace();
				return;
			}
			
			if(serverPort<1024||serverPort>65535){
				port_err();
				return;
			}
			
			try{
				// 3. Create server socket and wait for connection
				ServerSocket serverSoc;
				Socket clientSoc;
				
				serverSoc = new ServerSocket(serverPort);			
				clientSoc=serverSoc.accept();
				
				InputStream in=clientSoc.getInputStream();
				
				// 4. Keep reading data until the input stream closed.
				byte[] buffer = new byte[1000];
				long total_bytes_read = 0;
				int bytes_read;
				
				long t1, t2;
				t1 = System.nanoTime();
				
				while((bytes_read = in.read(buffer,0,1000)) > 0){
					total_bytes_read += bytes_read;
				}
				
				t2 = System.nanoTime();

				// 5. Close socket
				clientSoc.close();
				serverSoc.close();
				
				// 6. Print result
				System.out.println("received="+ (total_bytes_read/1000) +"KB rate="+ ((total_bytes_read/1000)/((t2-t1)/1000000000.0)/125) +"Mbps");
			}
			catch(Exception e){
				e.printStackTrace();
				return;
			}
		}
		else{
			arg_err();
			return;
		}
	}
}
