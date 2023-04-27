import japicmp.model.JApiChangeStatus;
import japicmp.model.JApiClass;
import japicmp.model.JApiMethod;

public abstract class ReportGenerator {
    protected boolean enableClassReport = Boolean.TRUE;
    protected boolean enableMethodReport = Boolean.FALSE;

    void setEnableClassReport(Boolean enable) {
        enableClassReport = enable;
    }

    void setEnableMethodReport(Boolean enable) {
        enableMethodReport = enable;
    }

    abstract void record(JApiChangeStatus status, JApiClass apiClass);

    abstract void record(JApiChangeStatus status, JApiMethod jApiMethod);

    abstract void flush();
}
