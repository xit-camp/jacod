package camp.xit.jacoa.entry.parser.ast;

public class CompileException extends RuntimeException {

    public CompileException() {
    }


    public CompileException(String message) {
        super(message);
    }


    public CompileException(String message, Throwable cause) {
        super(message, cause);
    }


    public CompileException(Throwable cause) {
        super(cause);
    }
}
