package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;

/** An empty shell to implement a greedy solver. */
public class GreedySolver implements Solver {

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

        ArrayList<Task> possibleTasks = new ArrayList<Task>();
        ResourceOrder sol = new ResourceOrder(instance);

        // INITIALIZATION
        for(int njob=0; njob<instance.numJobs; ++njob){
            possibleTasks.add(new Task(njob,0));
        }

        // LOOP OVER POSSIBLE TASKS
        while(! possibleTasks.isEmpty()){

            //System.out.println(possibleTasks + "size is:" + possibleTasks.size());

            Task chosenTask = null;

            switch(this.priority) {
                case SPT:
                    chosenTask = makeSPTchoice(instance, possibleTasks);
                    break;
                case LRPT:
                    chosenTask = makeLRPTchoice(instance, possibleTasks);
                    break;
                default:
                    System.out.println("Exception, problem in priority for greedy");
            }

            // If there is still a task after this one
            if( chosenTask.task < instance.numTasks-1){
                possibleTasks.add(new Task(chosenTask.job, chosenTask.task+1));
            }
            sol.addTaskToMachine(instance.machine(chosenTask), chosenTask);

        }

        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
    }


    // choice functions take the task from the list and suppresses it from the list.
    Task makeSPTchoice(Instance instance, ArrayList<Task> possibleTasks){

        int min_duration = instance.duration(possibleTasks.get(0));
        int min_index = 0;
        for(int i=1; i<possibleTasks.size(); ++i){
            int current_task_duration = instance.duration(possibleTasks.get(i));
            if( current_task_duration < min_duration){
                min_duration = current_task_duration;
                min_index = i;
            }
        }

        Task answer = possibleTasks.get(min_index);
        possibleTasks.remove(min_index);

        return answer;
    }


    Task makeLRPTchoice(Instance instance, ArrayList<Task> possibleTasks){

        int max_left_duration = -1;
        Task answer = null;

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

        possibleTasks.remove(answer);

        return answer;
    }


}
