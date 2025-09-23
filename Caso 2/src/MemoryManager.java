import java.util.*;

public class MemoryManager {
    private int totalFrames;
    private boolean[] frameAllocation;
    private Map<Integer, Integer> frameToProcess;
    private Map<Integer, Integer> frameToVirtualPage;
    private Map<Integer, List<Integer>> processFrames;
    private LRUReplacementPolicy lruPolicy;
    private Map<Integer, Process> allProcesses;

    public MemoryManager(int totalFrames, Map<Integer, Process> processes) {
        this.totalFrames = totalFrames;
        this.frameAllocation = new boolean[totalFrames];
        this.frameToProcess = new HashMap<>();
        this.frameToVirtualPage = new HashMap<>();
        this.processFrames = new HashMap<>();
        this.lruPolicy = new LRUReplacementPolicy();
        this.allProcesses = new HashMap<>(processes);

        for (int i = 0; i < totalFrames; i++) {
            frameAllocation[i] = false;
        }
    }

    public void initializeProcessFrames(int processId, int numFrames) {
        processFrames.put(processId, new ArrayList<>());

        for (int i = 0; i < numFrames && getFreeFrameCount() > 0; i++) {
            int freeFrame = findFreeFrame();
            if (freeFrame != -1) {
                allocateFrameToProcess(processId, freeFrame);
            }
        }
    }

    public int allocateFrame(int processId, int virtualPage) {
        int freeFrame = findFreeFrame();
        if (freeFrame != -1) {
            allocateFrameToProcess(processId, freeFrame);
            frameToVirtualPage.put(freeFrame, virtualPage);
            lruPolicy.updateAccessTime(freeFrame, System.nanoTime());
            return freeFrame;
        }

        return replaceFrame(processId, virtualPage);
    }

    private int replaceFrame(int processId, int virtualPage) {
        List<Integer> candidateFrames = processFrames.get(processId);
        if (candidateFrames == null || candidateFrames.isEmpty()) {
            return -1;
        }

        int victimFrame = lruPolicy.selectVictim(candidateFrames);
        if (victimFrame != -1) {
            int oldProcessId = frameToProcess.get(victimFrame);
            int oldVirtualPage = frameToVirtualPage.get(victimFrame);

            Process oldProcess = allProcesses.get(oldProcessId);
            if (oldProcess != null) {
                oldProcess.notifyPageRemoved(oldVirtualPage);
            }

            frameToProcess.remove(victimFrame);
            frameToVirtualPage.remove(victimFrame);

            List<Integer> oldFrames = processFrames.get(oldProcessId);
            if (oldFrames != null) {
                oldFrames.remove((Integer) victimFrame);
            }

            frameToProcess.put(victimFrame, processId);
            frameToVirtualPage.put(victimFrame, virtualPage);

            if (!processFrames.containsKey(processId)) {
                processFrames.put(processId, new ArrayList<>());
            }
            processFrames.get(processId).add(victimFrame);

            lruPolicy.updateAccessTime(victimFrame, System.nanoTime());

            return victimFrame;
        }

        return -1;
    }

    private int findFreeFrame() {
        for (int i = 0; i < totalFrames; i++) {
            if (!frameAllocation[i]) {
                return i;
            }
        }
        return -1;
    }

    private void allocateFrameToProcess(int processId, int frame) {
        frameAllocation[frame] = true;
        frameToProcess.put(frame, processId);

        if (!processFrames.containsKey(processId)) {
            processFrames.put(processId, new ArrayList<>());
        }
        processFrames.get(processId).add(frame);
    }

    public int getFreeFrameCount() {
        int count = 0;
        for (boolean allocated : frameAllocation) {
            if (!allocated) {
                count++;
            }
        }
        return count;
    }

    public void freeProcessFrames(int processId) {
        List<Integer> frames = processFrames.get(processId);
        if (frames != null) {
            for (int frame : frames) {
                frameAllocation[frame] = false;
                frameToProcess.remove(frame);
                frameToVirtualPage.remove(frame);
                lruPolicy.removeFrame(frame);
            }
            processFrames.remove(processId);
        }
    }

    public List<Integer> getProcessFrames(int processId) {
        return processFrames.getOrDefault(processId, new ArrayList<>());
    }

    public void handleWriteAccess(int processId, int virtualAddress) {
        int pageNumber = virtualAddress / getPageSize(processId);
        Process process = allProcesses.get(processId);
        if (process != null) {
            process.markPageAsModified(pageNumber);
        }
    }

    private int getPageSize(int processId) {
        Process process = allProcesses.get(processId);
        return (process != null) ? process.getPageSize() : 4096;
    }

    public Map<Integer, Integer> getFrameUsageStatistics() {
        Map<Integer, Integer> usage = new HashMap<>();
        for (int i = 0; i < totalFrames; i++) {
            if (frameAllocation[i]) {
                int processId = frameToProcess.get(i);
                usage.put(processId, usage.getOrDefault(processId, 0) + 1);
            }
        }
        return usage;
    }
}