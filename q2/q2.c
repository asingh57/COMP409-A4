#include <omp.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h> 
//ABHIJIT SINGH
//260675220
//ASSIGNMENT 4 COMP 409 part 2


//to compile simply do: gcc -g -o q2 -fopenmp q2.c


struct transition{//stores ascii that lead to next state
  int start_code_ascii;
  int end_code_ascii;  
  struct state *next_state;
};

struct state{//state and it's respective possible transitions
  struct transition *transitions[2];
  int length;
  int is_accept_state;
  char name[20];
};

struct char_state_association{//associates a range of character positions with the DFA they satisfy
  int position_start;//char at position start satisfies state start 
  int position_end;// char at position end satisfies state end
  struct state *state_start;
  struct state *state_end;
};

struct associations_chain{//DFA chain that are satisfied by string
  int association_count;
  struct char_state_association association_array[20];  
};

struct associations_chains_per_index{//list of DFA chains associated with a portion of a string given to a thread
  int index;
  int chain_count;
  struct associations_chain association_chain_array[5];  
};



struct transition trans_top_left={ (int)'0', (int)'0', NULL };
struct transition trans_bottom_left={ (int)'1', (int)'9', NULL };
struct transition trans_bottom_left_self={ (int)'0', (int)'9', NULL };
struct transition trans_decimal={ (int)'.', (int)'.', NULL };
struct transition trans_accept={ (int)'0', (int)'9', NULL };


struct state start_state;
struct state top_mid_state;
struct state bottom_mid_state;
struct state decimal_state;
struct state accept_state;

int check_transition_possible(struct state *current_state, int char_ascii, struct state **result){// check if transitions are possible for a given char from a given state and return the resulting states possible

  for(int i=0;i<current_state->length;i++){
    if(
      current_state->transitions[i]->start_code_ascii<=char_ascii
    &&
      current_state->transitions[i]->end_code_ascii>=char_ascii
    )
    {
      *result=(current_state->transitions[i]->next_state);
      return 1;
    }
  }
  return 0;  
}


int get_state_spans(struct state *start_from_state, char *string, int start_pos, int end_pos, struct char_state_association *associations){
  //gets list of dfas satisfied by string between given positions
  struct state *curr_state;
  curr_state= start_from_state;
  int currently_joining=0;
  struct char_state_association current_csa;
  int associations_length=0;
  struct state *double_ptr[1];
  
  for(int t=start_pos; t<end_pos;t++){
    struct state *recv_state; 
    if(check_transition_possible(curr_state,(int)string[t],double_ptr)==1){
      recv_state=*double_ptr;
      if(currently_joining==0){//this is the start of a new DFA recognised string
        currently_joining=1;
        struct char_state_association csa={t,t+1,curr_state,recv_state};
        current_csa=csa;
      }
      current_csa.position_end=t;
      current_csa.state_end=recv_state;
      
      if(t==end_pos-1){//if last char, publish it in return associations list
        associations[associations_length]=current_csa;
        associations_length+=1;
      }
      curr_state=recv_state;
      
    }
    else{
      
      if(curr_state==&accept_state){//check if the last state was the accept state, then publish it in the return associations list
        associations[associations_length]=current_csa;
        associations_length+=1;
      }
      currently_joining=0;//reset DFA for next char
      curr_state=&start_state;//reset to DFA's overall start state
    }
  }
  return associations_length;
  
}

struct state *state_list[5];




int main(int argc,char *argv[]) {
  //link all states with their transitions
  start_state.transitions[0]=&trans_top_left;
  start_state.transitions[1]=&trans_bottom_left;
  strcpy(start_state.name,"start_state");
  start_state.length=2;
  top_mid_state.transitions[0]=&trans_decimal;
  strcpy(top_mid_state.name,"top_mid_state");
  top_mid_state.length=1;
  bottom_mid_state.transitions[0]=&trans_decimal;
  bottom_mid_state.transitions[1]=&trans_bottom_left_self;
  strcpy(bottom_mid_state.name,"bottom_mid_state");
  bottom_mid_state.length=2;
  decimal_state.transitions[0]=&trans_accept;
  strcpy(decimal_state.name,"decimal_state");
  decimal_state.length=1;
  accept_state.transitions[0]=&trans_accept;
  strcpy(accept_state.name,"accept_state");
  accept_state.length=1;
  accept_state.is_accept_state=1;
  
  //link all transitions to states they lead to
  trans_top_left.next_state=&top_mid_state;
  trans_bottom_left.next_state=&bottom_mid_state;
  trans_bottom_left_self.next_state=&bottom_mid_state;
  trans_decimal.next_state=&decimal_state;
  trans_accept.next_state=&accept_state;
  
  //make a global list of all states
  state_list[0]=&start_state;
  state_list[1]=&top_mid_state;
  state_list[2]=&bottom_mid_state;
  state_list[3]=&decimal_state;
  state_list[4]=&accept_state;
  
  int max_chars=200;
  char * input_str=malloc(max_chars+1);
  char list[12]={'0','1','2','3','4','5','6','7','8','9','a','.'};
  for(int i=0;i<max_chars;i++){
    input_str[i]=list[rand()%12];
  }
  printf("%s\n",input_str);//print random generated string

  int string_size=strlen(input_str);
  
  int optimistic_threads=0;
  if(argc>1){
    optimistic_threads=atoi(argv[1]);
  }
  int total_threads=optimistic_threads+1;
  omp_set_num_threads(total_threads);//get count for optimistic_threads and add
  
  
  int spaces_needed=0;
  int is_working[total_threads];
  for (int i=0;i<total_threads;i++) {
      int start_char_index=(i*string_size)/total_threads;
      int end_char_index=((i+1)*string_size)/total_threads;
      if(i==total_threads-1){
        int delta=string_size-end_char_index;
        end_char_index+=delta;
      }
      if(start_char_index<end_char_index){        
        is_working[i]=spaces_needed;
        spaces_needed+=1;
      }
      else{
        is_working[i]=-1;
      }
  }
  
  struct associations_chains_per_index mass_associations_chain_index[spaces_needed];
  
  clock_t start; 
  start = clock();
  #pragma omp parallel for shared(mass_associations_chain_index)
    for (int i=0;i<total_threads;i++) {//split work to threads
      int start_char_index=(i*string_size)/total_threads;
      int end_char_index=((i+1)*string_size)/total_threads;
      if(i==total_threads-1){
        int delta=string_size-end_char_index;
        end_char_index+=delta;
      }
      
      for(int nav=start_char_index;nav<end_char_index;nav++){//write whitespaces in place of non recognised characters
        if(
        input_str[nav]!='\0'
        &&
        input_str[nav]!='.'
        &&
        ((int)input_str[nav]<(int)'0'
        ||
        (int)input_str[nav]>(int)'9')
        ){
          
          input_str[nav]=' ';
        }
        
      }
      
      
      if(i==0){
        
        int ct=(int)(((end_char_index-start_char_index+1)/3))+2;        
        
        struct char_state_association associations[ct];
        int associations_received=get_state_spans(&start_state, input_str, start_char_index, end_char_index, associations);
        //check if a valid dfa chain is obtainable
        
        if(associations_received>0){//add it to the global list of dfa chains
          struct associations_chain ac;
          ac.association_count=associations_received;
          for(int kk = 0; kk<associations_received;kk++){
              ac.association_array[kk]=associations[kk];
            }
          mass_associations_chain_index[is_working[i]].chain_count=1;//update count of chains associated
          mass_associations_chain_index[is_working[i]].association_chain_array[0]=ac; 
        }
        else{
          mass_associations_chain_index[is_working[i]].chain_count=0;
        }
        mass_associations_chain_index[is_working[i]].index=start_char_index;
        
        
      }
      else if(is_working[i]>=0){//it has a valid string allocated to it
        int ct=(int)(((end_char_index-start_char_index+1)/3))+2;
        
        int num_associations_chains_received=0;
        for(int l=0;l<5;l++){//check each individual state and associate a 2d array of dfa chains with the string
          struct associations_chain ac;
          struct char_state_association associations[ct];
          int associations_received=get_state_spans(state_list[l], input_str, start_char_index, end_char_index, associations);
          if(associations_received>0){
            
            
            ac.association_count=associations_received;
            for(int kk = 0; kk<associations_received;kk++){
              ac.association_array[kk]=associations[kk];
              
            }
            mass_associations_chain_index[is_working[i]].association_chain_array[num_associations_chains_received]=ac;  
            num_associations_chains_received+=1;              
          }
        }
        
        mass_associations_chain_index[is_working[i]].chain_count=num_associations_chains_received;
        mass_associations_chain_index[is_working[i]].index=start_char_index;
      }
      
    }

  
  int prev_chosen_association=-1;
  char * output_str=malloc(max_chars+1);
  for(int x=0;x<string_size+1;x++){
    output_str[x]=' ';
  }
   
  
  struct state *last_state;
  int prev_chain_continues_till_end=-1;//index for the end of the last incomplete dfa
  //if one of the matches in the previous chain links into the current one
  
  for(int i=0; i<total_threads;i++){
    
    if(is_working[i]<0){
      continue;
    }
    
    int chain_count= mass_associations_chain_index[is_working[i]].chain_count;
    if(chain_count>0){
      struct state *search_state[2];
      int next_states_possible=0;
      if(prev_chain_continues_till_end!=-1){
        //we are looking for one of the sequels to the last state
        for(int x=0;x<last_state->length;x++){
          search_state[x]=last_state->transitions[x]->next_state;
          next_states_possible+=1;//update count of next states possible from current position
        }
      }
      int continuing_chain_found_index=-1;
      int new_start_chain_found_index=-1;
      
      for(int chain_number=0;chain_number<chain_count;chain_number++){
          
          for(int r=0;r<next_states_possible;r++){//if we have next states
            if(mass_associations_chain_index[is_working[i]].association_chain_array[chain_number].association_array[0].position_start==mass_associations_chain_index[is_working[i]].index
                &&
              mass_associations_chain_index[is_working[i]].association_chain_array[chain_number].association_array[0].state_start==search_state[r]
            ){
              continuing_chain_found_index=chain_number;//save the chain number that is compatible with the string to the left of this one
            }
          }
          
          if(mass_associations_chain_index[is_working[i]].association_chain_array[chain_number].association_array[0].state_start==&start_state){
            new_start_chain_found_index=chain_number;
          }
        //}
      }
      if(continuing_chain_found_index==-1&&prev_chain_continues_till_end!=-1&&last_state!=&accept_state){
        int counter=prev_chain_continues_till_end;
        while(counter>=0&&output_str[counter]!=' '){
          output_str[counter]=' ';//wipe backwards and clear incomplete chain made by another thread
          counter=counter-1;
        }
      }
      if(continuing_chain_found_index==-1&&new_start_chain_found_index!=-1){
        continuing_chain_found_index=new_start_chain_found_index;
      }
      
      if(continuing_chain_found_index!=-1){
        //write to output using the chain selected
        for(int f=0;f<mass_associations_chain_index[is_working[i]].association_chain_array[continuing_chain_found_index].association_count;f++){
          for(int c=mass_associations_chain_index[is_working[i]].association_chain_array[continuing_chain_found_index].association_array[f].position_start;
          c<=mass_associations_chain_index[is_working[i]].association_chain_array[continuing_chain_found_index].association_array[f].position_end;
          c++){
          
            
            output_str[c]=input_str[c];
             
            
            int start_char_index=(i*string_size)/total_threads;
            int end_char_index=((i+1)*string_size)/total_threads;
            if(c==end_char_index-1){
              //now set the end for the next iteration using prev_chain_continues_till_end if it goes till the last character. also set last_state
              prev_chain_continues_till_end=c;
              last_state=mass_associations_chain_index[is_working[i]].association_chain_array[continuing_chain_found_index].association_array[f].state_end;
            }
          }
          
        }
        
        
        
      }
      
      
      
    }
    else{//no valid DFA match was generated for the given string so reset
      if(prev_chain_continues_till_end!=-1 && last_state!=&accept_state){
        //we must wipe out the last incomplete dfa
        int counter=prev_chain_continues_till_end;
        while(counter>=0&&output_str[counter]!=' '){
          output_str[counter]=' ';//wipe backwards
          counter=counter-1;
          
        }
      }
      struct state temp;
      last_state=&temp;//associate an empty string with the last state
      prev_chain_continues_till_end=-1;//make it known to the next iteration that previous chain does not need to be checked
    }
  
  }
  
  
  //printf("TIME TAKEN %f\n",((double)(clock() - start))/CLOCKS_PER_SEC);
  printf("%s\n",output_str);

}










