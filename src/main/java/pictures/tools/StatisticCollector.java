package pictures.tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import javax.swing.table.TableModel;
import rzx.ui.ZxBaseTableModel;

/**
 * Collects statistic data during the execution of the program.
 *
 * @author rene
 */
public class StatisticCollector extends ZxBaseTableModel {

    private static int m_pictureCount = 0;     // Counts pictures loaded
    private static int m_duplicatePictureCount = 0;  // Duplicates found, e.g. during import
    private static long m_dbStartSaveTime = 0L;
    private static long m_dbSaveTime = 0L;              // time for database operation
    private static long m_dbStartSearchTime = 0L;
    private static long m_dbSearchTime = 0L;              // time for database operation
    private static long m_compressStartTime = 0L;
    private static long m_compressTime = 0L;        // time to build the thumbnail
    private static long m_aggregatedThumbSize = 0L;
    private static Date m_startTime;
    private static Date m_endTime;
    private ArrayList<String> m_duplicatePictures = null;
    private int m_refreshCounter = 0;     // only used, if only duplicates
    //  are on a medium (never use the compress method)
    private Runtime m_thisRuntime = null;        // to read memory...
    private long m_maxMemoryUsed = 0L;
    private ArrayList<String[]> m_errors = null;

    private StringBuilder m_longBuilder = new StringBuilder();
    private Formatter m_longFormatter = new Formatter(m_longBuilder);

    private static final String LABELS[] = {
        "Start Time", "End Time", "Duration (sec)", "New Pictures", "Duplicates",
        "Aggregated Thumb Size (kb)", "Average Thumb Size (b)", "DB Save Time (sec)", "DB Search Time (sec)", "Compress Time (sec)",
        "Memory Used", "Max. Memory Used", "Free Memory", "Max Memory Available", "Error Count"
    };

    private static final String ERROR_TABLE_LABELS[] = {
        "File", "Folder", "Error"
    };

    private StatisticCollector() {
        m_thisRuntime = Runtime.getRuntime();
    }

    public static StatisticCollector getInstance() {
        return StatisticCollectorHolder.INSTANCE;
    }

    private static class StatisticCollectorHolder {

        private static final StatisticCollector INSTANCE = new StatisticCollector();
    }

    public void reset() {
        m_pictureCount = 0;
        m_aggregatedThumbSize = 0L;
        m_duplicatePictureCount = 0;
        m_dbSaveTime = 0;
        m_dbSearchTime = 0;
        m_compressTime = 0;
        m_startTime = null;
        m_endTime = null;
        m_duplicatePictures = new ArrayList<>();
        m_errors = new ArrayList<>();
        m_refreshCounter = 0;
        m_maxMemoryUsed = 0L;
        this.fireAllTableModelListener();
    }

    public String getDuplicatePictureCountAsString() {
        return Long.toString(m_duplicatePictureCount);
    }

    public ArrayList<String> getDuplicatePictureList() {
        return m_duplicatePictures;
    }

    public String getPictureCountAsString() {
        return Long.toString(m_pictureCount);
    }

    public String getDbSaveTimeAsString() {
        return Long.toString(m_dbSaveTime / 1000);     // Value is in msec
    }

    public String getDbSearchTimeAsString() {
        return Long.toString(m_dbSearchTime / 1000);     // Value is in msec
    }

    public String getCompressTimeAsString() {
        return Long.toString(m_compressTime / 1000);
    }

    public String getAggregatedThumbSizeAsString() {
        return formatLong(m_aggregatedThumbSize / 1000);
    }

    public String getAverageThumbSizeAsString() {
        if (m_pictureCount == 0)
            return "No thumbnail loaded to database";   // @todo use ressource / multilanguage
        else
//            return Long.toString(m_aggregatedThumbSize / m_pictureCount);
            return formatLong(m_aggregatedThumbSize / m_pictureCount);
    }

    public String getStartTimeAsString() {
        return getDateAsString(m_startTime, "Not started");
    }

    public String getEndTimeAsString() {
        return getDateAsString(m_endTime, "Not finished");
    }

    public String getDateAsString(Date date, String message) {
        if (date == null)
            return message;
        else
            return new SimpleDateFormat("hh:mm:ss").format(date);
    }

    public String getDurationAsString() {
        try {
            long et;
            if (m_endTime == null)
                et = new Date().getTime();
            else
                et = m_endTime.getTime();
            long duration = (et - m_startTime.getTime()) / 1000;
            return Long.toString(duration);
        } catch (NullPointerException ex) {
            return "";
        }
    }

    public long getCurrentMemory() {
        long currentMemory = m_thisRuntime.totalMemory() - getFreeMemory();
        if (currentMemory > m_maxMemoryUsed)
            m_maxMemoryUsed = currentMemory;
        return currentMemory;
    }

    public String getCurrentMemoryAsString() {
        return formatLong(getCurrentMemory());
    }

    public String getMaxMemoryUsedAsString() {
        return formatLong(m_maxMemoryUsed);
    }

    public long getFreeMemory() {
        return m_thisRuntime.freeMemory();
    }

    public String getFreeMemoryAsString() {
        return formatLong(m_thisRuntime.freeMemory());
    }

    public long getMaxMemory() {
        return m_thisRuntime.maxMemory();
    }

    public String getMaxMemoryAsString() {
        return formatLong(m_thisRuntime.maxMemory());
    }

    public String getErrorCountAsString() {
        if (m_errors == null)
            return "0";
        else
            return Integer.toString(m_errors.size());
    }

    public TableModel getErrorTabelModel() {
        return errorDataModel;
    }

    // Formatter
    private String formatLong(long value) {
        m_longBuilder = new StringBuilder();
        m_longFormatter = new Formatter(m_longBuilder);
        m_longFormatter.format("%,d", value);
        return m_longBuilder.toString();
    }

    // Data collector
    public void countPicture() {
        m_pictureCount++;
    }

    public void countDuplicate() {
        m_duplicatePictureCount++;
    }

    public void countDuplicate(String fileName) {
        countDuplicate();
        m_duplicatePictures.add(fileName);
        if (m_refreshCounter < 10)
            m_refreshCounter++;
        else {
            fireAllTableModelListener();
            m_refreshCounter = 0;
        }
    }

    public void startDBSave() {
        m_dbStartSaveTime = new Date().getTime();
    }

    public void endDBsave() {
        m_dbSaveTime += new Date().getTime() - m_dbStartSaveTime;
    }

    public void startDBsearch() {
        m_dbStartSearchTime = new Date().getTime();
    }

    public void endDBsearch() {
        m_dbSearchTime += new Date().getTime() - m_dbStartSearchTime;
    }

    public void startCompress() {
        m_compressStartTime = new Date().getTime();
    }

    public void endCompress() {
        m_compressTime += new Date().getTime() - m_compressStartTime;
        this.fireAllTableModelListener();
    }

    public void addThumbSize(long size) {
        m_aggregatedThumbSize += size;
    }

    public void startExecution() {
        m_startTime = new Date();
    }

    public void endExecution() {
        m_endTime = new Date();
        this.fireAllTableModelListener();
    }

    public void addError(String file, String folder, String error) {
        String entry[] = {file, folder, error};
        m_errors.add(entry);
    }

    // Implementation of TableModel
    @Override
    public int getRowCount() {
        return LABELS.length;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return LABELS[rowIndex];
        else
            switch (rowIndex) {
                case 0:
                    return getStartTimeAsString();
                case 1:
                    return getEndTimeAsString();
                case 2:
                    return getDurationAsString();
                case 3:
                    return getPictureCountAsString();
                case 4:
                    return getDuplicatePictureCountAsString();
                case 5:
                    return getAggregatedThumbSizeAsString();
                case 6:
                    return getAverageThumbSizeAsString();
                case 7:
                    return getDbSaveTimeAsString();
                case 8:
                    return getDbSearchTimeAsString();
                case 9:
                    return getCompressTimeAsString();
                case 10:
                    return getCurrentMemoryAsString();
                case 11:
                    return getMaxMemoryUsedAsString();
                case 12:
                    return getFreeMemoryAsString();
                case 13:
                    return getMaxMemoryAsString();
                case 14:
                    return getErrorCountAsString();
                default:
                    return "undefined column";
            }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    ZxBaseTableModel errorDataModel = new ZxBaseTableModel() {
        @Override
        public int getRowCount() {
            return m_errors.size();
        }

        @Override
        public int getColumnCount() {
            return ERROR_TABLE_LABELS.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return ERROR_TABLE_LABELS[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return m_errors.get(rowIndex)[columnIndex];
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
}
