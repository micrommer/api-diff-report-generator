import japicmp.model.JApiChangeStatus;
import japicmp.model.JApiClass;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MarkdownReportGenerator implements ReportGenerator {

    private final Path reportFile;
    private final HashMap<JApiChangeStatus, ArrayList<JApiClass>> map;

    private final Function<String, String> addHeader = s -> String.format("### %s", s);
    private final Function<String, String> addUnorderedList = s -> String.format("- %s", s);
    private final Function<JApiClass, String> classFormatter = apiClass -> new StringBuffer()
            .append("name : ")
            .append(apiClass.getFullyQualifiedName())
            .append(" - ")
            .append("changes : ")
            .append(apiClass.getCompatibilityChanges().isEmpty() ? " Nothing" : apiClass.getCompatibilityChanges().stream().map(Enum::toString).collect(Collectors.joining(" | ")))
            .toString();
    private final Function<String, String> addHighLight = s -> String.format("> %s", s);

    public MarkdownReportGenerator() throws IOException {
        reportFile = Paths.get("report.md");
        map = new HashMap<>();
    }

    @Override
    public void record(JApiChangeStatus status, JApiClass apiClass) {
        map.compute(status, (jApiChangeStatus, jApiClasses) -> {
            if (jApiClasses == null) {
                var a = new ArrayList<JApiClass>();
                a.add(apiClass);
                return a;
            } else {
                jApiClasses.add(apiClass);
                return jApiClasses;
            }
        });
    }

    @Override
    public void flush() {
        PrintWriter writer = null;
        try {
            if (!Files.exists(reportFile))
                com.google.common.io.Files.touch(reportFile.toFile());
            writer = new PrintWriter(reportFile.toFile());
            writer.write("");

        } catch (IOException e) {
            e.printStackTrace();
        }
        assert writer != null;

        final var finalWriter = writer;
        map.keySet().stream()
                .forEach(status -> {
                    finalWriter.println(addHeader.apply(status.name()));
                    map.get(status).stream().forEach(apiClass -> {
                        final var recordText = classFormatter.apply(apiClass);
                        if (apiClass.getAnnotations().stream().anyMatch(jApiAnnotation -> jApiAnnotation.getFullyQualifiedName().contains("OnlyModified")))
                            finalWriter.println(addUnorderedList.apply(addHighLight.apply(recordText)));
                        else
                            finalWriter.println(addUnorderedList.apply(recordText));
                    });
                });
        finalWriter.flush();
    }

}
