import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class edge_create implements Runnable{
  static List<Integer[]> available_vertex_list=new ArrayList<Integer[]>();
  static int max_vertices=0; 
  static int max_edges_per_vertex=0;
  static int edge_radius=0;
  static AtomicBoolean completed=new AtomicBoolean(false);
  static AtomicBoolean initialised=new AtomicBoolean(false);
  static Random ra = new Random();
  static int complete_count=0;
  Integer current_index[] = new Integer[2];
  int direction[];
  static int[][] directions= {
    {1,1},
    {1,0},
    {0,1},
    {-1,-1},
    {0,-1},
    {-1,0},
    {1,-1},
    {-1,1}
  };
  
  static synchronized void update_completion_count(){
    complete_count+=1;
    if(complete_count>=max_vertices){
      completed.set(true);
    }
  }
  
  
  static synchronized boolean modify_stack(Integer[] c, boolean is_push){//push or pop from stack in a synchronised way
    if(is_push){
      available_vertex_list.add(c);
    }
    else{
      if(available_vertex_list.size() > 0){
        Integer[] recv = available_vertex_list.get(ra.nextInt(available_vertex_list.size()));
        System.out.println("list size");
        System.out.println(available_vertex_list.size());
        available_vertex_list.remove(recv); 
        c[0]=recv[0];
        c[1]=recv[1];
        System.out.println("c was set to: ");
        System.out.println(c[0]);
        System.out.println(c[1]);
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
      //System.out.println("ec.current_index was set to: ");
      //System.out.println(ec.current_index[0]);
      //System.out.println(ec.current_index[1]);
      return true;
    }
    return false;
  }
  
  static void do_edge_creation(int thread_count){
    Thread[] th_arr=new Thread[thread_count];
    
    for(int i=0;i<thread_count;i++){
      th_arr[i]=new Thread(new edge_create());
      th_arr[i].start();
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
    tile.board[0][0].piece_at_position= new piece(true);
    tile.board[tile.board_size_x-1][tile.board_size_y-1].piece_at_position= new piece(true);
    //System.out.println(tile.board[tile.board_size_x-1][tile.board_size_y-1].piece_at_position.is_vertex);
    //System.out.println("the following is a vertex");
    //System.out.println(tile.board[tile.board_size_x-1][tile.board_size_y-1].tile_position_x);
    //System.out.println(tile.board[tile.board_size_x-1][tile.board_size_y-1].tile_position_y);
    tile.generate_edge(tile.board[0][0],tile.board_size_y-2,1,0);
    tile.generate_edge(tile.board[0][0],tile.board_size_x-2,0,1);
    tile.generate_edge(tile.board[tile.board_size_x-1][tile.board_size_y-1],tile.board_size_y-3,0,-1);
    tile.generate_edge(tile.board[tile.board_size_x-1][tile.board_size_y-1],tile.board_size_x-3,-1,0);
    Integer[] p1={0,0};
    Integer[] p2={tile.board_size_x-1,tile.board_size_y-1};
    edge_create.modify_stack(p1,true);
    edge_create.modify_stack(p2,true);
    
    printy();
    
    
    
    
    while(!completed.get()){ 
      if(obtain_vertex(this)){
        //System.out.println("obtained_vertex");
        //System.out.println(current_index[0]);
        //System.out.println(current_index[1]);
        Random rand = new Random();
        direction = directions[rand.nextInt(directions.length)]; 
        int temp_radius=rand.nextInt(edge_radius);
        if(!tile.generate_edge(tile.board[current_index[0]][current_index[1]],temp_radius,direction[0],direction[1])){
          edge_create.modify_stack(current_index,true);
          //System.out.println("added back");
        }
        else{
          update_completion_count();
          edge_create.modify_stack(current_index,true);
          System.out.println("updated completion count");
          printy();
        }
        
      }
      else{
        //System.out.println("could not obtain vertex");
      }
      try{
        Thread.sleep(5);//wait for vertex availability
      }
      catch(InterruptedException ex){
        
      }
      
    }
  }
  
  
}