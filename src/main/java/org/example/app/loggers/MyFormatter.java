package org.example.app.loggers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MyFormatter extends Formatter {

    private final Map<Level, Color> colorMap;

    public enum Color {
        RESET("\u001B[0m"),
        BLACK("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),
        CYAN("\u001B[36m"),
        WHITE("\u001B[37m");

        Color(String ANSI) {
            this.ANSI = ANSI;
        }

        String ANSI;
    }

    public MyFormatter() {
        this(getDefaultColorMap());
    }

    private static Map<Level, Color> getDefaultColorMap() {
        Map<Level, Color> colorMap = new HashMap<>(7);

        colorMap.put(Level.SEVERE, Color.RED);
        colorMap.put(Level.WARNING, Color.YELLOW);
        colorMap.put(Level.INFO, Color.BLUE);
        colorMap.put(Level.CONFIG, Color.CYAN);
        colorMap.put(Level.FINE, Color.GREEN);
        colorMap.put(Level.FINER, Color.GREEN);
        colorMap.put(Level.FINEST, Color.GREEN);

        return colorMap;
    }

    public MyFormatter(Map<Level, Color> colorMap) {
        this.colorMap = colorMap;
    }

    @Override
    public String format(LogRecord record) {
        Level level = record.getLevel();
        Color color = colorMap.getOrDefault(level, Color.BLACK);

        StringBuilder builder = new StringBuilder();
        builder.append(color.ANSI);

        builder.append("[");
        builder.append(calcDate(record.getMillis()));
        builder.append("]");

        builder.append(" [");
        builder.append(record.getSourceClassName());
        builder.append("]");

        builder.append(" [");
        builder.append(level.getName());
        builder.append("]");

        builder.append(color.ANSI);
        builder.append(" - ");
        builder.append(record.getMessage());

        Object[] params = record.getParameters();

        if (params != null)
        {
            builder.append("\t");
            for (int i = 0; i < params.length; i++)
            {
                builder.append(params[i]);
                if (i < params.length - 1)
                    builder.append(", ");
            }
        }

        builder.append(color.ANSI);
        builder.append("\n");
        return builder.toString();
    }

    private String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date resultDate = new Date(millisecs);
        return date_format.format(resultDate);
    }
}
