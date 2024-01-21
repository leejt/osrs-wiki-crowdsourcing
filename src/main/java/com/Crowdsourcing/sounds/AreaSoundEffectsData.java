/*
 * Copyright (c) 2019, Weird Gloop <admin@weirdgloop.org>
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

package com.Crowdsourcing.sounds;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AreaSoundEffectsData
{
	/**
	 * The area sound effect id
	 */
	private final int soundId;

	/**
	 * The id of the area sound effect actor source; -1 for player or >= 0 for NPC id
	 */
	private final int sourceId;

	/**
	 * The animation id of the area sound effect actor source
	 */
	private final int animationId;

	/**
	 * The x-coordinate of the area sound effect
	 */
	private final int x;

	/**
	 * The y-coordinate of the area sound effect
	 */
	private final int y;

	/**
	 * The z-coordinate of the area sound effect
	 */
	private final int z;

	/**
	 * The radius around the (x, y) in which the area sound effect is played 
	 */
	private final int range;

	/**
	 * The delay in ticks for the area sound effect to start playing
	 */
	private final int delay;
}
