
public class VirtualMemorySimulator {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java VirtualMemorySimulator <opcion> [parametros]");
            System.out.println("Opcion 1: -g <archivo_config>");
            System.out.println("Opcion 2: -s <num_procesos> <num_marcos>");
            return;
        }

        VirtualMemorySimulator simulator = new VirtualMemorySimulator();

        if (args[0].equals("-g") && args.length >= 2) {
            simulator.generateReferences(args[1]);
        } else if (args[0].equals("-s") && args.length >= 3) {
            int numProcesses = Integer.parseInt(args[1]);
            int totalFrames = Integer.parseInt(args[2]);
            simulator.simulateExecution(numProcesses, totalFrames);
        } else {
            System.out.println("Parametros incorrectos");
        }
    }

    public void generateReferences(String configFile) {
        ReferenceGenerator generator = new ReferenceGenerator();
        generator.generateFromConfig(configFile);
    }

    public void simulateExecution(int numProcesses, int totalFrames) {
        ExecutionSimulator executor = new ExecutionSimulator();
        executor.simulate(numProcesses, totalFrames);
    }
}