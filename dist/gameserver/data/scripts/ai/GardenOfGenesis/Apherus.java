/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.GardenOfGenesis;

import java.util.HashMap;
import java.util.Map;

import lineage2.commons.threading.RunnableImpl;
import lineage2.commons.util.Rnd;
import lineage2.gameserver.ThreadPoolManager;
import lineage2.gameserver.ai.Fighter;
import lineage2.gameserver.model.Creature;
import lineage2.gameserver.model.Skill;
import lineage2.gameserver.model.instances.DoorInstance;
import lineage2.gameserver.model.instances.NpcInstance;
import lineage2.gameserver.utils.Location;
import lineage2.gameserver.utils.NpcUtils;
import lineage2.gameserver.utils.ReflectionUtils;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author KilRoy
 */
public final class Apherus extends Fighter
{
	final Skill GroundStrike;
	final Skill JumpAttack;
	static final int[] DOORS =
	{
		26210041,
		26210042,
		26210043,
		26210044
	};
	private static final int APHERUS_SUBORDINATE = 25865;
	static boolean lock60 = false;
	static boolean lock40 = false;
	static boolean lock20 = false;
	
	public Apherus(NpcInstance actor)
	{
		super(actor);
		TIntObjectHashMap<Skill> skills = getActor().getTemplate().getSkills();
		GroundStrike = skills.get(5227);
		JumpAttack = skills.get(5228);
	}
	
	@Override
	protected boolean createNewTask()
	{
		clearTasks();
		Creature target;
		
		if ((target = prepareTarget()) == null)
		{
			return false;
		}
		
		NpcInstance actor = getActor();
		
		if (actor.isDead())
		{
			return false;
		}
		
		double distance = actor.getDistance(target);
		double actor_hp_precent = actor.getCurrentHpPercents();
		
		if ((actor_hp_precent <= 80) && (actor_hp_precent > 60) && !lock60)
		{
			lock60 = true;
			ThreadPoolManager.getInstance().schedule(new runSpawnTask60(2, target), 30000L);
		}
		
		if ((actor_hp_precent <= 60) && (actor_hp_precent > 40) && !lock40)
		{
			lock40 = true;
			ThreadPoolManager.getInstance().schedule(new runSpawnTask40(3, target), 20000L);
		}
		
		if ((actor_hp_precent <= 40) && (actor_hp_precent > 20) && !lock20)
		{
			lock20 = true;
			ThreadPoolManager.getInstance().schedule(new runSpawnTask20(4, target), 20000L);
		}
		
		Map<Skill, Integer> skills = new HashMap<>();
		addDesiredSkill(skills, target, distance, GroundStrike);
		addDesiredSkill(skills, target, distance, JumpAttack);
		Skill skill = selectTopSkill(skills);
		
		if ((skill != null) && !skill.isOffensive())
		{
			target = actor;
		}
		
		return chooseTaskAndTargets(skill, target, distance);
	}
	
	void spawnMinions(int count, Creature target)
	{
		NpcInstance actor = getActor();
		
		for (int i = 0; i < count; i++)
		{
			NpcInstance subordinate = NpcUtils.spawnSingle(APHERUS_SUBORDINATE, new Location(actor.getX() - Rnd.get(100), actor.getY() - Rnd.get(100), actor.getZ(), 0));
			subordinate.getAggroList().addDamageHate(target, 0, 10000);
			subordinate.setAggressionTarget(target);
		}
	}
	
	private class runSpawnTask60 extends RunnableImpl
	{
		private final int _count;
		private final Creature _target;
		
		runSpawnTask60(int count, Creature target)
		{
			_count = count;
			_target = target;
		}
		
		@Override
		public void runImpl()
		{
			lock60 = false;
			spawnMinions(_count, _target);
		}
	}
	
	private class runSpawnTask40 extends RunnableImpl
	{
		private final int _count;
		private final Creature _target;
		
		runSpawnTask40(int count, Creature target)
		{
			_count = count;
			_target = target;
		}
		
		@Override
		public void runImpl()
		{
			lock40 = false;
			spawnMinions(_count, _target);
		}
	}
	
	private class runSpawnTask20 extends RunnableImpl
	{
		private final int _count;
		private final Creature _target;
		
		runSpawnTask20(int count, Creature target)
		{
			_count = count;
			_target = target;
		}
		
		@Override
		public void runImpl()
		{
			lock20 = false;
			spawnMinions(_count, _target);
		}
	}
	
	private class runDoorOpener extends RunnableImpl
	{
		public runDoorOpener()
		{
		}
		
		@Override
		public void runImpl()
		{
			for (int doorID : DOORS)
			{
				DoorInstance door = ReflectionUtils.getDoor(doorID);
				door.closeMe();
			}
		}
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		ThreadPoolManager.getInstance().schedule(new runDoorOpener(), 120000L);
		super.onEvtDead(killer);
	}
}