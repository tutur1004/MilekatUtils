package fr.milekat.utils.messaging;

public class MessagingChanel {
    private final String NAME;
    private final String TYPE;

    public MessagingChanel(String name, String type) {
        this.NAME = name;
        this.TYPE = type;
    }

    public String getName() {
        return NAME;
    }

    public String getType() {
        return TYPE;
    }
}
