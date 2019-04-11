 //Individual spaces on the board
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Random;


 public class tile{//1x1 in size
  static int max_edges_per_vertex=3;
  static int board_size_x=100;//we choose to use 100x100 board instead of 1x1 to avoid floating point errors
  static int board_size_y=100;
  static volatile tile[][] board= new tile[board_size_x][board_size_y];
  static{//initialise as empty board
    for(int x=0;x<board.length;x++){
      for(int y=0;y<board[x].length;y++){
        board[x][y]= new tile(x,y);
      }
    }
  }
  
  static void print_board(){//print board's current config
    for(int x=0;x<board.length;x++){
      for(int y=0;y<board[x].length;y++){
        board[x][y].print();
      }
      System.out.println();
    }
    System.out.println();
  }
  
  static int max_x=board_size_x-piece.obstacle_width-1;//make sure obstacle is not placed at the edge
  static int min_x=2;
  static int max_y=board_size_y-piece.obstacle_height-1;
  static int min_y=2;
  
  static void generate_obstacle(){
    Random rand = new Random();
    //generate obstacle at random position such that it is not out of bounds    
    int pos_x=rand.nextInt((max_x - min_x) + 1) + min_x;
    int pos_y=rand.nextInt((max_y - min_y) + 1) + min_y;
    
    for(int i=pos_x;i<pos_x+piece.obstacle_width;i++){
      for(int j=pos_y;j<pos_y+piece.obstacle_height;j++){
        board[i][j].piece_at_position= new piece(false,true);
      }
    }
    
  } 
  
  
  static boolean generate_edge(tile start_tile,int radius, int dir_x, int dir_y){
    //generates an edge
    //takes as input the start tile, the length of edge to be made and the direction in which it has to be made
   //ignore starting tile as it is the starting vertex

    
   
    int from_x=start_tile.tile_position_x;
    int from_y=start_tile.tile_position_y;
    int to_x= from_x+radius*dir_x;
    int to_y= from_y+radius*dir_y;

      
    
    
    
    if(to_x+dir_x>=(board_size_x)||to_y+dir_y>=(board_size_y)||to_x+dir_x<0||to_y+dir_y<0){
      //System.out.println("exceeded bounds");
      //System.out.println(from_x);
      //System.out.println(from_y);
      //System.out.println(to_x);
      //System.out.println(to_y);
      return false;      
    }
    System.out.println("edge details:from:");
    System.out.print(from_x);
    System.out.print(",");
    System.out.print(from_y);
    System.out.print("to:");
    System.out.print(to_x);
    System.out.print(",");
    System.out.println(to_y);
    
    int num_changed=0;
    int curr_x=from_x;
    int curr_y=from_y;
    boolean fail=false;
    
    
    
    while(!(curr_x==to_x+dir_x&&curr_y==to_y+dir_y)){
      if(!board[curr_x][curr_y].reserve_tile()){
        //fail if tile is already reserved or if that tile has a vertex that will exceed edge count if this edge is created
        fail=true;
        if(dir_x!=0&&dir_y!=0){
          //System.out.println("found reserved tile at ");
          //System.out.println(curr_x);
          //System.out.println(curr_y);
        }
        
        break;
      }
      curr_x+=dir_x;
      curr_y+=dir_y;
    } 
    
    //now check if there is an object at the next tile, if there is, the player won't ve able to walk between the two walls and our edge generation fails
    if(!board[curr_x][curr_y].reserve_tile()){
      fail=true;
      //System.out.println("no gap tile");
      //System.out.println(from_x);
      //System.out.println(from_y);
      //System.out.println(curr_x);
      //System.out.println(curr_y);
    }
    else{
      //we will make permanently block this 1 unit space so the player can actually walk through
      board[curr_x][curr_y].piece_at_position= new piece(false);
      board[curr_x][curr_y].piece_at_position.change_symbol(" ");//make this whitespace so player can walk through
      board[curr_x][curr_y].unreserve_tile();
      //System.out.println("One block reserved for movement");
      //System.out.println(curr_x);
      //System.out.println(curr_y);
      curr_x-=dir_x;
      curr_y-=dir_y;
    }
    
    if(fail){//we found an obstacle in the middle or exceeded edge count on a vertex so we unreserve tiles
      if(curr_x==from_x&&curr_y==from_y){
        //System.out.println("current is the start index so not unreserved");
        return false;
      }
      for(curr_x-=dir_x,curr_y-=dir_y;!(curr_x==from_x-dir_x&&curr_y==from_y-dir_y);curr_x-=dir_x,curr_y-=dir_y){
        board[curr_x][curr_y].unreserve_tile();
        //unreserve tile
      }
      //System.out.println("failed");
      return false;
    }
    else{//we proceed by setting all reserved tiles as piece
      for(;!(curr_x==from_x-dir_x&&curr_y==from_y-dir_y);curr_x-=dir_x,curr_y-=dir_y){
        //System.out.println(curr_x);
        //System.out.println(curr_y);
        if(board[curr_x][curr_y].piece_at_position==null){
          //space is empty so create a new piece or vertex
          if((curr_x==to_x&&curr_y==to_y) || (from_x==curr_x&&from_y==curr_y)){
            //create a vertex if it is at either end
            board[curr_x][curr_y].piece_at_position= new piece(true);
            //System.out.println("created new vertex ");
            Integer []pair ={curr_x,curr_y};
            edge_create.modify_stack(pair,true);
          }
          else{
            //simply create a piece
            board[curr_x][curr_y].piece_at_position= new piece(false);
            //System.out.println("created new piece ");
          }
          
          //System.out.println("new piece value");
          //System.out.println(board[curr_x][curr_y].piece_at_position.type);
        }
        else{//it is already a vertex 
        
        //System.out.println("already a vertex");
        //System.out.println(board[curr_x][curr_y].piece_at_position.type);
        //System.out.println(board[curr_x][curr_y].piece_at_position.is_vertex);
        //System.out.println(curr_x);
        //System.out.println(curr_y);
          board[curr_x][curr_y].piece_at_position.add_connections(1);
          if(board[curr_x][curr_y].piece_at_position.connections<edge_create.max_edges_per_vertex){
            Integer [] stackvals={new Integer(curr_x),new Integer(curr_y)}; 
            edge_create.modify_stack(stackvals,true);
          }
        }
        board[curr_x][curr_y].unreserve_tile();
      }
    }
    
    return true;
  }
  
 
  volatile AtomicBoolean isOccupied=new AtomicBoolean(false);
  int tile_position_x;
  int tile_position_y;
  piece piece_at_position;
  
  tile(int x, int y){
    this.tile_position_x=x;
    this.tile_position_y=y;
  }
  
  void print(){//prints tile
    if(this.piece_at_position!=null){
      piece_at_position.print();
    }
    else{
      System.out.print(" ");
    }
    
  }
  
  
  boolean reserve_tile(){
    
    if(!((this.piece_at_position!=null&&this.piece_at_position.type=="piece"&&this.piece_at_position.is_vertex)||this.piece_at_position==null||this.piece_at_position.type==null)){//either another piece or an obstacle goes through this tile
      //System.out.println("the following is not a vertex");
      //System.out.println(this.tile_position_x);
      //System.out.println(this.tile_position_y);
          //System.out.println("not a vertex and is a piece");  
          //System.out.println(this.piece_at_position.type);
          //System.out.println(this.tile_position_x);
          //System.out.println(this.tile_position_y);
      return false;
    }
    else if(this.piece_at_position!=null){
        //System.out.println("eligible for new entry");  
          //System.out.println(this.piece_at_position.type);
          //System.out.println(this.piece_at_position.print_symbol);
          //System.out.println(this.tile_position_x);
          //System.out.println(this.tile_position_y);
    }
    
    if(this.isOccupied.compareAndSet(false,true)){
      return true;
    }
    //System.out.println("set failed");          
          //System.out.println(this.tile_position_x);
          //System.out.println(this.tile_position_y);
    return false;
  }
  
  void unreserve_tile(){
    this.isOccupied.set(false);
  }
  
  
 }