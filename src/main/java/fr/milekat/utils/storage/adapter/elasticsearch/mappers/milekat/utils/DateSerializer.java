package fr.milekat.utils.storage.adapter.elasticsearch.mappers.milekat.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import fr.milekat.utils.DateMileKat;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Date;


public class DateSerializer extends StdSerializer<Date> {

    public DateSerializer() {
        super(Date.class);
    }

    @Override
    public void serialize(@Nonnull Date value, @NotNull JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        gen.assignCurrentValue(DateMileKat.getDateEs(value));
        gen.writeEndObject();
    }
}
