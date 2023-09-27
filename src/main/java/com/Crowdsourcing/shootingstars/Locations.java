/*
 * Copyright (c) 2023, Ron Young <https://github.com/raiyni>
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
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

package com.Crowdsourcing.shootingstars;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import java.util.List;
import net.runelite.api.Point;

public class Locations
{
	private static final ListMultimap<Integer, Point> POINTS = ArrayListMultimap.create();

	static
	{
		List<Point> points = Lists.newArrayList(
			new Point(1210, 3651),
			new Point(1258, 3564),
			new Point(1279, 3817),
			new Point(1322, 3816),
			new Point(1437, 3840),
			new Point(1534, 3747),
			new Point(1597, 3648),
			new Point(1760, 3853),
			new Point(1769, 3709),
			new Point(1778, 3493),
			new Point(2139, 3938),
			new Point(2173, 3409),
			new Point(2200, 2792),
			new Point(2269, 3158),
			new Point(2318, 3269),
			new Point(2329, 3163),
			new Point(2341, 3635),
			new Point(2375, 3832),
			new Point(2393, 3814),
			new Point(2444, 3490),
			new Point(2448, 3436),
			new Point(2468, 2842),
			new Point(2483, 2886),
			new Point(2528, 3887),
			new Point(2567, 2858),
			new Point(2571, 2964),
			new Point(2589, 3478),
			new Point(2602, 3086),
			new Point(2608, 3233),
			new Point(2624, 3141),
			new Point(2630, 2993),
			new Point(2683, 3699),
			new Point(2705, 3333),
			new Point(2727, 3683),
			new Point(2736, 3221),
			new Point(2742, 3143),
			new Point(2804, 3434),
			new Point(2822, 3238),
			new Point(2827, 2999),
			new Point(2835, 3296),
			new Point(2845, 3037),
			new Point(2882, 3474),
			new Point(2906, 3355),
			new Point(2940, 3280),
			new Point(2974, 3241),
			new Point(3018, 3443),
			new Point(3018, 3593),
			new Point(3030, 3348),
			new Point(3049, 3940),
			new Point(3057, 3887),
			new Point(3091, 3962),
			new Point(3093, 3756),
			new Point(3094, 3235),
			new Point(3108, 3569),
			new Point(3153, 3150),
			new Point(3171, 2910),
			new Point(3175, 3362),
			new Point(3188, 3932),
			new Point(3230, 3155),
			new Point(3258, 3408),
			new Point(3274, 6055),
			new Point(3276, 3164),
			new Point(3290, 3353),
			new Point(3296, 3298),
			new Point(3316, 2867),
			new Point(3351, 3281),
			new Point(3424, 3160),
			new Point(3434, 2889),
			new Point(3451, 3233),
			new Point(3500, 3219),
			new Point(3505, 3485),
			new Point(3635, 3340),
			new Point(3650, 3214),
			new Point(3686, 2969),
			new Point(3774, 3814),
			new Point(3818, 3801)
		);

		for (Point p : points)
		{
			POINTS.put(((p.getX() >> 6) << 8) | (p.getY() >> 6), p);
		}
	}

	public static List<Point> get(int regionId)
	{
		return POINTS.get(regionId);
	}

	public static boolean containsKey(int regionId)
	{
		return POINTS.containsKey(regionId);
	}
}
