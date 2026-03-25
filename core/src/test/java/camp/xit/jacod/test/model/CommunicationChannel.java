package camp.xit.jacod.test.model;

import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.model.CodelistEnum;

public class CommunicationChannel extends CodelistEntry {

    public enum Types implements CodelistEnum<CommunicationChannel> {
        /**
         * Email
         */
        EMAIL,
        /**
         * SMS
         */
        SMS,
        /**
         * Pošta
         */
        LETTER,
        /**
         * Telefónny hovor
         */
        CALL,
        /**
         * Námietka proti profilovaniu osobných údajov. Znemožňuje predvypĺňanie údajov.
         */
        PROFILE_OBJECTION
    }


    private CommunicationChannel() {
    }


    public CommunicationChannel(Types code) {
        super(code.toString());
    }
}
