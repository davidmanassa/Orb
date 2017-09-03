package com.nuno1212s.rediscommunication;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.StringReader;

/**
 * Message
 */
public class Message {

    @Getter
    private String channel, reason;

    @Getter
    private JSONObject data;

    public Message(byte[] data) {
        ByteArrayDataInput dataInput = ByteStreams.newDataInput(data);
        this.channel = dataInput.readUTF();
        this.reason = dataInput.readUTF();

        try (StringReader r = new StringReader(dataInput.readUTF())) {
            this.data = (JSONObject) new JSONParser().parse(r);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

    public Message(String channel, String reason, JSONObject data) {
        this.channel = channel;
        this.reason = reason;
        this.data = data;
    }

    public byte[] toByteArray() {
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();

        dataOutput.writeUTF(channel);
        dataOutput.writeUTF(reason);
        dataOutput.writeUTF(data.toJSONString());

        return dataOutput.toByteArray();
    }


}
