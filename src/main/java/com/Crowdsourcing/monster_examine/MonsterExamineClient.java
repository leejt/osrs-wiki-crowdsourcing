package com.Crowdsourcing.monster_examine;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
public class MonsterExamineClient {

    private static final String SUBMIT_URL = "https://chisel.weirdgloop.org/monsterexamine/submit";
    private static final String SEEN_URL = "https://chisel.weirdgloop.org/monsterexamine/seen";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final MonsterExamine plugin;


    @Inject
    private MonsterExamineClient(MonsterExamine plugin)
    {
        this.plugin = plugin;
    }

    protected void submitToAPI(MonsterExamineData data)
    {
        Request r = new Request.Builder()
                .url(SUBMIT_URL)
                .post(RequestBody.create(JSON, RuneLiteAPI.GSON.toJson(data)))
                .build();
        RuneLiteAPI.CLIENT.newCall(r).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                log.debug("Error sending monster examine data", e);
            }

            @Override
            public void onResponse(Call call, Response response)
            {
                log.info("Successfully sent monster examine data");
                getSeenIds();
                response.close();
            }
        });
    }

    protected void getSeenIds()
    {
        Request request = new Request.Builder()
                .url(SEEN_URL)
                .build();

        RuneLiteAPI.CLIENT.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                log.debug("Error getting seen monster ids", e);
            }

            @Override
            public void onResponse(Call call, Response response)
            {
                try
                {
                    InputStream in = response.body().byteStream();
                    Set<Integer> tmp = RuneLiteAPI.GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), new TypeToken<Set<Integer>>(){}.getType());
                    if (tmp == null)
                    {
                        log.debug("Error parsing monster ids JSON");
                        response.close();
                        return;
                    }
                    plugin.setSeenIds(tmp);
                    response.close();
                }
                catch (JsonParseException ex)
                {
                    log.debug("Error parsing monster ids JSON", ex);
                    response.close();
                }
            }
        });
    }
}
