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
  
  static int max_x=board_size_x-piece.obstacle_width-2;//make sure obstacle is not placed at the edge
  static int min_x=3;
  static int max_y=board_size_y-piece.obstacle_height-2;
  static int min_y=3;
  
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

    
   
    int from_x=start_tile.tile_position_x;//generate from and two points given directions
    int from_y=start_tile.tile_position_y;
    int to_x= from_x+radius*dir_x;
    int to_y= from_y+radius*dir_y;

    int[] reserve_dir_x={-dir_y,dir_y};//the perpendicular direction to the path's direction
    int[] reserve_dir_y={dir_x,-dir_x};
      
    
    
    
    if(to_x+dir_x>=(board_size_x)||to_y+dir_y>=(board_size_y)||to_x+dir_x<0||to_y+dir_y<0){

      return false;      //if out of bounds return failure
    }
    
    int num_changed=0;
    int curr_x=from_x;
    int curr_y=from_y;
    boolean fail=false;
    
    
    int [] empty= {};
    while(!(curr_x==to_x+dir_x&&curr_y==to_y+dir_y)){//while all points between origin vertex and final are not reserved
      if((
            (curr_x==from_x&&curr_y==from_y)
            ||(curr_x==from_x+dir_x&&curr_y==from_y+dir_y)
            ||(curr_x==to_x&&curr_y==to_y)
            ||(curr_x==to_x-dir_x&&curr_y==to_y-dir_y)
            ||(curr_x==to_x+dir_x&&curr_y==to_y+dir_y)
          ) ){
            if((!board[curr_x][curr_y].reserve_tile(empty,empty))){//if it's not the vertex or a point around the vertex, reserve the points around the point on the edge
            //this guarantees that two polys aren't side by side and touch parallely
              fail=true;
              break;
            }
      }
      else if(!board[curr_x][curr_y].reserve_tile(reserve_dir_x,reserve_dir_y)){
        //fail if tile is already reserved or if that tile has a vertex that will exceed edge count if this edge is created
        fail=true;
        if(dir_x!=0&&dir_y!=0){
        }
        
        break;
      }
      curr_x+=dir_x;
      curr_y+=dir_y;
    }
    
    //now check if there is an object at the next tile, if there is, the player won't ve able to walk between the two walls and our edge generation fails
    if(!board[curr_x][curr_y].reserve_tile(empty,empty)){
      fail=true;
    }
    else{
      //we will make permanently block this 1 unit space so the player can actually walk through
      board[curr_x][curr_y].piece_at_position= new piece(false);
      board[curr_x][curr_y].piece_at_position.change_symbol(" ");//make this whitespace so player can walk through
      board[curr_x][curr_y].unreserve_tile(reserve_dir_x,reserve_dir_y);

      curr_x-=dir_x;
      curr_y-=dir_y;
    }
    
    if(fail){//we found an obstacle in the middle or exceeded edge count on a vertex so we unreserve tiles
      if(curr_x==from_x&&curr_y==from_y){
        return false;
      }
      for(curr_x-=dir_x,curr_y-=dir_y;!(curr_x==from_x-dir_x&&curr_y==from_y-dir_y);curr_x-=dir_x,curr_y-=dir_y){
        if(
          (
            (curr_x==from_x&&curr_y==from_y)
            ||(curr_x==from_x+dir_x&&curr_y==from_y+dir_y)
            ||(curr_x==to_x&&curr_y==to_y)
            ||(curr_x==to_x-dir_x&&curr_y==to_y-dir_y)
            ||(curr_x==to_x+dir_x&&curr_y==to_y+dir_y) 
          )        
        ){
          board[curr_x][curr_y].unreserve_tile(empty,empty);//unreserve tile but not its neighbors (for vertex or point next to vertex)
        }
        else{
          board[curr_x][curr_y].unreserve_tile(reserve_dir_x,reserve_dir_y);//unreserve tile as well as its parallel neighbors
        }
        
        //unreserve tile
      }
      return false;
    }
    else{//we proceed by setting all reserved tiles as piece
      for(;!(curr_x==from_x-dir_x&&curr_y==from_y-dir_y);curr_x-=dir_x,curr_y-=dir_y){

        if(board[curr_x][curr_y].piece_at_position==null){
          //space is empty so create a new piece or vertex
          if((curr_x==to_x&&curr_y==to_y) || (from_x==curr_x&&from_y==curr_y)){
            //create a vertex if it is at either end
            board[curr_x][curr_y].piece_at_position= new piece(true);
            Integer []pair ={curr_x,curr_y};
            edge_create.modify_stack(pair,true);
          }
          else{
            //simply create a piece
            board[curr_x][curr_y].piece_at_position= new piece(false);
          }
          
        }
        else{//it is already a vertex 
        
          board[curr_x][curr_y].piece_at_position.add_connections(1);
          if(board[curr_x][curr_y].piece_at_position.connections<edge_create.max_edges_per_vertex){
            Integer [] stackvals={new Integer(curr_x),new Integer(curr_y)}; 
            edge_create.modify_stack(stackvals,true);//add new vertex to stack
          }
        }
        board[curr_x][curr_y].unreserve_tile(reserve_dir_x,reserve_dir_y);//unreserve parallel tiles
      }
    }
    
    return true;
  }
  
 
  volatile AtomicBoolean isOccupied=new AtomicBoolean(false);//property specific to tile specifying whether its occupied
  int tile_position_x;
  int tile_position_y;
  piece piece_at_position;
  
  tile(int x, int y){//initial a new tile
    this.tile_position_x=x;
    this.tile_position_y=y;
  }
  
  void print(){//prints tile
    if(this.piece_at_position!=null){
      piece_at_position.print();
    }
    else{
      System.out.print(" ");//print blank space if no tile exists
    }
    
  }
  
  
  boolean reserve_tile(int[] reserve_dir_x, int[] reserve_dir_y){//reserves a tile and its neighbors if reserve_dir_x,y are not empty
    
    if(!((this.piece_at_position!=null&&this.piece_at_position.type=="piece"&&this.piece_at_position.is_vertex)||this.piece_at_position==null||this.piece_at_position.type==null)){//either another piece or an obstacle goes through this tile

      return false;
    }
    if(this.isOccupied.compareAndSet(false,true)){//use cas to try reserve
      boolean ret=true;
      for(int i=0; i<reserve_dir_x.length;i++){
        try{
          int[] empty={};
          ret=board[this.tile_position_x+reserve_dir_x[i]][this.tile_position_y+reserve_dir_y[i]].reserve_tile(empty,empty);//reserve points at the sides
          if(!ret){
            //reserve failed for adjacent pieces
            for(;i>-1;i--){
              try{
                board[this.tile_position_x+reserve_dir_x[i]][this.tile_position_y+reserve_dir_y[i]].unreserve_tile(empty,empty);
              }
              catch(java.lang.ArrayIndexOutOfBoundsException ex){//if side points are out of bounds just ignore
                
              }
            }
            this.unreserve_tile(empty,empty);
            return false;
          }
        }
        catch(java.lang.ArrayIndexOutOfBoundsException ex){
          //do nothing
        }
      }
      
      return true;
    }
    return false;
  }
  
  void unreserve_tile(int[] reserve_dir_x, int[] reserve_dir_y){//make blank tile available to other threads
    int[] empty={};
    this.isOccupied.set(false);
    for(int i=0;i<reserve_dir_x.length;i++){
      try{
        board[this.tile_position_x+reserve_dir_x[i]][this.tile_position_y+reserve_dir_y[i]].unreserve_tile(empty,empty);//unreserve tiles to the sides and the tile itself
      }
      catch(java.lang.ArrayIndexOutOfBoundsException ex){
        //ignore if side tiles are out of bounds
      }
    }
  }
  
  
 }