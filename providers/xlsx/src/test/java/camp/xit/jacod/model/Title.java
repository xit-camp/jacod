package camp.xit.jacod.model;

public class Title extends CodelistEntry {

    public enum Position {
        BEFORE, AFTER
    }

    private Position position;


    public Title() {
    }


    public Title(String code) {
        super(code);
    }


    public Title(CodelistEnum<Title> codeEnum) {
        super(codeEnum.toString());
    }


    public Position getPosition() {
        return position;
    }


    public void setPosition(Position position) {
        this.position = position;
    }


    @Override
    public String toString() {
        return "Title{" + toStringAttrs() + ", position=" + position + '}';
    }
}
