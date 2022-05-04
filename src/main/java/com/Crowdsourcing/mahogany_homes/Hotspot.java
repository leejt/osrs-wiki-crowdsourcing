/*
 * Copyright (c) 2020, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
package com.Crowdsourcing.mahogany_homes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
enum Hotspot
{
	MAHOGANY_HOMES_HOTSPOT_1(10554,
		ImmutableSet.of(39981, 39989, 39997, 40002, 40007, 40011, 40083, 40156, 40164, 40171, 40296, 40297)),
	MAHOGANY_HOMES_HOTSPOT_2(10555,
		ImmutableSet.of(39982, 39990, 39998, 40008, 40084, 40089, 40095, 40157, 40165, 40172, 40287, 40293)),
	MAHOGANY_HOMES_HOTSPOT_3(10556,
		ImmutableSet.of(39983, 39991, 39999, 40003, 40012, 40085, 40090, 40096, 40158, 40166, 40173, 40290)),
	MAHOGANY_HOMES_HOTSPOT_4(10557,
		ImmutableSet.of(39984, 39992, 40000, 40086, 40091, 40097, 40159, 40167, 40174, 40288, 40291, 40294)),
	MAHOGANY_HOMES_HOTSPOT_5(10558,
		ImmutableSet.of(39985, 39993, 40004, 40009, 40013, 40087, 40092, 40160, 40168, 40175, 40286, 40298)),
	MAHOGANY_HOMES_HOTSPOT_6(10559,
		ImmutableSet.of(39986, 39994, 40001, 40005, 40010, 40014, 40088, 40093, 40098, 40161, 40169, 40176)),
	MAHOGANY_HOMES_HOTSPOT_7(10560,
		ImmutableSet.of(39987, 39995, 40006, 40015, 40094, 40099, 40162, 40170, 40177, 40292, 40295)),
	MAHOGANY_HOMES_HOTSPOT_8(10561,
		ImmutableSet.of(39988, 39996, 40163, 40289, 40299)),
	;

	private final int varb;
	private final ImmutableSet<Integer> objectIds;

	private static final ImmutableMap<Integer, Hotspot> HOTSPOT_BY_OBJECT_ID;
	static
	{
		final ImmutableMap.Builder<Integer, Hotspot> objects = new ImmutableMap.Builder<>();
		for (final Hotspot hotspot : values())
		{
			hotspot.getObjectIds().forEach(id -> objects.put(id, hotspot));
		}
		HOTSPOT_BY_OBJECT_ID = objects.build();
	}

	@Nullable
	static Hotspot getByObjectId(final int objectId)
	{
		return HOTSPOT_BY_OBJECT_ID.get(objectId);
	}

	static boolean isHotspotObject(final int id)
	{
		return HOTSPOT_BY_OBJECT_ID.containsKey(id);
	}
}