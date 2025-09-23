import java.util.*;

public class PageTable {
    private Map<Integer, PageTableEntry> entries;

    public PageTable() {
        this.entries = new HashMap<>();
    }

    public PageTableEntry getEntry(int virtualPage) {
        if (!entries.containsKey(virtualPage)) {
            entries.put(virtualPage, new PageTableEntry(virtualPage));
        }
        return entries.get(virtualPage);
    }

    public boolean isPagePresent(int virtualPage) {
        return entries.containsKey(virtualPage) && entries.get(virtualPage).isPresent();
    }

    public Integer getPhysicalFrame(int virtualPage) {
        if (entries.containsKey(virtualPage)) {
            return entries.get(virtualPage).getPhysicalFrame();
        }
        return null;
    }

    public void setPagePresent(int virtualPage, int physicalFrame) {
        PageTableEntry entry = getEntry(virtualPage);
        entry.setPhysicalFrame(physicalFrame);
        entry.setPresent(true);
        entry.setLastAccessTime(System.nanoTime());
    }

    public void updateAccessTime(int virtualPage) {
        if (entries.containsKey(virtualPage)) {
            entries.get(virtualPage).setLastAccessTime(System.nanoTime());
        }
    }

    public Collection<PageTableEntry> getAllEntries() {
        return entries.values();
    }

    public Integer getVirtualPageFromFrame(int physicalFrame) {
        for (PageTableEntry entry : entries.values()) {
            if (entry.isPresent() && entry.getPhysicalFrame() == physicalFrame) {
                return entry.getVirtualPage();
            }
        }
        return null;
    }

    public static class PageTableEntry {
        private int virtualPage;
        private Integer physicalFrame;
        private boolean present;
        private boolean referenced;
        private boolean modified;
        private long lastAccessTime;

        public PageTableEntry(int virtualPage) {
            this.virtualPage = virtualPage;
            this.present = false;
            this.referenced = false;
            this.modified = false;
            this.lastAccessTime = 0;
        }

        public int getVirtualPage() {
            return virtualPage;
        }

        public Integer getPhysicalFrame() {
            return physicalFrame;
        }

        public void setPhysicalFrame(Integer frame) {
            this.physicalFrame = frame;
        }

        public boolean isPresent() {
            return present;
        }

        public void setPresent(boolean present) {
            this.present = present;
        }

        public boolean isReferenced() {
            return referenced;
        }

        public void setReferenced(boolean referenced) {
            this.referenced = referenced;
        }

        public boolean isModified() {
            return modified;
        }

        public void setModified(boolean modified) {
            this.modified = modified;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }

        public void setLastAccessTime(long time) {
            this.lastAccessTime = time;
        }
    }
}