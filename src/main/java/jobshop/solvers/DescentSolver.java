package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.encodings.ResourceOrder;
import jobshop.solvers.neighborhood.Neighborhood;

/** An empty shell to implement a descent solver. */
public class DescentSolver implements Solver {

    final Neighborhood<ResourceOrder> neighborhood;
    final Solver baseSolver;

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
        throw new UnsupportedOperationException();
    }

}
