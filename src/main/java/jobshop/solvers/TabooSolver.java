package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.neighborhood.Neighbor;
import jobshop.solvers.neighborhood.Neighborhood;
import jobshop.solvers.neighborhood.Nowicki;

import java.util.ArrayList;
import java.util.List;

/** An empty shell to implement a descent solver. */
public class TabooSolver implements Solver {

    final Neighborhood<ResourceOrder> neighborhood;
    final Solver baseSolver;
    final int maxIter;
    final int tabooTime;
    int numIter;

    private ResourceOrder bestRO;

    private int[][] tabooMatrice;

    private ArrayList<Integer> makespans = new ArrayList<Integer>();

    /** Creates a new descent solver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generates neighbor solutions to the current candidate.
     * @param baseSolver A solver to provide the initial solution.
     */
    public TabooSolver(Neighborhood<ResourceOrder> neighborhood, Solver baseSolver, int maxIter, int tabooTime) {
        this.neighborhood = neighborhood;
        this.baseSolver = baseSolver;
        this.maxIter = maxIter;
        this.tabooTime = tabooTime;
        this.numIter = 0;
    }

    @Override
    public Result solve(Instance instance, long deadline) {

        this.tabooMatrice =
                new int[instance.numTasks * instance.numJobs][instance.numTasks * instance.numJobs];

        Solver sol = baseSolver;
        Result res = sol.solve(instance, deadline);
        ResourceOrder ro = new ResourceOrder(res.schedule.get());
        bestRO = ro.copy();


        while(deadline - System.currentTimeMillis() > 1){

            ++numIter;

            Nowicki nowicki = (Nowicki) neighborhood;
            List<Neighbor<ResourceOrder>> neighbors = nowicki.generateNeighbors(ro);

            if(neighbors.isEmpty()){
                return new Result(instance, ro.toSchedule(), Result.ExitCause.Blocked);
            }

            Neighbor<ResourceOrder> best = null;
            int best_makespan = Integer.MAX_VALUE;

            /*
            for(int i=0; i<neighbors.size(); ++i){
                Neighbor<ResourceOrder> actual = neighbors.get(i);
                if( ! forbidden(actual, instance) ) {
                    actual.applyOn(ro);
                    int new_makespan = ro.toSchedule().get().makespan();
                    actual.undoApplyOn(ro);
                    if (new_makespan < best_makespan) {
                        best_makespan = new_makespan;
                        best = actual;
                    }
                }
            }*/
/*
            for(int i=0; i<neighbors.size(); ++i){
                Neighbor<ResourceOrder> actual = neighbors.get(i);
                actual.applyOn(ro);
                int new_makespan = ro.toSchedule().get().makespan();
                actual.undoApplyOn(ro);
                // If it's even better than actual makespan, we take it anyway
                if(new_makespan < bestRO.toSchedule().get().makespan()){
                    best_makespan = new_makespan;
                    best = actual;
                } else {
                    // Otherwise, we apply classical taboo method
                    if( ! forbidden(actual, instance) && new_makespan < best_makespan) {
                            best_makespan = new_makespan;
                            best = actual;
                    }
                }
            }*/

            for(int i=0; i<neighbors.size(); ++i){
                Neighbor<ResourceOrder> actual = neighbors.get(i);
                actual.applyOn(ro);
                int new_makespan = ro.toSchedule().get().makespan();
                actual.undoApplyOn(ro);
                // If it's even better than actual makespan, we take it anyway
                if(forbidden(actual,instance) && new_makespan < bestRO.toSchedule().get().makespan()){
                    best_makespan = new_makespan;
                    best = actual;
                } else {
                    // Otherwise, we apply classical taboo method
                    if( ! forbidden(actual, instance) && new_makespan < best_makespan) {
                        best_makespan = new_makespan;
                        best = actual;
                    }
                }
            }



            if(best != null){
                makespans.add(best_makespan);
                best.applyOn(ro);

                forbid(best, instance);

                if(best_makespan < bestRO.toSchedule().get().makespan()){
                    bestRO = ro.copy();
                }
            }

            if(numIter == maxIter){
                return new Result(instance, bestRO.toSchedule(), Result.ExitCause.MaxIteration);
            }
        }

        return new Result(instance, ro.toSchedule(), Result.ExitCause.Timeout);
    }

    public ArrayList<Integer> getMakespans(){
        return this.makespans;
    }

    private void forbid(Neighbor<ResourceOrder> forbidden, Instance instance){
        Nowicki.Swap swap = (Nowicki.Swap) forbidden;
        Task t1 = bestRO.getTaskOfMachine(swap.machine, swap.t1);
        Task t2 = bestRO.getTaskOfMachine(swap.machine, swap.t2);
        int t1_index = t1.job * instance.numTasks + t1.task;
        int t2_index = t2.job * instance.numTasks + t2.task;
        tabooMatrice[t1_index][t2_index] = numIter + tabooTime;
        tabooMatrice[t2_index][t1_index] = numIter + tabooTime;
    }

    private boolean forbidden(Neighbor<ResourceOrder> neighbor, Instance instance){
        Nowicki.Swap swap = (Nowicki.Swap) neighbor;
        Task t1 = bestRO.getTaskOfMachine(swap.machine, swap.t1);
        Task t2 = bestRO.getTaskOfMachine(swap.machine, swap.t2);
        int t1_index = t1.job * instance.numTasks + t1.task;
        int t2_index = t2.job * instance.numTasks + t2.task;
        return tabooMatrice[t1_index][t2_index] > numIter + tabooTime
                ||  tabooMatrice[t2_index][t1_index] > numIter + tabooTime;
    }

}
