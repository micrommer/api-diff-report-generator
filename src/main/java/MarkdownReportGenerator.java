import japicmp.model.JApiChangeStatus;
import japicmp.model.JApiClass;
import japicmp.model.JApiMethod;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MarkdownReportGenerator extends ReportGenerator {

    private final Path reportFile;
    private final HashMap<JApiChangeStatus, ArrayList<JApiClass>> classMap;
    private final HashMap<JApiChangeStatus, ArrayList<JApiMethod>> methodMap;

    private final Function<String, String> addHeader = s -> String.format("## %s", s);
    private final Function<String, String> addSubHeader = s -> String.format("#### %s", s);
    private final Function<String, String> addUnorderedList = s -> String.format("- %s", s);
    private final Function<JApiClass, String> classFormatter = apiClass -> new StringBuffer()
            .append("name : ")
            .append(apiClass.getFullyQualifiedName())
            .append(" - ")
            .append("changes : ")
            .append(apiClass.getCompatibilityChanges().isEmpty() ? " Nothing" : apiClass.getCompatibilityChanges().stream().map(Enum::toString).collect(Collectors.joining(" | ")))
            .toString();

    private final Function<JApiMethod, String> methodFormatter = jApiMethod -> new StringBuffer()
            .append("name : ")
            .append(jApiMethod.getName())
            .append(" - ")
            .append("changes : ")
            .append(jApiMethod.getCompatibilityChanges().isEmpty() ? " Nothing" : jApiMethod.getCompatibilityChanges().stream().map(Enum::toString).collect(Collectors.joining(" | ")))
            .toString();
    private final Function<String, String> addHighLight = s -> String.format("> %s", s);

    public MarkdownReportGenerator() throws IOException {
        reportFile = Paths.get("report.md");
        classMap = new HashMap<>();
        methodMap = new HashMap<>();
    }

    @Override
    public void record(JApiChangeStatus status, JApiClass apiClass) {
        classMap.compute(status, (jApiChangeStatus, jApiClasses) -> {
            if (jApiClasses == null) {
                var newList = new ArrayList<JApiClass>();
                newList.add(apiClass);
                return newList;
            } else {
                jApiClasses.add(apiClass);
                return jApiClasses;
            }
        });
    }

    @Override
    public void record(JApiChangeStatus status, JApiMethod jApiMethod) {
        methodMap.compute(status, (jApiChangeStatus, jApiMethods) -> {
            if (jApiMethods == null) {
                var newList = new ArrayList<JApiMethod>();
                newList.add(jApiMethod);
                return newList;
            } else {
                jApiMethods.add(jApiMethod);
                return jApiMethods;
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
        Arrays.stream(JApiChangeStatus.values()).forEach(status -> {
            finalWriter.println(addHeader.apply(status.name()));

            if (enableClassReport) {
                finalWriter.println(addSubHeader.apply("Classes"));
                classMap.get(status).forEach(apiClass -> {
                    final var recordText = classFormatter.apply(apiClass);
                    if (apiClass.getAnnotations().stream().anyMatch(jApiAnnotation -> jApiAnnotation.getFullyQualifiedName().contains("OnlyModified")))
                        finalWriter.println(addUnorderedList.apply(addHighLight.apply(recordText)));
                    else
                        finalWriter.println(addUnorderedList.apply(recordText));
                });
            }

            if (enableMethodReport){
                finalWriter.println(addSubHeader.apply("Methods"));
                methodMap.get(status).forEach(jApiMethod -> {
                    final var recordText = methodFormatter.apply(jApiMethod);
                    if (jApiMethod.getAnnotations().stream().anyMatch(jApiAnnotation -> jApiAnnotation.getFullyQualifiedName().contains("OnlyModified")))
                        finalWriter.println(addUnorderedList.apply(addHighLight.apply(recordText)));
                    else
                        finalWriter.println(addUnorderedList.apply(recordText));
                });
            }
        });

        finalWriter.flush();
    }

}
