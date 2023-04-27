import japicmp.cmp.JApiCmpArchive;
import japicmp.cmp.JarArchiveComparator;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.model.JApiClass;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        JarArchiveComparatorOptions comparatorOptions = new JarArchiveComparatorOptions();
        JarArchiveComparator jarArchiveComparator = new JarArchiveComparator(comparatorOptions);
        final var oldArchives = new JApiCmpArchive(new File("/Users/imanbuhlool/Project/GitHub/tools/vavr-0.10.4.jar"), "0.10.4");
        final var newArchives = new JApiCmpArchive(new File("/Users/imanbuhlool/Project/GitHub/tools/vavr-1.0.0-SNAPSHOT.jar"), "1.0.0-SNAPSHOT");
        List<JApiClass> jApiClasses = jarArchiveComparator.compare(oldArchives, newArchives);
        final ReportGenerator reportGenerator = new MarkdownReportGenerator();
        jApiClasses.forEach(it -> {
            reportGenerator.record(it.getChangeStatus(), it);
            it.getMethods().forEach(jApiMethod -> reportGenerator.record(jApiMethod.getChangeStatus(), jApiMethod));
        });
        reportGenerator.setEnableMethodReport(Boolean.TRUE);
        reportGenerator.setEnableClassReport(Boolean.TRUE);
        reportGenerator.flush();
    }
}
