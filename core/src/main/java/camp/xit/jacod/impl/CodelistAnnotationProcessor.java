package camp.xit.jacod.impl;

import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("camp.xit.jacod.BaseEntry")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
// https://hannesdorfmann.com/annotation-processing/annotationprocessing101/
public class CodelistAnnotationProcessor extends AbstractProcessor {

    public static final String CODELISTS_FILE = "META-INF/jacod-codelists";

    protected Filer filer;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // get elements annotated with the @Setter annotation
        if (!roundEnv.processingOver() && !annotations.isEmpty()) {
            try {
                FileObject obj = filer.createResource(StandardLocation.CLASS_OUTPUT, "", CODELISTS_FILE);
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
}
