public class q1{ 
  
  //we assume a board of size 100x100 and a point is 1 unit
  //so an object becomes 5x5
  public static void main(String args[]){
    int thread_count=Integer.parseInt(args[0]);
    edge_create.max_vertices=Integer.parseInt(args[1]);
    edge_create.max_edges_per_vertex=Integer.parseInt(args[2]);
    float ft= Float.parseFloat(args[3])*100;
    edge_create.edge_radius=(int) ft;
    obstacle_create.create_all(thread_count);
    tile.print_board();
    System.out.println("*************");
    edge_create.do_edge_creation(thread_count);
    
    System.out.println("*************");
    tile.print_board();
  }
  
  
}