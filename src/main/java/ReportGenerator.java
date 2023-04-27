import japicmp.model.JApiChangeStatus;
import japicmp.model.JApiClass;

public interface ReportGenerator {
    void record(JApiChangeStatus status, JApiClass apiClass);
    void flush();
}
