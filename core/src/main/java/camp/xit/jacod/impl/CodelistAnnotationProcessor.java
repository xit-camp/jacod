package camp.xit.jacod.impl;

import camp.xit.jacod.AdvancedCodelistProvider;
import camp.xit.jacod.model.CodelistEntry;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("camp.xit.jacod.BaseEntry")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
// https://hannesdorfmann.com/annotation-processing/annotationprocessing101/
public final class CodelistAnnotationProcessor extends AbstractProcessor {

    private static final String PROVIDER_INTERFACE = AdvancedCodelistProvider.class.getCanonicalName();
    private static final String SERVICES_PATH = "META-INF/services/" + PROVIDER_INTERFACE;
    private static final String PROVIDER_CLASS = "_" + AdvancedCodelistProvider.class.getSimpleName() + "Impl";

    protected Filer filer;
    private Messager messager;
    private Elements elementUtils;
    private Types typeUtils;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.typeUtils = processingEnv.getTypeUtils();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // get elements annotated with the @Setter annotation
        if (!roundEnv.processingOver() && !annotations.isEmpty()) {
            try {
                int count = 0;
                Map<String, Set<TypeElement>> codelists = new HashMap<>();
                for (TypeElement annotation : annotations) {
                    Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
                    for (Element el : annotatedElements) {
                        if (el.getKind() != ElementKind.CLASS) {
                            throw new ProcessingException(el, "Only classes can be annotated with @%s", annotation.getSimpleName());
                        }
                        TypeElement codelistEl = (TypeElement) el;
                        checkValidClass(codelistEl, annotation);

                        PackageElement pkgEl = elementUtils.getPackageOf(codelistEl);
                        String pkg = pkgEl.getQualifiedName().toString();
                        Set<TypeElement> pkgEls = codelists.get(pkg);
                        if (pkgEls == null) {
                            pkgEls = new HashSet<>();
                            codelists.put(pkg, pkgEls);
                        }
                        pkgEls.add(codelistEl);
                        count++;
                    }
                }
                printMsg("Found " + count + " advanced codelists.");

                List<String> providers = codelists.entrySet().stream()
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
                jw.emitImports(Collection.class, Set.class, CodelistEntry.class);
                jw.emitImports(pkgName + ".*");
                jw.emitEmptyLine();
                jw.beginType(PROVIDER_CLASS, "class", EnumSet.of(Modifier.PUBLIC), null, PROVIDER_INTERFACE);
                jw.emitEmptyLine();
                jw.emitAnnotation(Override.class);
                jw.beginMethod("Collection<Class<? extends CodelistEntry>>", "getAdvancedCodelists", EnumSet.of(Modifier.PUBLIC));
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


    /**
     * Checks if the annotated element observes our rules
     */
    private void checkValidClass(TypeElement codelistType, TypeElement annotation) throws ProcessingException {

        if (!codelistType.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ProcessingException(codelistType, "The class %s is not public.",
                    codelistType.getQualifiedName().toString());
        }

        // Check if it's an abstract class
        if (codelistType.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new ProcessingException(codelistType,
                    "The class %s is abstract. You can't annotate abstract classes with @%",
                    codelistType.getQualifiedName().toString(), annotation.getSimpleName());
        }

        // Check inheritance: Class must be childclass as specified in @Factory.type();
        TypeElement superClassElement = elementUtils.getTypeElement(codelistType.getClass().getCanonicalName());
        if (superClassElement.getKind() != ElementKind.CLASS && superClassElement.asType() != null) {
            // Check interface implemented
            if (!codelistType.getInterfaces().contains(superClassElement.asType())) {
                throw new ProcessingException(codelistType,
                        "The class %s annotated with @%s must extends the class %s",
                        codelistType.getQualifiedName().toString(), annotation.getSimpleName(),
                        CodelistEntry.class.getSimpleName());
            }
        } else {
            // Check subclassing
            TypeElement currentClass = codelistType;
            while (true) {
                TypeMirror superClassType = currentClass.getSuperclass();

                if (superClassType.getKind() == TypeKind.NONE) {
                    // Basis class (java.lang.Object) reached, so exit
                    throw new ProcessingException(codelistType,
                            "The class %s annotated with @%s must inherit from %s",
                            codelistType.getQualifiedName().toString(), annotation,
                            codelistType.getSimpleName());
                }

                if (superClassType.toString().equals(CodelistEntry.class.getName())) {
                    // Required super class found
                    break;
                }

                // Moving up in inheritance tree
                currentClass = (TypeElement) typeUtils.asElement(superClassType);
            }
        }

        // Check if an empty public constructor is given
        for (Element enclosed : codelistType.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                List<? extends VariableElement> parameters = constructorElement.getParameters();
                if (parameters.size() == 0/* && constructorElement.getModifiers()
                          .contains(Modifier.PUBLIC)*/) {
                    // Found an empty constructor
                    return;
                } else if (parameters.size() == 1
                        && parameters.get(0).asType().toString().equals(String.class.getName())) {
                    return;
                }

            }
        }

        // No empty constructor found
        throw new ProcessingException(codelistType,
                "The class %s must provide an empty default constructor or constructor with 1 string code parameter",
                codelistType.getQualifiedName().toString());
    }


    private void printMsg(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
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
