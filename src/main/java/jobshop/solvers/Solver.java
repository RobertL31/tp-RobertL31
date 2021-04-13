package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;

/** Common interface that must implemented by all solvers. */
public interface Solver {

    /** Look for a solution until blocked or a deadline has been met.
     *
     * @param instance Jobshop instance that should be solved.
     * @param deadline Absolute time at which the solver should have returned a solution.
     *                 This time is in milliseconds and can be compared with System.currentTimeMilliseconds()
     * @return A Result containing the solution found and an explanation of why the solver exited.
     */
    Result solve(Instance instance, long deadline);

    /** Static factory method to create a new solver based on its name. */
    static Solver getSolver(String name) {
        switch (name) {
            case "basic": return new BasicSolver();
            case "random": return new RandomSolver();
            case "spt": return new GreedySolver(GreedySolver.Priority.SPT);
            // TODO: add new solvers
            default: throw new RuntimeException("Unknown solver: "+ name);
        }
    }

}
