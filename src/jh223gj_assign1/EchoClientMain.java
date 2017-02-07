package jh223gj_assign1;

import java.util.ArrayList;
import java.util.Scanner;

public class EchoClientMain {

	public static void main(String[] args) {
		AbstractClient client = new TCPEchoClient();
		//AbstractClient client = new TCPEchoClientTimer();
		//AbstractClient client = new UDPEchoClientTimer();
		//AbstractClient client = new UDPEchoClient();
		
		int runTimes = 5;
		
		Scanner scan = new Scanner(System.in);
		System.out.println("Normal Mode [N]\nHome Mode [H]\nMessage Transfer Rate Mode [T]\nLocal Mode [L]\nDefault: Normal Mode ");
		String input = scan.nextLine();
		switch (input) {
			case "N" : normalMode(client, args, runTimes);
				break;
			case "H" : homeMode(client, args, runTimes);
				break;
			case "T" : mtrMode(client, args, runTimes);
				break;
			case "L" : localMode(client, args, runTimes);
			default : normalMode(client, args, runTimes);
				break;
		}
		
		scan.close();

	}
	
	public static void localMode(AbstractClient client, String[] args, int runTimes){
		args[0] = "127.0.0.1";
		normalMode(client, args, runTimes);
	}
	
	public static void homeMode(AbstractClient client, String[] args, int runTimes){
		args[0] = "10.0.0.106";
		normalMode(client, args, runTimes);
	}
	
	public static void normalMode(AbstractClient client, String[] args, int runTimes){
		for (int i=0;i<runTimes;i++)  client.run(args);
	}
	
	public static void mtrMode(AbstractClient client, String args[], int runTimes){

		ArrayList<Long> list250 = new ArrayList<Long>();
		long average250 = 0;
		ArrayList<Long> list500 = new ArrayList<Long>();
		long average500 = 0;
		ArrayList<Long> list750 = new ArrayList<Long>();
		long average750 = 0;
		ArrayList<Long> list1000 = new ArrayList<Long>();
		long average1000 = 0;
		
		
		System.out.print("****************************************************************\n");
		System.out.printf("**************** Message size = %d *******************\n", 250);
		System.out.print("****************************************************************\n");
		args[3] = "250";
		for (int i=0;i<runTimes;i++){
			list250.add(client.run(args));
			System.out.println("Turn: " + i);
		}
		
		System.out.print("****************************************************************\n");
		System.out.printf("**************** Message size = %d *******************\n", 500);
		System.out.print("****************************************************************\n");
		args[3] = "500";
		for (int i=0;i<runTimes;i++){
			list500.add(client.run(args));
			System.out.println("Turn: " + i);
		}
		
		System.out.print("****************************************************************\n");
		System.out.printf("**************** Message size = %d *******************\n", 750);
		System.out.print("****************************************************************\n");
		args[3] = "750";
		for (int i=0;i<runTimes;i++){
			list750.add(client.run(args));
			System.out.println("Turn: " + i);
		}
		
		System.out.print("****************************************************************\n");
		System.out.printf("**************** Message size = %d *******************\n", 1000);
		System.out.print("****************************************************************\n");
		args[3] = "1000";
		for (int i=0;i<runTimes;i++){
			list1000.add(client.run(args));
			System.out.println("Turn: " + i);
		}
		
		System.out.print("****************************************************************\n");
		System.out.print("\t\t\tResults\t\t\t\n");
		System.out.print("****************************************************************\n");
		System.out.print("For 250: \n");
		for (int i=0; i<list250.size();i++){
			System.out.println(list250.get(i));
			average250 += list250.get(i);
		}
		System.out.print("\nFor 500: \n");
		for (int i=0; i<list500.size();i++){
			System.out.println(list500.get(i));
			average500 += list500.get(i);
		}
		System.out.print("\nFor 750: \n");
		for (int i=0; i<list750.size();i++){
			System.out.println(list750.get(i));
			average750 += list750.get(i);
		}
		System.out.print("\nFor 1000: \n");
		for (int i=0; i<list1000.size();i++){
			System.out.println(list1000.get(i));
			average1000 += list1000.get(i);
		}
		
		System.out.print("******************* Average ************************\n");
		System.out.printf("For 250: %d\n", average250/list250.size());
		System.out.printf("For 500: %d\n", average500/list500.size());
		System.out.printf("For 750: %d\n", average750/list750.size());
		System.out.printf("For 1000: %d\n", average1000/list1000.size());
		
		/*
		 * ******************* Average UDP with 50 repetitions each ************************
			For 250messages/s: 1000100000
			For 500messages/s: 1000160000
			For 750messages/s: 1000060000
			For 1000messages/s: 1000180000
			
			All pausing execution implementations rely on the specific OS and can vary in nanoseconds.
			However the precision should be good enough.
		 */
	}

}
