import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        if (localDateTime == null) {
            jsonWriter.nullValue();
        } else {
            jsonWriter.value(localDateTime.format(formatter));
        }
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(value, formatter);
    }
}