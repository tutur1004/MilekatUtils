package fr.milekat.utils.storage.adapter.elasticsearch.mappers.milekat.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import fr.milekat.utils.DateMileKat;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;


public class DateDeserializer extends StdDeserializer<Date> {

    public DateDeserializer() {
        super(Date.class);
    }

    @Override
    public Date deserialize(@NotNull JsonParser p, DeserializationContext context) throws IOException {
        try {
            if (p.getText() == null) return null;
            return DateMileKat.getESStringDate(p.getText());
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}
