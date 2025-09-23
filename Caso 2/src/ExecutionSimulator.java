import java.io.*;
import java.util.*;

public class ExecutionSimulator {
    private MemoryManager memoryManager;
    private Map<Integer, Process> processes;
    private LRUReplacementPolicy lruPolicy;

    public void simulate(int numProcesses, int totalFrames) {
        this.processes = new HashMap<>();

        if (totalFrames % numProcesses != 0) {
            System.out.println("Error: El número de marcos debe ser múltiplo del número de procesos");
            return;
        }

        loadProcesses(numProcesses);

        if (processes.size() != numProcesses) {
            System.out.println("Error: No se pudieron cargar todos los procesos");
            return;
        }

        this.memoryManager = new MemoryManager(totalFrames, processes);

        int framesPerProcess = totalFrames / numProcesses;
        for (Process process : processes.values()) {
            memoryManager.initializeProcessFrames(process.getPid(), framesPerProcess);
        }

        simulateRoundRobin();
        showStatistics();
    }

    private void loadProcesses(int numProcesses) {
        for (int i = 0; i < numProcesses; i++) {
            try {
                Process process = loadProcessFromFile(i);
                if (process != null) {
                    processes.put(i, process);
                }
            } catch (IOException e) {
                System.out.println("Error cargando proceso " + i + ": " + e.getMessage());
            }
        }
    }

    private Process loadProcessFromFile(int processId) throws IOException {
        String filename = "proc" + processId + ".txt";
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Archivo no encontrado: " + filename);
            return null;
        }

        BufferedReader reader = new BufferedReader(new FileReader(filename));
        int pageSize = 4096, numRows = 0, numCols = 0;
        List<Integer> addresses = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("TP:")) {
                pageSize = Integer.parseInt(line.split(":")[1].trim());
            } else if (line.startsWith("NF:")) {
                numRows = Integer.parseInt(line.split(":")[1].trim());
            } else if (line.startsWith("NC:")) {
                numCols = Integer.parseInt(line.split(":")[1].trim());
            } else if (line.startsWith("Direcciones:")) {
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        addresses.add(Integer.parseInt(line.trim()));
                    }
                }
            }
        }
        reader.close();

        Process process = new Process(processId, pageSize, numRows, numCols);
        process.setVirtualAddresses(addresses);
        return process;
    }

    private void simulateRoundRobin() {
        Queue<Process> processQueue = new LinkedList<>(processes.values());
        List<Process> completedProcesses = new ArrayList<>();
        int cycle = 0;

        System.out.println("Iniciando simulación...");

        while (!processQueue.isEmpty()) {
            Process currentProcess = processQueue.poll();
            cycle++;

            if (currentProcess.hasMoreAddresses()) {
                int virtualAddress = currentProcess.getNextAddress();
                boolean pageFault = handleMemoryAccess(currentProcess, virtualAddress);

                if (pageFault) {
                    processQueue.add(currentProcess);
                } else if (currentProcess.hasMoreAddresses()) {
                    processQueue.add(currentProcess);
                } else {
                    completedProcesses.add(currentProcess);
                    memoryManager.freeProcessFrames(currentProcess.getPid());
                    System.out.println("Proceso " + currentProcess.getPid() + " completado.");
                }
            }

            if (cycle % 1000 == 0) {
                System.out.println("Ciclo: " + cycle + ", Procesos activos: " + processQueue.size());
            }
        }

        System.out.println("Simulación completada en " + cycle + " ciclos.");
    }

    private boolean handleMemoryAccess(Process process, int virtualAddress) {
        int pageNumber = virtualAddress / process.getPageSize();
        PageTable.PageTableEntry entry = process.getPageTable().getEntry(pageNumber);

        if (entry.isPresent()) {
            entry.setLastAccessTime(System.nanoTime());
            return false; // Hit
        } else {
            process.incrementPageFaults();
            process.incrementSwapAccesses();

            int physicalFrame = memoryManager.allocateFrame(process.getPid(), pageNumber);
            if (physicalFrame != -1) {
                process.getPageTable().setPagePresent(pageNumber, physicalFrame);

                if (physicalFrame == -2) {
                    process.addSwapAccesses(1);
                }

                return true; // Page fault, reintentar
            }

            return true;
        }
    }

    private void showStatistics() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ESTADÍSTICAS FINALES DE SIMULACIÓN");
        System.out.println("=".repeat(60));

        for (Process process : processes.values()) {
            System.out.println("\nProceso " + process.getPid() + ":");
            System.out.println("  Tamaño matriz: " + process.getNumRows() + "x" + process.getNumCols());
            System.out.println("  Referencias totales: " + process.getTotalReferences());
            System.out.println("  Fallos de página: " + process.getPageFaults());
            System.out.println("  Accesos a SWAP: " + process.getSwapAccesses());
            System.out.println("  Tasa de fallos: " + String.format("%.2f%%", process.getPageFaultRate() * 100));
            System.out.println("  Tasa de éxito: " + String.format("%.2f%%", process.getHitRate() * 100));
        }

        showLocalityAnalysis();
        showFrameUsage();
    }

    private void showLocalityAnalysis() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("ANÁLISIS DE LOCALIDAD");
        System.out.println("=".repeat(40));

        LocalityAnalyzer analyzer = new LocalityAnalyzer();
        analyzer.analyzeAllProcesses(processes);
    }

    private void showFrameUsage() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("USO DE MARCOS DE MEMORIA");
        System.out.println("=".repeat(40));

        Map<Integer, Integer> usage = memoryManager.getFrameUsageStatistics();
        for (Map.Entry<Integer, Integer> entry : usage.entrySet()) {
            System.out.println("Proceso " + entry.getKey() + ": " + entry.getValue() + " marcos");
        }
    }
}