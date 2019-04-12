public class obstacle_create implements Runnable{
  static int obstacle_x=5;//dimensions of the obstacle
  static int obstacle_y=5;
  static int obstacles_created=0; 
  static int obstacle_count=20;
  static void create_all(int thread_count){
     

    Thread[] th_arr=new Thread[thread_count];
    
    for(int i=0;i<thread_count;i++){
      th_arr[i]=new Thread(new obstacle_create());
      th_arr[i].start();
    }
    for(int i=0;i<thread_count;i++){
      try{
        th_arr[i].join();//create multithreaded obstacles
      }
      catch(InterruptedException err){
        
      }
      
    }
    
  }
  
  static synchronized boolean obtain_obstacle(){//create obstacle
    if(obstacles_created<obstacle_count){
      obstacles_created+=1;
      return true;
    }
    return false;
  }
  
  obstacle_create(){
  }
  
  
  
  public void run(){
    while(obtain_obstacle()){
      tile.generate_obstacle();//use tile function to generate obstacle
    }    
  }
}