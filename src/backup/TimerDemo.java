package backup;

import java.util.Timer;
import java.util.TimerTask;

public class TimerDemo {
	   public static void main(String[] args) {
	   // creating timer task, timer
	   TimerTask task = new SendReceiveTimer();
	   Timer timer = new Timer();
	      
	   // scheduling the task at fixed rate delay
	   timer.schedule(task,500);      
	   } 
	   
	   static class SendReceiveTimer extends TimerTask{
		   @Override
			public void run() {
			   System.out.println("working at fixed rate delay");      				
			}
	   }
	   

		
	}
