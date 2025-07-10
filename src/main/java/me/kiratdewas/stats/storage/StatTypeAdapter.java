package me.kiratdewas.stats.storage;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.kiratdewas.stats.stat.Stat;
import me.kiratdewas.stats.stat.StatManager;

import java.io.IOException;

public class StatTypeAdapter extends TypeAdapter<Stat> {
    @Override
    public void write(JsonWriter out, Stat value) throws IOException {
        out.value(value.getName());
    }

    @Override
    public Stat read(JsonReader in) throws IOException {
        String name = in.nextString();
        return StatManager.getInstance().getStat(name).orElse(null);
    }
}
