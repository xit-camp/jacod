package camp.xit.jacod.impl;

import camp.xit.jacod.CodelistMappingProvider;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes({"camp.xit.jacod.BaseEntryMapping", "camp.xit.jacod.EntryMapping", "camp.xit.jacod.EntryMappings"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class MappingAnnotationProcessor extends AbstractProcessor {

    private static final String PROVIDER_INTERFACE = CodelistMappingProvider.class.getCanonicalName();
    private static final String SERVICES_PATH = "META-INF/services/" + PROVIDER_INTERFACE;
    private static final String PROVIDER_CLASS = "_" + CodelistMappingProvider.class.getSimpleName() + "Impl";

    private Messager messager;
    private Filer filer;


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver() && !annotations.isEmpty()) {
            try {
                int count = 0;
                Map<String, Set<TypeElement>> mapperClasses = new HashMap<>();
                for (TypeElement annotation : annotations) {
                    Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
                    for (Element el : annotatedElements) {
                        if (el.getKind() != ElementKind.CLASS && el.getKind() != ElementKind.INTERFACE) {
                            throw new ProcessingException(el, "%s: Only classes or interfaces can be annotated with @%s",
                                    el, annotation.getSimpleName());
                        }
                        TypeElement typeEl = (TypeElement) el;

                        PackageElement pkgEl = processingEnv.getElementUtils().getPackageOf(el);
                        String pkg = pkgEl.getQualifiedName().toString();
                        Set<TypeElement> pkgEls = mapperClasses.get(pkg);
                        if (pkgEls == null) {
                            pkgEls = new HashSet<>();
                            mapperClasses.put(pkg, pkgEls);
                        }
                        pkgEls.add(typeEl);
                        count++;
                    }
                }
                printMsg("Found " + count + " classes with codelist mapper annotation.");

                List<String> providers = mapperClasses.entrySet().stream()
                        .map(e -> generateProvider(filer, e.getKey(), e.getValue()))
                        .collect(toList());
                writeServices(filer, providers);
            } catch (ProcessingException e) {
                printError(null, e.getMessage());
            }
        }
        return true;
    }


    private void writeServices(Filer filer, List<String> providers) {
        try {
            final FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, "", SERVICES_PATH);
            try (Writer writer = fo.openWriter()) {
                for (String provider : providers) {
                    writer.append(provider).append("\n");
                };
            }
        } catch (IOException e) {
            printError(null, e.getMessage());
        }
    }


    private String generateProvider(Filer filer, String pkgName, Set<TypeElement> codelists) {
        String providerClassName = pkgName + "." + PROVIDER_CLASS;
        try {
            JavaFileObject obj = filer.createSourceFile(providerClassName);
            try (JavaWriter jw = new JavaWriter(obj.openWriter())) {
                jw.emitPackage(pkgName);
                jw.emitEmptyLine();
                jw.emitImports(Collection.class, Set.class);
                jw.emitEmptyLine();
                jw.beginType(PROVIDER_CLASS, "class", EnumSet.of(Modifier.PUBLIC), null, PROVIDER_INTERFACE);
                jw.emitEmptyLine();
                jw.emitAnnotation(Override.class);
                jw.beginMethod("Collection<Class<?>>", "getMapperClasses", EnumSet.of(Modifier.PUBLIC));
                jw.emitEmptyLine();
                String codelistClasses = codelists.stream().map(t -> t.getSimpleName() + ".class").collect(joining(",\n"));
                jw.emitStatement("return Set.of(\n%s\n)", codelistClasses);
                jw.endMethod();
                jw.endType();
            }
        } catch (IOException e) {
            printError(null, e.getMessage());
        }
        return providerClassName;
    }


    private void printMsg(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }


    @Override
    public void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        this.filer = processingEnvironment.getFiler();
        this.messager = processingEnvironment.getMessager();
    }


    /**
     * Prints an printError message
     *
     * @param e The element which has caused the printError. Can be null
     * @param msg The printError message
     */
    public void printError(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
}
