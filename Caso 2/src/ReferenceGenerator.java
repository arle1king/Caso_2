import java.io.*;
import java.util.*;

public class ReferenceGenerator {

    public void generateFromConfig(String configFile) {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(configFile));

            int pageSize = Integer.parseInt(props.getProperty("TP"));
            int numProcesses = Integer.parseInt(props.getProperty("NPROC"));
            String[] matrixSizes = props.getProperty("TAMS").split(",");

            if (matrixSizes.length != numProcesses) {
                System.out.println("Error: El número de tamaños de matriz no coincide con el número de procesos");
                return;
            }

            for (int i = 0; i < numProcesses; i++) {
                int matrixSize = Integer.parseInt(matrixSizes[i].trim());
                generateProcessReferences(i, pageSize, matrixSize, matrixSize);
            }

            System.out.println("Generación completada. Se crearon " + numProcesses + " archivos de proceso.");

        } catch (IOException e) {
            System.out.println("Error leyendo archivo de configuración: " + e.getMessage());
        }
    }

    public void generateProcessReferences(int processId, int pageSize, int numRows, int numCols) {
        List<Integer> addresses = new ArrayList<>();
        int totalElements = numRows * numCols;

        int baseMatriz1 = 0;
        int baseMatriz2 = totalElements * 4;
        int baseMatriz3 = totalElements * 8;

        for (int matrix = 0; matrix < 3; matrix++) {
            int baseAddress = (matrix == 0) ? baseMatriz1 : (matrix == 1) ? baseMatriz2 : baseMatriz3;

            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numCols; j++) {
                    int address = baseAddress + (i * numCols + j) * 4;
                    addresses.add(address);
                }
            }
        }

        writeToFile(processId, pageSize, numRows, numCols, addresses);
    }

    private void writeToFile(int processId, int pageSize, int numRows, int numCols, List<Integer> addresses) {
        String filename = "proc" + processId + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("TP: " + pageSize);
            writer.println("NF: " + numRows);
            writer.println("NC: " + numCols);
            writer.println("NR: " + addresses.size());
            writer.println("NP: " + calculateVirtualPages(pageSize, numRows, numCols));
            writer.println("Direcciones:");

            for (int address : addresses) {
                writer.println(address);
            }

        } catch (IOException e) {
            System.out.println("Error escribiendo archivo " + filename + ": " + e.getMessage());
        }
    }

    private int calculateVirtualPages(int pageSize, int numRows, int numCols) {
        int totalBytes = 3 * numRows * numCols * 4;
        return (int) Math.ceil((double) totalBytes / pageSize);
    }
}