package de.presti.ree6.backend.utils.data.container;

import com.google.gson.JsonArray;
import de.presti.ree6.sql.entities.Recording;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.tomcat.util.codec.binary.Base64;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordContainer {

    String data;
    JsonArray participants;

    public RecordContainer(Recording recording) {
        this(Base64.encodeBase64String(recording.getRecording()), recording.getJsonArray());
    }
}
