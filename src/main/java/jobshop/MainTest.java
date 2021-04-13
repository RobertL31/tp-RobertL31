package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.encodings.Task;

import java.io.IOException;
import java.nio.file.Paths;

/** A java main classes for testing purposes. */
public class MainTest {

    public static void main(String[] args) {
        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

            // builds a solution in the job-numbers encoding [0 0 1 1 0 1]
            JobNumbers enc = new JobNumbers(instance);
            enc.addTaskOfJob(0);
            enc.addTaskOfJob(0);
            enc.addTaskOfJob(1);
            enc.addTaskOfJob(1);
            enc.addTaskOfJob(0);
            enc.addTaskOfJob(1);

            System.out.println("\nENCODING: " + enc);

            // convert to a schedule and display
            Schedule schedule = enc.toSchedule().get();
            System.out.println("VALID: " + schedule.isValid());
            System.out.println("MAKESPAN: " + schedule.makespan());
            System.out.println("SCHEDULE: " + schedule.toString());
            System.out.println("GANTT: " + schedule.asciiGantt());

            Schedule manualSchedule = new Schedule(instance);
            // TODO: encode the same solution
            //manualSchedule.setStartTime(....);

            ResourceOrder manualRO = new ResourceOrder(instance);
            // TODO: encode the same solution
            //manualRO.addTaskToMachine(..., new Task(..., ...));

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
