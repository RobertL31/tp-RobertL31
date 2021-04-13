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
            manualSchedule.setStartTime(0,0,0);
            manualSchedule.setStartTime(0,1,3);
            manualSchedule.setStartTime(0,2,6);
            manualSchedule.setStartTime(1,0,6);
            manualSchedule.setStartTime(1,1,8);
            manualSchedule.setStartTime(1,2,10);

            System.out.println("\n\nMY MANUAL SCHEDULE GANTT:" + manualSchedule.asciiGantt());


            ResourceOrder manualRO = new ResourceOrder(instance);
            manualRO.addTaskToMachine(0, new Task(0,0));
            manualRO.addTaskToMachine(1, new Task(0,1));
            manualRO.addTaskToMachine(2, new Task(0,2));
            manualRO.addTaskToMachine(1, new Task(1,0));
            manualRO.addTaskToMachine(0, new Task(1,1));
            manualRO.addTaskToMachine(2, new Task(1,2));

            System.out.println("\n\nMY MANUAL RESOURCE ORDER GANTT:" + manualRO.toSchedule().get().asciiGantt());


            ResourceOrder optimalRO = new ResourceOrder(instance);
            optimalRO.addTaskToMachine(0, new Task(0,0));
            optimalRO.addTaskToMachine(1, new Task(1,0));
            optimalRO.addTaskToMachine(1, new Task(0,1));
            optimalRO.addTaskToMachine(0, new Task(1,1));
            optimalRO.addTaskToMachine(2, new Task(1,2));
            optimalRO.addTaskToMachine(2, new Task(0,2));

            System.out.println("\n\nMY OPTIMAL RESOURCE ORDER GANTT:" + optimalRO.toSchedule().get().asciiGantt());

        /*
            // Bellow is the code that can crash the app
            ResourceOrder crashRO = new ResourceOrder(instance);
            crashRO.addTaskToMachine(0, new Task(0,1));

            // We changed above task to try putting on machine 0 a wrong task.
            // As the solution we are building no longer satisfies the constraints of the instance,
            // the program crashes producing a RunTimeException when trying to schedule Task(0,1) on machine 0

            crashRO.addTaskToMachine(1, new Task(1,0));
            crashRO.addTaskToMachine(1, new Task(0,1));
            crashRO.addTaskToMachine(0, new Task(1,1));
            crashRO.addTaskToMachine(2, new Task(1,2));
            crashRO.addTaskToMachine(2, new Task(0,2));
        */

            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
