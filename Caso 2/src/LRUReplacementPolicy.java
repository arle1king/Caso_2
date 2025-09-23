import java.util.*;

public class LRUReplacementPolicy {
    private Map<Integer, Long> accessTimes;

    public LRUReplacementPolicy() {
        this.accessTimes = new HashMap<>();
    }

    public int selectVictim(List<Integer> candidateFrames) {
        if (candidateFrames == null || candidateFrames.isEmpty()) {
            return -1;
        }

        int victimFrame = -1;
        long oldestTime = Long.MAX_VALUE;

        for (int frame : candidateFrames) {
            Long accessTime = accessTimes.get(frame);
            if (accessTime != null && accessTime < oldestTime) {
                oldestTime = accessTime;
                victimFrame = frame;
            }
        }

        return victimFrame;
    }

    public void updateAccessTime(int frame, long time) {
        accessTimes.put(frame, time);
    }

    public void removeFrame(int frame) {
        accessTimes.remove(frame);
    }

    public long getAccessTime(int frame) {
        return accessTimes.getOrDefault(frame, 0L);
    }
}