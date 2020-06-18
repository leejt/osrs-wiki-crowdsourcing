/*
 * Copyright (c) 2018, Weird Gloop <admin@weirdgloop.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.Crowdsourcing;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Singleton
public class CrowdsourcingManager
{

	private static final String CROWDSOURCING_BASE = "https://crowdsource.runescape.wiki/runelite";
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final Gson GSON = new Gson();
	private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
		.pingInterval(30, TimeUnit.SECONDS)
		.build();

	private List<Object> data = new ArrayList<>();

	public void storeEvent(Object event)
	{
		data.add(event);
	}

	protected void submitToAPI()
	{
		if (data.isEmpty())
		{
			return;
		}

		Request r = new Request.Builder()
			.url(CROWDSOURCING_BASE)
			.post(RequestBody.create(JSON, GSON.toJson(this.data)))
			.build();

		data.clear();

		try (Response response = CLIENT.newCall(r).execute())
		{
			if (response.isSuccessful())
			{
				log.info("Successfully sent crowdsourcing data");
			}
			else
			{
				log.debug("Error sending crowdsourcing data");
				log.debug(response.body().toString());
			}
		}
		catch (IOException e)
		{
			log.debug("IOException: {}", e.getMessage());
		}
	}
}
