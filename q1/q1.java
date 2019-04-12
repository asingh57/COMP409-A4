public class q1{ 

  //Abhijit Singh 260675220
  //COMP409 Assignment 1 Q1 
  //compile with javac q1.java
  //we assume a board of size 100x100 and a point is 1 unit
  //This choice was made because of the known incapabilities of computers to hold floats properly
  //so an object becomes 5x5
  //feel free to adjust the board size as you please in tile.java
  //The board is generated in such a way that there always exists a path from the bottom left corner to the top right corner
  public static void main(String args[]){
    int thread_count=Integer.parseInt(args[0]);//set thread count
    edge_create.max_vertices=Integer.parseInt(args[1]);//set total vertices
    edge_create.max_edges_per_vertex=Integer.parseInt(args[2]);//set max edges per vertex
    float ft= Float.parseFloat(args[3])*100;//parse float and fit on the board
    edge_create.edge_radius=(int) ft;//convert to int
    obstacle_create.create_all(thread_count);//generate random obstacles on board
    long start = System.currentTimeMillis(); //get start time of program
    edge_create.do_edge_creation(thread_count);//run the program 
    tile.print_board();//finally, print the board
    long end = System.currentTimeMillis();
    System.out.println("Total parallel time:");
    System.out.println(end-start);
  }
  
  
}