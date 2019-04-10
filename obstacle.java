public class obstacle extends piece{// this a unit value wall for an obstacle and not the whole obstacle itself
  
  String type;
  String print_symbol;
  static int width=5;
  static int height=5;
  static int count=20;
  obstacle(){
    this.print_symbol="o";
    this.type="obstacle";
  } 
  
  public void print(){
    System.out.print(this.print_symbol);
  }
  
  public void add_connections(int count){}
  public void change_symbol(String x){}
}