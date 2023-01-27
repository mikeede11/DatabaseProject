package edu.yu.dbimpl;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MyFormatter extends Formatter {
        public String format(LogRecord rec) {
            StringBuffer buf = new StringBuffer(5000);
            buf.append(" INFO: ");
            buf.append(' ');
            buf.append(formatMessage(rec));
            buf.append('\n');
            return buf.toString();
        }

        public String getHead(Handler h) {
            return ""; // Here instead of the Timestamp, Atul will be written to the exception trace in the log
        }

        public String getTail(Handler h) {
            return "";
        }
}
