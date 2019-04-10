 //individual piece, could be an obstacle or a wall
 
 public abstract class piece{
  String type;
  boolean is_vertex=false;
  abstract void print();
  abstract void add_connections(int count);
  abstract void change_symbol(String x);
} 