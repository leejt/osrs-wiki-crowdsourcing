package com.Crowdsourcing.scenery;

import com.google.common.collect.ImmutableSet;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.client.eventbus.Subscribe;
import com.Crowdsourcing.CrowdsourcingManager;

@Slf4j
public class CrowdsourcingScenery
{
	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	private GameState gameState;

	// List of objects we don't care about, even if they are loaded later. Mostly POH.
	private final ImmutableSet<Integer> blacklist = ImmutableSet.of(
		4515, 4516, 4517, 4518, 4519, 4520, 4521, 4523, 4524, 4525, 4528, 4529, 5127, 5140, 5141, 5142, 5144, 5145, 5146, 5147, 5148, 5149, 5150, 5151, 5632, 5907, 6745, 6746, 6747, 6750, 6751, 6752, 6754, 6756, 6759, 6760, 6761, 6765, 6766, 6767, 6768, 6769, 6770, 6777, 6778, 6779, 6782, 6786, 6787, 6788, 6789, 6790, 6792, 6795, 6798, 6799, 6802, 6805, 6806, 6807, 13005, 13010, 13116, 13117, 13137, 13138, 13139, 13140, 13149, 13153, 13154, 13160, 13161, 13167, 13168, 13171, 13175, 13178, 13197, 13213, 13214, 13216, 13240, 13272, 13282, 13291, 13295, 13299, 13302, 13306, 13307, 13308, 13309, 13311, 13312, 13330, 13337, 13338, 13339, 13340, 13342, 13348, 13349, 13365, 13381, 13382, 13389, 13394, 13397, 13398, 13402, 13404, 13490, 13497, 13501, 13502, 13505, 13511, 13516, 13521, 13523, 13526, 13531, 13536, 13542, 13549, 13550, 13551, 13556, 13557, 13558, 13561, 13563, 13566, 13567, 13571, 13573, 13574, 13576, 13578, 13579, 13615, 13639, 13640, 13647, 13648, 13654, 13655, 13658, 13661, 13663, 13670, 13673, 13677, 13687, 13689, 13696, 13723, 13725, 13732, 13795, 13827, 13830, 15256, 15257, 15259, 15260, 15261, 15262, 15263, 15264, 15265, 15266, 15267, 15268, 15269, 15270, 15271, 15273, 15274, 15275, 15276, 15277, 15278, 15279, 15280, 15281, 15282, 15283, 15284, 15285, 15286, 15287, 15288, 15289, 15290, 15291, 15292, 15293, 15294, 15295, 15296, 15297, 15298, 15299, 15300, 15301, 15302, 15303, 15304, 15309, 15310, 15315, 15316, 15317, 15323, 15324, 15325, 15326, 15327, 15328, 15329, 15330, 15331, 15336, 15337, 15342, 15343, 15344, 15345, 15346, 15347, 15348, 15349, 15350, 15351, 15352, 15353, 15354, 15355, 15356, 15361, 15362, 15363, 15364, 15365, 15366, 15367, 15368, 15369, 15370, 15371, 15372, 15373, 15374, 15375, 15376, 15377, 15378, 15379, 15380, 15382, 15383, 15384, 15385, 15386, 15387, 15388, 15389, 15390, 15391, 15392, 15393, 15394, 15395, 15396, 15397, 15398, 15399, 15400, 15401, 15402, 15403, 15404, 15405, 15406, 15407, 15408, 15409, 15420, 15421, 15422, 15423, 15424, 15425, 15426, 15427, 15429, 15434, 15435, 15436, 15437, 15438, 15439, 15440, 15441, 15442, 15443, 15444, 15445, 15446, 15447, 15448, 15450, 18768, 18776, 18782, 18792, 18802, 18808, 18810, 18811, 18812, 18813, 18814, 18815, 19038, 26231, 28859, 29119, 29120, 29121, 29122, 29123, 29124, 29125, 29126, 29127, 29128, 29129, 29130, 29131, 29132, 29133, 29136, 29137, 29138, 29139, 29140, 29141, 29142, 29143, 29144, 29145, 29146, 29147, 29150, 29151, 29152, 29156, 29157, 29184, 29226, 29227, 29228, 29230, 29241, 29254, 29255, 29256, 29257, 29258, 29259, 29260, 29261, 29267, 29268, 29269, 29270, 29271, 29274, 29275, 29276, 29277, 29335, 29338, 29342, 29355, 29358, 29361, 31554, 33346, 33347, 33349, 33350, 33351, 33352, 33353, 33409, 33410, 33419, 33420, 37349, 37620, 40383, 40384, 40385, 40768, 40769, 40770, 40771, 40772, 40773, 40776, 40778, 40848, 40851, 40862, 40863, 26851, 26845, 26867, 26868, 26857, 26860, 26839, 26296, 26299, 26833, 26861, 26863, 26865, 26866, 26869, 26872, 29246, 29262, 29244, 29264, 29263, 29243, 29247, 29242, 29245, 13515, 31986, 33417, 33402, 33413, 13731, 10816, 13672, 15431, 13684, 15306, 15305, 13676, 13018, 13017, 26855, 26849, 26843, 26831, 26837, 26864, 6804, 5130, 5139, 5136, 4537, 5133, 13695, 13011, 29252, 29250, 29249, 29253, 29248, 40846, 29251, 40779, 29231, 29234, 29236, 31983, 37629, 29153, 13733, 40880, 40917, 40871, 40878, 31977, 13491, 13722, 13508, 13141, 13144, 13142, 13383, 13143, 18769, 18794, 13390, 13664, 13520, 13130, 13129, 13132, 13126, 13128, 13127, 13131, 13375, 2715, 13372, 29337, 13341, 13503, 13737, 13289, 13322, 13323, 13769, 13499, 27083, 6780, 13170, 13159, 27082, 27069, 27068, 6762, 6763, 6758, 6764, 40774, 5138, 13817, 13298, 37621, 13572, 18770, 18796, 18803, 18809, 29229, 37630, 29211, 31689, 29272, 29273, 13753, 13668, 27071, 27072, 33412, 27070, 40904, 13215, 40915, 37592, 29341, 33433, 13653, 26852, 26846, 26840, 26834, 26871, 13527, 13514, 13510, 6728, 5152, 6729, 6727, 5135, 13385, 13392, 27083, 13633, 27082, 29272, 29273, 29232, 29360, 29356, 29357, 29359, 33181, 13634, 29354, 13762, 13669, 27072, 29229, 29160, 13527, 13503, 6776, 27074, 13281, 13794, 13487, 13305, 6784, 13298, 13524, 27068, 27069, 13148, 18800, 13369, 13721, 13325, 13373, 13326, 13368, 34833, 13625, 13627, 13624, 13264, 13522, 13674, 13671, 13685, 33396, 26853, 26847, 26841, 26835, 15432, 26185
	);
	private void addObjectThisTick(SceneryEventType type, WorldPoint baseLocation, int id)
	{
		if (gameState != GameState.LOGGED_IN || manager.size() > 10000 || blacklist.contains(id))
		{
			// Ignore any objects sent when game state is LOADING. This should ignore all spawns that happen
			// while reading the maps index.
			// If for whatever reason this is an ineffective check, don't let it grow unbounded...
			return;
		}
		LocalPoint local = LocalPoint.fromWorld(client, baseLocation);
		WorldPoint location = WorldPoint.fromLocalInstance(client, local);
		manager.storeEvent(new SceneryEvent(type, location, id));
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		addObjectThisTick(
			SceneryEventType.DECORATIVE_OBJECT_SPAWNED,
			event.getTile().getWorldLocation(),
			event.getDecorativeObject().getId()
		);
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{
		addObjectThisTick(
			SceneryEventType.WALL_OBJECT_SPAWNED,
			event.getTile().getWorldLocation(),
			event.getWallObject().getId()
		);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		addObjectThisTick(
			SceneryEventType.GAME_OBJECT_SPAWNED,
			event.getTile().getWorldLocation(),
			event.getGameObject().getId()
		);
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		addObjectThisTick(
			SceneryEventType.GROUND_OBJECT_SPAWNED,
			event.getTile().getWorldLocation(),
			event.getGroundObject().getId()
		);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		gameState = event.getGameState();
	}
}