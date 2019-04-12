import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;

public class edge_create implements Runnable{
  static volatile List<Integer[]> available_vertex_list=new ArrayList<Integer[]>();//list of available vertices
  static int max_vertices=0; 
  static int max_edges_per_vertex=0;
  static int edge_radius=0;
  static AtomicBoolean completed=new AtomicBoolean(false);//boolean that indicates that threads have completed
  static AtomicBoolean initialised=new AtomicBoolean(false);//boolean that indicates that the board has been initialised and edge formation can start
  static Random ra = new Random();
  static int complete_count=0;
  Integer current_index[] = new Integer[2];//x,y positions of the vertex assigned to current thread
  int direction[];
  static volatile HashMap<String, Boolean> working_list = new HashMap<String, Boolean>();//hashmap that indicates if a point is available to be a vertex
  static{
    for(int i=0;i<tile.board_size_x;i++){
      for(int j=0;j<tile.board_size_y;j++){
        working_list.put((Integer.toString(i)+","+Integer.toString(j)), false);//add the board to the hashmap
      }
    }
    
  }
    
  static int[][] directions= {//directions 1,1 indicates north east, 1,0 is north 0,1 is east etc..
    {1,1},
    {1,0},
    {0,1},
    {-1,-1},
    {0,-1},
    {-1,0},
    {1,-1},
    {-1,1}
  };
  
  static synchronized void update_completion_count(){//add to vertex count
    complete_count+=1;
    if(complete_count>=max_vertices){
      completed.set(true);
    }
  }
  
  
  static synchronized boolean modify_stack(Integer[] c, boolean is_push){//get or add  vertex from/to a list in a synchronised way
    if(is_push){
      Integer[] dup = {c[0],c[1]};
      if(working_list.get((Integer.toString(dup[0])+","+Integer.toString(dup[1])))){//already in the list
          return false;
        }
      working_list.put((Integer.toString(dup[0])+","+Integer.toString(dup[1])),true);
      available_vertex_list.add(dup);//add to list
    }
    else{
      if(available_vertex_list.size() > 0){
        Integer[] recv = available_vertex_list.get(ra.nextInt(available_vertex_list.size()));
        available_vertex_list.remove(recv); //remove from list and return
        c[0]=recv[0];
        c[1]=recv[1];
        if(!working_list.get((Integer.toString(c[0])+","+Integer.toString(c[1])))){
          return false;
        }
        else{
          working_list.put((Integer.toString(c[0])+","+Integer.toString(c[1])),false);//mark off vertex from availability
        }
        
      }
      else{
        return false;
      }
    }
    return true;
  }
  
  static synchronized boolean obtain_vertex(edge_create ec){
    if(completed.get()){
      return false;
    }
    if(modify_stack(ec.current_index,false)){
      return true;
    }
    return false;
  }
  
  static void do_edge_creation(int thread_count){
    
    tile.board[0][0].piece_at_position= new piece(true);
    tile.board[tile.board_size_x-1][tile.board_size_y-1].piece_at_position= new piece(true);
    tile.generate_edge(tile.board[0][0],tile.board_size_y-2,1,0);//generate the four sides to the plane
    tile.generate_edge(tile.board[0][0],tile.board_size_x-2,0,1);
    tile.generate_edge(tile.board[tile.board_size_x-1][tile.board_size_y-1],tile.board_size_y-3,0,-1);
    tile.generate_edge(tile.board[tile.board_size_x-1][tile.board_size_y-1],tile.board_size_x-3,-1,0);
    Integer[] p1={0,0};
    Integer[] p2={tile.board_size_x-1,tile.board_size_y-1};
    edge_create.modify_stack(p1,true);//add vertices to stack
    edge_create.modify_stack(p2,true);
    //printy();
    Thread[] th_arr=new Thread[thread_count];
    
    for(int i=0;i<thread_count;i++){
      th_arr[i]=new Thread(new edge_create());
      th_arr[i].start();//use threads to create edges
    }
    for(int i=0;i<thread_count;i++){
      try{
        th_arr[i].join();
      }
      catch(InterruptedException err){
        
      }
    }
    
  }
  
  edge_create(){}
  
  synchronized static void printy(){
    tile.print_board();
  }
  
  public void run(){
    
    
    Random rand = new Random();
    while(!completed.get()){ //while not completed
      
      if(obtain_vertex(this)){
        
        direction = directions[rand.nextInt(directions.length)]; //generate random direction for path
        int temp_radius=rand.nextInt(edge_radius);//generate length of this path
        if(!tile.generate_edge(tile.board[current_index[0]][current_index[1]],temp_radius,direction[0],direction[1])){//try reserve everything in the path
          edge_create.modify_stack(current_index,true);
        }
        else{
          update_completion_count();//add to vertex count
          edge_create.modify_stack(current_index,true);
        }
         
      }
      try{
        Thread.sleep(rand.nextInt(5));//wait for vertex availability
      }
      catch(InterruptedException ex){
        
      }
      
    }
  }
  
  
}