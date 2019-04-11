 //individual piece, could be an obstacle or a wall
 
 public class piece{
  static int obstacle_width=5;
  static int obstacle_height=5;
  
  volatile String type="piece";
  volatile int connections=2;
  volatile String print_symbol="+";
  volatile boolean is_vertex=false;
  
  void print(){
   System.out.print(this.print_symbol); 
  }
  void add_connections(int count){
    this.connections+=1;
  }
  void change_symbol(String x){
    this.print_symbol=x;
  }
  piece(boolean vert){
    if(vert){
      //System.out.println("changing print symbol");
      this.is_vertex=true;
      this.print_symbol="X";
    }
  }
  piece(boolean vert, boolean isobject){
    if(isobject){
      this.print_symbol="o";
      this.type="obstacle";
    }
  }
}