import java.io.*;
import java.util.*;

public class Process {
    private int pid;
    private int numRows;
    private int numCols;
    private List<Integer> virtualAddresses;
    private PageTable pageTable;
    private int pageFaults;
    private int swapAccesses;
    private int totalReferences;
    private int currentAddressIndex;
    private int pageSize;

    public Process(int pid, int pageSize, int numRows, int numCols) {
        this.pid = pid;
        this.pageSize = pageSize;
        this.numRows = numRows;
        this.numCols = numCols;
        this.virtualAddresses = new ArrayList<>();
        this.pageTable = new PageTable();
        this.pageFaults = 0;
        this.swapAccesses = 0;
        this.totalReferences = 0;
        this.currentAddressIndex = 0;
    }

    public void loadAddressesFromFile(String filename) throws IOException {
        virtualAddresses.clear();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        boolean addressesSection = false;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("Direcciones:")) {
                addressesSection = true;
                continue;
            }

            if (addressesSection && !line.trim().isEmpty()) {
                virtualAddresses.add(Integer.parseInt(line.trim()));
            }
        }
        reader.close();

        this.totalReferences = virtualAddresses.size();
    }

    public void setVirtualAddresses(List<Integer> addresses) {
        this.virtualAddresses = new ArrayList<>(addresses);
        this.totalReferences = addresses.size();
    }

    public int getNextAddress() {
        if (currentAddressIndex < virtualAddresses.size()) {
            return virtualAddresses.get(currentAddressIndex++);
        }
        return -1;
    }

    public boolean hasMoreAddresses() {
        return currentAddressIndex < virtualAddresses.size();
    }

    public void incrementPageFaults() {
        pageFaults++;
    }

    public void incrementSwapAccesses() {
        swapAccesses++;
    }

    public void addSwapAccesses(int count) {
        swapAccesses += count;
    }

    public void notifyPageRemoved(int virtualPage) {
        PageTable.PageTableEntry entry = pageTable.getEntry(virtualPage);
        if (entry != null && entry.isPresent()) {
            if (entry.isModified()) {
                swapAccesses++;
            }
            entry.setPresent(false);
            entry.setPhysicalFrame(null);
            entry.setModified(false);
        }
    }

    public Integer getVirtualPageFromFrame(int physicalFrame) {
        return pageTable.getVirtualPageFromFrame(physicalFrame);
    }

    public void markPageAsModified(int virtualPage) {
        PageTable.PageTableEntry entry = pageTable.getEntry(virtualPage);
        if (entry != null) {
            entry.setModified(true);
        }
    }

    // Getters
    public int getPid() {
        return pid;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public int getPageFaults() {
        return pageFaults;
    }

    public int getSwapAccesses() {
        return swapAccesses;
    }

    public int getTotalReferences() {
        return totalReferences;
    }

    public PageTable getPageTable() {
        return pageTable;
    }

    public int getPageSize() {
        return pageSize;
    }

    public List<Integer> getVirtualAddresses() {
        return new ArrayList<>(virtualAddresses);
    }

    public double getPageFaultRate() {
        return (double) pageFaults / totalReferences;
    }

    public double getHitRate() {
        return 1.0 - getPageFaultRate();
    }
}