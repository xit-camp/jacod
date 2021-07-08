package camp.xit.jacod.impl;

import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes({"camp.xit.jacod.BaseEntryMapping", "camp.xit.jacod.EntryMapping", "camp.xit.jacod.EntryMappings"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class EntryAnnotationProcessor extends AbstractProcessor {

    public static final String MAPPERS_FILE = "META-INF/codelist-mappers";

    private Messager messager;


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // get elements annotated with the @Setter annotation
        if (!roundEnv.processingOver()) {
            try {
                Filer filer = processingEnv.getFiler();
                FileObject obj = filer.createResource(StandardLocation.CLASS_OUTPUT, "", MAPPERS_FILE);
                try (Writer writer = obj.openWriter()) {
                    for (TypeElement annotation : annotations) {
                        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

                        for (Element el : annotatedElements) {
                            writer.append(el.toString()).append("\n");
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Cannot process annotations", e);
            }
        }
        return true;
    }


    private void printError(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }


    @Override
    public void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        // get messager for printing errors
        messager = processingEnvironment.getMessager();
    }
}
