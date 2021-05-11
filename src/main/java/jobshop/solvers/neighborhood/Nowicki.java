package jobshop.solvers.neighborhood;

import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


/** Implementation of the Nowicki and Smutnicki neighborhood.
 *
 * It works on the ResourceOrder encoding by generating two neighbors for each block
 * of the critical path.
 * For each block, two neighbors should be generated that respectively swap the first two and
 * last two tasks of the block.
 */
public class Nowicki extends Neighborhood<ResourceOrder> {


    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    public static class Block {
        /** machine on which the block is identified */
        public final int machine;
        /** index of the first task of the block */
        public final int firstTask;
        /** index of the last task of the block */
        public final int lastTask;

        /** Creates a new block. */
        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }

        public String toString(){
            return "Task " + firstTask + " to task " + lastTask + " belong to machine " + machine;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swap with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    public static class Swap extends Neighbor<ResourceOrder> {
        /** machine on which to perform the swap */
        public final int machine;

        /** index of one task to be swapped (in the resource order encoding) */
        public final int t1;

        /** index of the other task to be swapped (in the resource order encoding) */
        public final int t2;

        /** Creates a new swap of two tasks. */
        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }


        /** Apply this swap on the given ResourceOrder, transforming it into a new solution. */
        @Override
        public void applyOn(ResourceOrder current) {
            current.swapTasks(this.machine, this.t1, this.t2);
        }

        /** Unapply this swap on the neighbor, transforming it back into the original solution. */
        @Override
        public void undoApplyOn(ResourceOrder current) {
            current.swapTasks(this.machine, this.t1, this.t2);
        }
    }


    @Override
    public List<Neighbor<ResourceOrder>> generateNeighbors(ResourceOrder current) {
        // this simply converts the list of swaps into a list of neighbors
        return new ArrayList<>(allSwaps(current));
    }

    /** Generates all swaps of the given ResourceOrder.
     * This method can be used if one wants to access the inner fields of a neighbors. */
    public List<Swap> allSwaps(ResourceOrder current) {
        List<Swap> neighbors = new ArrayList<>();
        // iterate over all blocks of the critical path
        for(var block : blocksOfCriticalPath(current)) {
            // for this block, compute all neighbors and add them to the list of neighbors
            neighbors.addAll(neighbors(block, current));
        }
        return neighbors;
    }


    private int getTaskIndexOnMachine(int machine, Task task, ResourceOrder current){
        for(int i=0; i<current.instance.numJobs; ++i){
            if( current.getTaskOfMachine(machine, i).equals(task) ){
                return i;
            }
        }
        return -1;
    }

    /** Returns a list of all the blocks of the critical path. */
    public List<Block> blocksOfCriticalPath(ResourceOrder order) {

        ArrayList<Block> answer = new ArrayList<>();
        List<Task> criticalPath = order.toSchedule().get().criticalPath();

        int currentBlockMachine = -1;
        int currentFirstTask = -1;
        int currentLastTask = -1;
        int newMachine = -1;

        for(int i=0; i<criticalPath.size(); ++i){

            newMachine = order.instance.machine(criticalPath.get(i));


            if( newMachine != currentBlockMachine ){
                // If the block size is more than one, in that case, we validate the block
                if(currentFirstTask != currentLastTask){
                    answer.add(new Block(currentBlockMachine, currentFirstTask, currentLastTask));
                }

                // Initializing next block variables
                int current_index = getTaskIndexOnMachine(newMachine, criticalPath.get(i), order );
                currentFirstTask = current_index;
                currentLastTask = current_index;
                currentBlockMachine = newMachine;

            } else {
                // Current block size increased
                currentLastTask = getTaskIndexOnMachine(currentBlockMachine, criticalPath.get(i), order );
            }

        }

        if(currentFirstTask != currentLastTask){
            answer.add(new Block(newMachine, currentFirstTask, currentLastTask));
        }

        return answer;



    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block, ResourceOrder current) {

        if(block.firstTask == block.lastTask) return null;

        ArrayList<Swap> answer = new ArrayList<>();

        if(block.lastTask - block.firstTask == 1){
            Swap swap = new Swap(block.machine, block.firstTask, block.lastTask);
            swap.applyOn(current);
            if(current.toSchedule().get() != null){
                answer.add(swap);
            } else {
                System.out.println("--\nNOT VALID RESOURCE ORDER PRODUCED\n--");
            }
            swap.undoApplyOn(current);

        } else {
            Swap swap1 = new Swap(block.machine, block.firstTask, block.firstTask+1);
            Swap swap2 = new Swap(block.machine, block.lastTask-1, block.lastTask);
            swap1.applyOn(current);
            if(current.toSchedule().get() != null){
                answer.add(swap1);
            } else {
                System.out.println("--\nNOT VALID RESOURCE ORDER PRODUCED\n--");
            }
            swap1.undoApplyOn(current);

            swap2.applyOn(current);
            if(current.toSchedule().get() != null){
                answer.add(swap2);
            } else {
                System.out.println("--\nNOT VALID RESOURCE ORDER PRODUCED\n--");
            }
            swap2.undoApplyOn(current);
        }

        return answer;
    }

}
