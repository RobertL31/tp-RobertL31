package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.encodings.ResourceOrder;
import jobshop.solvers.neighborhood.Neighbor;
import jobshop.solvers.neighborhood.Neighborhood;
import jobshop.solvers.neighborhood.Nowicki;

import java.util.ArrayList;
import java.util.List;

/** An empty shell to implement a descent solver. */
public class DescentSolver implements Solver {

    final Neighborhood<ResourceOrder> neighborhood;
    final Solver baseSolver;

    public ArrayList<Integer> makespans = new ArrayList<Integer>();

    /** Creates a new descent solver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generates neighbor solutions to the current candidate.
     * @param baseSolver A solver to provide the initial solution.
     */
    public DescentSolver(Neighborhood<ResourceOrder> neighborhood, Solver baseSolver) {
        this.neighborhood = neighborhood;
        this.baseSolver = baseSolver;
    }

    @Override
    public Result solve(Instance instance, long deadline) {

        GreedySolver sol = (GreedySolver) baseSolver;
        Result res = sol.solve(instance, deadline);
        ResourceOrder greedyRO = sol.getResourceOrder();


        while(deadline - System.currentTimeMillis() > 1){

            Nowicki nowicki = (Nowicki) neighborhood;
            List<Neighbor<ResourceOrder>> neighbors = nowicki.generateNeighbors(greedyRO);

            if(neighbors.isEmpty()){
                return new Result(instance, greedyRO.toSchedule(), Result.ExitCause.Blocked);
            }

            Neighbor<ResourceOrder> best = neighbors.get(0);
            best.applyOn(greedyRO);
            int best_makespan = greedyRO.toSchedule().get().makespan();
            best.undoApplyOn(greedyRO);

            for(int i=1; i<neighbors.size(); ++i){
                neighbors.get(i).applyOn(greedyRO);
                int new_makespan = greedyRO.toSchedule().get().makespan();
                neighbors.get(i).undoApplyOn(greedyRO);
                if( new_makespan < best_makespan){
                    best_makespan = new_makespan;
                    best = neighbors.get(i);
                }
            }

            if(greedyRO.toSchedule().get().makespan() > best_makespan){
                makespans.add(best_makespan);
                best.applyOn(greedyRO);
            } else {
                return new Result(instance, greedyRO.toSchedule(), Result.ExitCause.Blocked);

            }
        }


        return new Result(instance, greedyRO.toSchedule(), Result.ExitCause.Timeout);
    }

}
