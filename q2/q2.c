#include <omp.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct transition{
  int start_code_ascii;
  int end_code_ascii;  
  struct state *next_state;
};

struct state{
  struct transition *transitions[2];
  int length;
  int is_accept_state;
};

struct char_state_association{
  int position_start;
  int position_end;
  struct state *state_start;
  struct state *state_end;
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

int check_transition_possible(struct state *current_state, int char_ascii, struct state *result){

  for(int i=0;i<current_state->length;i++){
    if(
      current_state->transitions[i]->start_code_ascii<=char_ascii
    &&
      current_state->transitions[i]->end_code_ascii>=char_ascii
    )
    {
      *result=*(current_state->transitions[i]->next_state);
      return 1;
    }
  }
  return 0;  
}


int get_state_spans(struct state *start_from_state, char *string, int start_pos, int end_pos, struct char_state_association **associations){
  struct state *curr_state;
  curr_state= start_from_state;
  int currently_joining=0;
  struct char_state_association *current_csa;
  int associations_length=0;
  for(int t=start_pos; t<end_pos;t++){
    struct state *recv_state; 
    recv_state=start_from_state;
    if(check_transition_possible(curr_state,(int)string[t],recv_state)==1){
      if(currently_joining==0){//this is the start of a new DFA recognised string
        currently_joining=1;
        struct char_state_association csa={t,t,curr_state,recv_state};
        current_csa=&csa;
      }
      current_csa->position_end=t;
      current_csa->state_end=recv_state;
      
      if(t==end_pos-1){//if last char, publish it in return associations list
        associations[associations_length]=current_csa;
        associations_length+=1;
      }
      curr_state=recv_state;
      
      return 0;
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

int get_eligible_states(int char_ascii, struct state *results){
  //gets list of all possible states for a given character to be in
  int count=0;
  for(int i=0;i<5;i++){
    //results[count]=&start_state;
    
    if(check_transition_possible(state_list[i], char_ascii, &results[count])==1){
      count+=1;
    }
  }
  return count;  
}


int main(int argc,char *argv[]) {
  //link all states with their transitions
  start_state.transitions[0]=&trans_top_left;
  start_state.transitions[1]=&trans_bottom_left;
  start_state.length=2;
  top_mid_state.transitions[0]=&trans_decimal;
  top_mid_state.length=1;
  bottom_mid_state.transitions[0]=&trans_decimal;
  bottom_mid_state.transitions[1]=&trans_bottom_left_self;
  bottom_mid_state.length=2;
  decimal_state.transitions[0]=&trans_accept;
  decimal_state.length=1;
  accept_state.transitions[0]=&trans_accept;
  accept_state.length=1;
  accept_state.is_accept_state=1;
  
  //link all transitions to states they lead to
  trans_top_left.next_state=&top_mid_state;
  trans_bottom_left.next_state=&bottom_mid_state;
  trans_bottom_left_self.next_state=&bottom_mid_state;
  trans_decimal.next_state=&decimal_state;
  trans_accept.next_state=&accept_state;
  
  state_list[0]=&start_state;
  state_list[1]=&top_mid_state;
  state_list[2]=&bottom_mid_state;
  state_list[3]=&decimal_state;
  state_list[4]=&accept_state;
  
  int max_chars=1000;
  char * input_str=malloc(max_chars+1);
  strcpy(input_str, "This 1 1.1.1.1 is _1.02 an input.str 2.11s");
  printf("%s\n",input_str);

  int string_size=strlen(input_str);
  
  int optimistic_threads=0;
  if(argc>1){
    optimistic_threads=atoi(argv[1]);
  }
  int total_threads=optimistic_threads+1;
  omp_set_num_threads(total_threads);
  
  struct char_state_association ***associations_list_by_thread[total_threads];
  int association_chains_per_thread[total_threads];
  int *associations_in_chain[total_threads];
  
  #pragma omp parallel for
    for (int i=0;i<total_threads;i++) {
      int start_char_index=(i*string_size)/total_threads;
      int end_char_index=((i+1)*string_size)/total_threads;
      if(i==total_threads-1){
        int delta=string_size-end_char_index;
        end_char_index+=delta;
      }
      
      for(int nav=start_char_index;nav<end_char_index;nav++){
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
        //struct state results[5];
        //get_eligible_states((int)input_str[nav], results);
        
      }
      
      
      if(i==0){        
        int ct=(int)(((end_char_index-start_char_index+1)/3))+2;
        struct char_state_association *associations[ct];
        int associations_received=get_state_spans(&start_state, input_str, start_char_index, end_char_index, associations);
        struct char_state_association **associations_list[1];
        associations_list[0]=associations;
        associations_list_by_thread[i]=associations_list;
        association_chains_per_thread[0]=0;
        if(associations_received>0){
          association_chains_per_thread[0]=1;
        }
        
      }
      else{
        int ct=(int)(((end_char_index-start_char_index+1)/3))+2;
        struct char_state_association **associations_list[5];
        
        int count=0;
        for(int l=0;l<5;l++){
          struct char_state_association *associations[ct];
          int associations_received=get_state_spans(state_list[l], input_str, start_char_index, end_char_index, associations);
          if(associations_received>0){
            associations_list[count++]=associations;
          }
        }
        associations_list_by_thread[i]=associations_list; 
        
        association_chains_per_thread[i]=0;
        if(count>0){
          association_chains_per_thread[i]=1;
        }        
      }
    }
  
  int prev_chosen_association=-1;
  char * output_str=malloc(max_chars+1);
  for(int i=0;i<total_threads;i++){
    int start_char_index=(i*string_size)/total_threads;
    int end_char_index=((i+1)*string_size)/total_threads;
    if(i==total_threads-1){
      int delta=string_size-end_char_index;
      end_char_index+=delta;
    }
    if(prev_chosen_association<0&&association_chains_per_thread[i]>0){
      prev_chosen_association=0;//we'll simply pick the one with the start state as the initial state
      //now we do the appropriate deletions in the string
      for(int g=0;g<[i][0];g++){
        output_str[]=input_str[];
      }
    }
    else if(association_chains_per_thread[i]>0&&prev_chosen_association>=0){
      //we see that if any one of our present dfas matches up with the ending of an old one
      //if it does, we pick that
      //else we pick the one with start state if it exists
      //else we set previous association to -1
    }
    else if(association_chains_per_thread[i]==0&&prev_chosen_association>=0){
      //we must delete the incomplete dfa matches from last our match (if eligible)
      prev_chosen_association=-1;
    }
    else{
      //neither last piece nor this one has any matches so replace everything with whitespace
      prev_chosen_association=-1;
    }
    
  }
    
  printf("%s\n",output_str);
    
  
  
}










