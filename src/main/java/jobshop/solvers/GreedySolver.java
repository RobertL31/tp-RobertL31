package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.neighborhood.Nowicki;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

/** An empty shell to implement a greedy solver. */
public class GreedySolver implements Solver {

    private ResourceOrder sol;

    /** All possible priorities for the greedy solver. */
    public enum Priority {
        SPT, LPT, SRPT, LRPT, EST_SPT, EST_LPT, EST_SRPT, EST_LRPT
    }

    /** Priority that the solver should use. */
    final Priority priority;

    /** Creates a new greedy solver that will use the given priority. */
    public GreedySolver(Priority p) {
        this.priority = p;
    }

    @Override
    public Result solve(Instance instance, long deadline) {

        int[] freeTimeOfMachine = new int[instance.numMachines];
        int[] freeTimeOfJob = new int[instance.numJobs];

        ArrayList<Task> possibleTasks = new ArrayList<Task>();
        this.sol = new ResourceOrder(instance);

        // INITIALIZATION
        for(int njob=0; njob<instance.numJobs; ++njob){
            possibleTasks.add(new Task(njob,0));
        }

        // LOOP OVER POSSIBLE TASKS
        while(! possibleTasks.isEmpty()){

            //System.out.println(possibleTasks + "size is:" + possibleTasks.size());

            Task chosenTask = null;

            ArrayList<Task> startingSoonest = null;

            switch(this.priority) {
                case SPT:
                    chosenTask = makeSPTchoice(instance, possibleTasks);
                    break;
                case LRPT:
                    chosenTask = makeLRPTchoice(instance, possibleTasks);
                    break;
                case EST_SPT:
                    startingSoonest = makeEST_filter(instance, possibleTasks, freeTimeOfJob, freeTimeOfMachine);
                    chosenTask = makeSPTchoice(instance, startingSoonest);
                    break;
                case EST_LRPT:
                    startingSoonest = makeEST_filter(instance, possibleTasks, freeTimeOfJob, freeTimeOfMachine);
                    chosenTask = makeLRPTchoice(instance, startingSoonest);
                    break;
                default:
                    System.out.println("Exception, problem in priority for greedy");
            }

            possibleTasks.remove(chosenTask);

            // If there is still a task after this one
            if( chosenTask.task < instance.numTasks-1){
                possibleTasks.add(new Task(chosenTask.job, chosenTask.task+1));
            }
            this.sol.addTaskToMachine(instance.machine(chosenTask), chosenTask);

            //In every case, freeTime will be increased by the duration

            int machineEndTime = freeTimeOfMachine[instance.machine(chosenTask)];
            int jobEndTime = freeTimeOfJob[chosenTask.job];
            int date =  Math.max(machineEndTime, jobEndTime);
            freeTimeOfMachine[instance.machine(chosenTask)]  = date + instance.duration(chosenTask);
            freeTimeOfJob[chosenTask.job]  = date + instance.duration(chosenTask);

            /*
            if(freeTimeOfMachine[instance.machine(chosenTask)] > freeTimeOfJob[chosenTask.job]){
                freeTimeOfMachine[instance.machine(chosenTask)] += freeTimeOfMachine[instance.machine(chosenTask)] - freeTimeOfJob[chosenTask.job];
            } else{
                if(freeTimeOfMachine[instance.machine(chosenTask)] < freeTimeOfJob[chosenTask.job]){
                    freeTimeOfJob[chosenTask.job] += instance.duration(chosenTask) + freeTimeOfJob[chosenTask.job] - freeTimeOfMachine[instance.machine(chosenTask)];
                }
            }*/


        }

        return new Result(instance, this.sol.toSchedule(), Result.ExitCause.Blocked);
    }


    // choice functions take the task from the list and suppresses it from the list.
    Task makeSPTchoice(Instance instance, ArrayList<Task> possibleTasks){

        Task answer = null;

        int min_duration = instance.duration(possibleTasks.get(0));
        int min_index = 0;

        for(int i=1; i<possibleTasks.size(); ++i){
            int current_task_duration = instance.duration(possibleTasks.get(i));
            if( current_task_duration < min_duration){
                min_duration = current_task_duration;
                min_index = i;
            }
        }

        answer = possibleTasks.get(min_index);

        return answer;
    }


    Task makeLRPTchoice(Instance instance, ArrayList<Task> possibleTasks){

        Task answer = null;

        int max_left_duration = -1;

        for(Task t : possibleTasks){

            int left_duration = 0;

            for (int task = t.task; task < instance.numTasks; ++task) {
                left_duration += instance.duration(t.job, task);
            }

            if(left_duration > max_left_duration){
                max_left_duration = left_duration;
                answer = t;
            }

        }

        return answer;
    }


    ArrayList<Task> makeEST_filter(Instance instance, ArrayList<Task> possibleTasks, int[] freeTimeOfJob, int[] freeTimeOfMachine){


        ArrayList<Integer> startingTimes = new ArrayList<>();


        for(Task t : possibleTasks){
            //System.out.println(t);
            int machineEndTime = freeTimeOfMachine[instance.machine(t)];
            int jobEndTime = freeTimeOfJob[t.job];
            startingTimes.add( Math.max(machineEndTime, jobEndTime));
        }

        //System.out.println("---------------");

        ArrayList<Task> startingSoonest = new ArrayList<>();
        int minimum = Collections.min(startingTimes);

         for(int i=0; i<startingTimes.size(); ++i){
             if(startingTimes.get(i) == minimum)
                 startingSoonest.add(possibleTasks.get(i));
         }

        //System.out.println(startingSoonest);

        //System.out.println("-------------");

        return startingSoonest;
    }


    public ResourceOrder getResourceOrder(){
        return this.sol;
    }


}
