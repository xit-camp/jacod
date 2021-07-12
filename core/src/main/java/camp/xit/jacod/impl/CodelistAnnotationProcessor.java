package camp.xit.jacod.impl;

import camp.xit.jacod.model.CodelistEntry;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("camp.xit.jacod.BaseEntry")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
// https://hannesdorfmann.com/annotation-processing/annotationprocessing101/
public final class CodelistAnnotationProcessor extends AbstractProcessor {

    public static final String CODELISTS_FILE = "META-INF/jacod-codelists";

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
                FileObject obj = filer.createResource(StandardLocation.CLASS_OUTPUT, "", CODELISTS_FILE);
                try (Writer writer = obj.openWriter()) {
                    for (TypeElement annotation : annotations) {
                        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
                        for (Element el : annotatedElements) {
                            if (el.getKind() != ElementKind.CLASS) {
                                throw new ProcessingException(el, "Only classes can be annotated with @%s", annotation.getSimpleName());
                            }
                            checkValidClass((TypeElement) el, annotation);
                            writer.append(el.toString()).append("\n");
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Cannot process annotations", e);
            } catch (ProcessingException e) {
                printError(null, e.getMessage());
            }
        }
        return true;
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
