public class path extends piece{
  volatile int connections = 2;
  volatile boolean is_vertex=false;
  
  String type;
  String print_symbol;
  
  path(boolean is_vertex){
    this.type="path";
    this.print_symbol="+";
    if(is_vertex){
      this.is_vertex=true;
      this.connections=1;
      this.print_symbol="X";
    }
  }
  
  void add_connections(int count){
    this.connections+=count;
    this.is_vertex=true;
    this.print_symbol="X";
  }
  
  void change_symbol(String x){
    this.print_symbol=x;
  }  
  
  void print(){
    System.out.print(this.print_symbol);
  }
  
}