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

    private ArrayList<Integer> makespans = new ArrayList<Integer>();

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

        Solver sol = baseSolver;
        Result res = sol.solve(instance, deadline);
        ResourceOrder ro = new ResourceOrder(res.schedule.get());


        while(deadline - System.currentTimeMillis() > 1){

            Nowicki nowicki = (Nowicki) neighborhood;
            List<Neighbor<ResourceOrder>> neighbors = nowicki.generateNeighbors(ro);

            if(neighbors.isEmpty()){
                return new Result(instance, ro.toSchedule(), Result.ExitCause.Blocked);
            }

            Neighbor<ResourceOrder> best = neighbors.get(0);
            best.applyOn(ro);
            int best_makespan = ro.toSchedule().get().makespan();
            best.undoApplyOn(ro);

            for(int i=1; i<neighbors.size(); ++i){
                neighbors.get(i).applyOn(ro);
                int new_makespan = ro.toSchedule().get().makespan();
                neighbors.get(i).undoApplyOn(ro);
                if( new_makespan < best_makespan){
                    best_makespan = new_makespan;
                    best = neighbors.get(i);
                }
            }

            if(ro.toSchedule().get().makespan() > best_makespan){
                makespans.add(best_makespan);
                best.applyOn(ro);
            } else {
                return new Result(instance, ro.toSchedule(), Result.ExitCause.Blocked);

            }
        }


        return new Result(instance, ro.toSchedule(), Result.ExitCause.Timeout);
    }

    public ArrayList<Integer> getMakespans(){
        return this.makespans;
    }

    public void deleteMakespan(){ this.makespans.clear();}
}
