

import java.util.ArrayList;

public class EchoClientMain {

	public static void main(String[] args) {
		TCPEchoClient client = new TCPEchoClient();
		//client.run(args);
		
		ArrayList<Long> list250 = new ArrayList<Long>();
		long average250 = 0;
		ArrayList<Long> list500 = new ArrayList<Long>();
		long average500 = 0;
		ArrayList<Long> list750 = new ArrayList<Long>();
		long average750 = 0;
		ArrayList<Long> list1000 = new ArrayList<Long>();
		long average1000 = 0;


		int runTimes = 10000;
		
		System.out.print("****************************************************************\n");
		System.out.printf("**************** Message size = %d *******************\n", 250);
		System.out.print("****************************************************************\n");
		args[3] = "250";
		for (int i=0;i<runTimes;i++){
			client.run(args);
			//list250.add(client.run(args));
		}
		
		System.out.print("****************************************************************\n");
		System.out.printf("**************** Message size = %d *******************\n", 500);
		System.out.print("****************************************************************\n");
		args[3] = "500";
		for (int i=0;i<runTimes;i++){
			client.run(args);
			//list500.add(client.run(args));
		}
		
		System.out.print("****************************************************************\n");
		System.out.printf("**************** Message size = %d *******************\n", 750);
		System.out.print("****************************************************************\n");
		args[3] = "750";
		for (int i=0;i<runTimes;i++){
			client.run(args);
			//list750.add(client.run(args));
		}
		
		System.out.print("****************************************************************\n");
		System.out.printf("**************** Message size = %d *******************\n", 1000);
		System.out.print("****************************************************************\n");
		args[3] = "1000";
		for (int i=0;i<runTimes;i++){
			client.run(args);
			//list1000.add(client.run(args));
		}
		
		System.out.print("****************************************************************\n");
		System.out.print("\t\t\tResults\t\t\t");
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
		/*
		System.out.print("******************* Average ************************\n");
		System.out.printf("For 250: %d\n", average250/list250.size());
		System.out.printf("For 500: %d\n", average500/list500.size());
		System.out.printf("For 750: %d\n", average750/list750.size());
		System.out.printf("For 1000: %d\n", average1000/list1000.size());
		
		/*
		 * ******************* Average with 50 repetitions each ************************
			For 250messages/s: 1000100000
			For 500messages/s: 1000160000
			For 750messages/s: 1000060000
			For 1000messages/s: 1000180000
			
			All pausing execution implementations rely on the specific OS and can vary in nanoseconds.
			However the precision should be good enough.
		 */


	}

}
