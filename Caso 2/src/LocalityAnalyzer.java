import java.util.*;

public class LocalityAnalyzer {

    public void analyzeAllProcesses(Map<Integer, Process> processes) {
        System.out.println("Análisis de localidad por proceso:");

        int totalPageHits = 0;
        int totalAccesses = 0;

        for (Process process : processes.values()) {
            List<Integer> addresses = process.getVirtualAddresses();
            if (addresses == null || addresses.isEmpty()) {
                System.out.println("Proceso " + process.getPid() + ": No hay direcciones para analizar");
                continue;
            }

            int pageHits = 0;
            int pageChanges = 0;
            int currentPage = -1;

            for (int address : addresses) {
                int page = address / process.getPageSize();
                if (page != currentPage) {
                    pageChanges++;
                    currentPage = page;
                } else {
                    pageHits++;
                }
            }

            totalPageHits += pageHits;
            totalAccesses += addresses.size();

            double localityRatio = (double) pageHits / addresses.size();
            String localityLevel = getLocalityLevel(localityRatio);

            System.out.println("Proceso " + process.getPid() + ":");
            System.out.println("  Accesos: " + addresses.size() +
                    ", Aciertos página: " + pageHits +
                    ", Cambios: " + pageChanges);
            System.out.println("  Localidad: " + String.format("%.2f%%", localityRatio * 100) +
                    " - " + localityLevel);
        }

        if (totalAccesses > 0) {
            double globalLocality = (double) totalPageHits / totalAccesses;
            System.out.println("\nLOCALIDAD GLOBAL: " +
                    String.format("%.2f%%", globalLocality * 100) + " - " +
                    getLocalityLevel(globalLocality));
        }
    }

    private String getLocalityLevel(double ratio) {
        if (ratio >= 0.8)
            return "ALTA (excelente para caché)";
        else if (ratio >= 0.6)
            return "MEDIA-ALTA (buen comportamiento)";
        else if (ratio >= 0.4)
            return "MEDIA (comportamiento regular)";
        else if (ratio >= 0.2)
            return "BAJA (muchos saltos de página)";
        else
            return "MUY BAJA (patrón aleatorio)";
    }
}