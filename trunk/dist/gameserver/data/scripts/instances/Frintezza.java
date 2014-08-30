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
package instances;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import lineage2.commons.threading.RunnableImpl;
import lineage2.commons.util.Rnd;
import lineage2.gameserver.ThreadPoolManager;
import lineage2.gameserver.ai.CtrlEvent;
import lineage2.gameserver.listener.actor.OnCurrentHpDamageListener;
import lineage2.gameserver.listener.actor.OnDeathListener;
import lineage2.gameserver.listener.zone.OnZoneEnterLeaveListener;
import lineage2.gameserver.model.Creature;
import lineage2.gameserver.model.Player;
import lineage2.gameserver.model.Skill;
import lineage2.gameserver.model.Zone;
import lineage2.gameserver.model.entity.Reflection;
import lineage2.gameserver.model.instances.NpcInstance;
import lineage2.gameserver.network.serverpackets.Earthquake;
import lineage2.gameserver.network.serverpackets.ExShowScreenMessage;
import lineage2.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import lineage2.gameserver.network.serverpackets.MagicSkillCanceled;
import lineage2.gameserver.network.serverpackets.MagicSkillUse;
import lineage2.gameserver.network.serverpackets.SocialAction;
import lineage2.gameserver.network.serverpackets.SystemMessage;
import lineage2.gameserver.network.serverpackets.components.NpcString;
import lineage2.gameserver.tables.SkillTable;
import lineage2.gameserver.utils.Location;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author pchayka
 *         <p/>
 */
public final class Frintezza extends Reflection
{
	private static final int HallAlarmDevice = 18328;
	private static final int DarkChoirPlayer = 18339;
	private static final int _weakScarletId = 29046;
	private static final int _strongScarletId = 29047;
	private static final int TeleportCube = 29061;
	private static final int _frintezzasSwordId = 7903;
	private static final int DewdropItem = 8556;
	static final int[] hallADoors =
	{
		17130051,
		17130052,
		17130053,
		17130054,
		17130055,
		17130056,
		17130057,
		17130058
	};
	static final int[] corridorADoors =
	{
		17130042,
		17130043
	};
	static final int[] hallBDoors =
	{
		17130061,
		17130062,
		17130063,
		17130064,
		17130065,
		17130066,
		17130067,
		17130068,
		17130069,
		17130070
	};
	static final int[] corridorBDoors =
	{
		17130045,
		17130046
	};
	static final int[] blockANpcs =
	{
		18329,
		18330,
		18331,
		18333
	};
	static final int[] blockBNpcs =
	{
		18334,
		18335,
		18336,
		18337,
		18338
	};
	static final int _intervalOfFrintezzaSongs = 30000;
	
	public static class NpcLocation extends Location
	{
		public int npcId;
		
		public NpcLocation()
		{
		}
		
		public NpcLocation(int x, int y, int z, int heading, int npcId)
		{
			super(x, y, z, heading);
			this.npcId = npcId;
		}
	}
	
	// The Boss
	static final NpcLocation frintezzaSpawn = new NpcLocation(-87784, -155090, -9080, 16048, 29045);
	// Weak Scarlet Van Halisha.
	static final NpcLocation scarletSpawnWeak = new NpcLocation(-87784, -153288, -9176, 16384, 29046);
	// Portrait spawns - 4 portraits = 4 spawns
	static final NpcLocation[] portraitSpawns =
	{
		new NpcLocation(-86136, -153960, -9168, 35048, 29048),
		new NpcLocation(-86184, -152456, -9168, 28205, 29049),
		new NpcLocation(-89368, -152456, -9168, 64817, 29048),
		new NpcLocation(-89416, -153976, -9168, 57730, 29049)
	};
	// Demon spawns - 4 portraits = 4 demons
	static final NpcLocation[] demonSpawns =
	{
		new NpcLocation(-86136, -153960, -9168, 35048, 29050),
		new NpcLocation(-86184, -152456, -9168, 28205, 29051),
		new NpcLocation(-89368, -152456, -9168, 64817, 29051),
		new NpcLocation(-89416, -153976, -9168, 57730, 29050)
	};
	NpcInstance _frintezzaDummy;
	NpcInstance frintezza;
	NpcInstance weakScarlet;
	NpcInstance strongScarlet;
	final NpcInstance[] portraits = new NpcInstance[4];
	final NpcInstance[] demons = new NpcInstance[4];
	int _scarletMorph = 0;
	private static final long battleStartDelay = 5 * 60000L; // 5min
	final DeathListener _deathListener = new DeathListener();
	final CurrentHpListener _currentHpListener = new CurrentHpListener();
	private final ZoneListener _zoneListener = new ZoneListener();
	ScheduledFuture<?> musicTask;
	
	@Override
	protected void onCreate()
	{
		super.onCreate();
		getZone("[Frintezza]").addListener(_zoneListener);
		
		for (NpcInstance n : getNpcs())
		{
			n.addListener(_deathListener);
		}
		
		blockUnblockNpcs(true, blockANpcs);
	}
	
	private class FrintezzaStart extends RunnableImpl
	{
		public FrintezzaStart()
		{
		}
		
		@Override
		public void runImpl()
		{
			ThreadPoolManager.getInstance().schedule(new Spawn(1), 1000);
		}
	}
	
	private class Spawn extends RunnableImpl
	{
		private int _taskId = 0;
		
		public Spawn(int taskId)
		{
			_taskId = taskId;
		}
		
		@Override
		public void runImpl()
		{
			try
			{
				switch (_taskId)
				{
					case 1: // spawn.
						_frintezzaDummy = spawn(new NpcLocation(-87784, -155096, -9080, 16048, 29059));
						ThreadPoolManager.getInstance().schedule(new Spawn(2), 1000);
						break;
					
					case 2:
						closeDoor(corridorBDoors[1]);
						frintezza = spawn(frintezzaSpawn);
						showSocialActionMovie(frintezza, 500, 90, 0, 6500, 8000, 0);
						
						for (int i = 0; i < 4; i++)
						{
							portraits[i] = spawn(portraitSpawns[i]);
							portraits[i].startImmobilized();
							demons[i] = spawn(demonSpawns[i]);
						}
						
						blockAll(true);
						ThreadPoolManager.getInstance().schedule(new Spawn(3), 6500);
						break;
					
					case 3:
						showSocialActionMovie(_frintezzaDummy, 1800, 90, 8, 6500, 7000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(4), 900);
						break;
					
					case 4:
						showSocialActionMovie(_frintezzaDummy, 140, 90, 10, 2500, 4500, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(5), 4000);
						break;
					
					case 5:
						showSocialActionMovie(frintezza, 40, 75, -10, 0, 1000, 0);
						showSocialActionMovie(frintezza, 40, 75, -10, 0, 12000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(6), 1350);
						break;
					
					case 6:
						frintezza.broadcastPacket(new SocialAction(frintezza.getObjectId(), 2));
						ThreadPoolManager.getInstance().schedule(new Spawn(7), 7000);
						break;
					
					case 7:
						_frintezzaDummy.deleteMe();
						_frintezzaDummy = null;
						ThreadPoolManager.getInstance().schedule(new Spawn(8), 1000);
						break;
					
					case 8:
						showSocialActionMovie(demons[0], 140, 0, 3, 22000, 3000, 1);
						ThreadPoolManager.getInstance().schedule(new Spawn(9), 2800);
						break;
					
					case 9:
						showSocialActionMovie(demons[1], 140, 0, 3, 22000, 3000, 1);
						ThreadPoolManager.getInstance().schedule(new Spawn(10), 2800);
						break;
					
					case 10:
						showSocialActionMovie(demons[2], 140, 180, 3, 22000, 3000, 1);
						ThreadPoolManager.getInstance().schedule(new Spawn(11), 2800);
						break;
					
					case 11:
						showSocialActionMovie(demons[3], 140, 180, 3, 22000, 3000, 1);
						ThreadPoolManager.getInstance().schedule(new Spawn(12), 3000);
						break;
					
					case 12:
						showSocialActionMovie(frintezza, 240, 90, 0, 0, 1000, 0);
						showSocialActionMovie(frintezza, 240, 90, 25, 5500, 10000, 3);
						ThreadPoolManager.getInstance().schedule(new Spawn(13), 3000);
						break;
					
					case 13:
						showSocialActionMovie(frintezza, 100, 195, 35, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(14), 700);
						break;
					
					case 14:
						showSocialActionMovie(frintezza, 100, 195, 35, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(15), 1300);
						break;
					
					case 15:
						showSocialActionMovie(frintezza, 120, 180, 45, 1500, 10000, 0);
						frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5006, 1, 34000, 0));
						ThreadPoolManager.getInstance().schedule(new Spawn(16), 1500);
						break;
					
					case 16:
						showSocialActionMovie(frintezza, 520, 135, 45, 8000, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(17), 7500);
						break;
					
					case 17:
						showSocialActionMovie(frintezza, 1500, 110, 25, 10000, 13000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(18), 9500);
						break;
					
					case 18:
						weakScarlet = spawn(scarletSpawnWeak);
						block(weakScarlet, true);
						weakScarlet.addListener(_currentHpListener);
						weakScarlet.broadcastPacket(new MagicSkillUse(weakScarlet, weakScarlet, 5016, 1, 3000, 0));
						Earthquake eq = new Earthquake(weakScarlet.getLoc(), 50, 6);
						
						for (Player pc : getPlayers())
						{
							pc.broadcastPacket(eq);
						}
						
						showSocialActionMovie(weakScarlet, 1000, 160, 20, 6000, 6000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(19), 5500);
						break;
					
					case 19:
						showSocialActionMovie(weakScarlet, 800, 160, 5, 1000, 10000, 2);
						ThreadPoolManager.getInstance().schedule(new Spawn(20), 2100);
						break;
					
					case 20:
						showSocialActionMovie(weakScarlet, 300, 60, 8, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(21), 2000);
						break;
					
					case 21:
						showSocialActionMovie(weakScarlet, 1000, 90, 10, 3000, 5000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(22), 3000);
						break;
					
					case 22:
						for (Player pc : getPlayers())
						{
							pc.leaveMovieMode();
						}
						
						ThreadPoolManager.getInstance().schedule(new Spawn(23), 2000);
						break;
					
					case 23:
						blockAll(false);
						spawn(new NpcLocation(-87904, -141296, -9168, 0, TeleportCube));
						_scarletMorph = 1;
						musicTask = ThreadPoolManager.getInstance().schedule(new Music(), 5000);
						break;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class Music extends RunnableImpl
	{
		public Music()
		{
		}
		
		@Override
		public void runImpl()
		{
			if (frintezza == null)
			{
				return;
			}
			
			int song = Math.max(1, Math.min(4, getSong()));
			NpcString song_name;
			
			switch (song)
			{
				case 1:
					song_name = NpcString.REQUIEM_OF_HATRED;
					break;
				
				case 2:
					song_name = NpcString.FRENETIC_TOCCATA;
					break;
				
				case 3:
					song_name = NpcString.FUGUE_OF_JUBILATION;
					break;
				
				case 4:
					song_name = NpcString.MOURNFUL_CHORALE_PRELUDE;
					break;
				
				default:
					return;
			}
			
			if (!frintezza.isBlocked())
			{
				frintezza.broadcastPacket(new ExShowScreenMessage(song_name, 3000, ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
				frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, song, _intervalOfFrintezzaSongs, 0));
				// Launch the song's effects (they start about 10 seconds after he starts to play)
				ThreadPoolManager.getInstance().schedule(new SongEffectLaunched(getSongTargets(song), song, 10000), 10000);
			}
			
			// Schedule a new song to be played in 30-40 seconds...
			musicTask = ThreadPoolManager.getInstance().schedule(new Music(), _intervalOfFrintezzaSongs + Rnd.get(10000));
		}
		
		/**
		 * Depending on the song, returns the song's targets (either mobs or players)
		 * @param songId
		 * @return
		 */
		private List<Creature> getSongTargets(int songId)
		{
			List<Creature> targets = new ArrayList<>();
			
			if (songId < 4) // Target is the minions
			{
				if ((weakScarlet != null) && !weakScarlet.isDead())
				{
					targets.add(weakScarlet);
				}
				
				if ((strongScarlet != null) && !strongScarlet.isDead())
				{
					targets.add(strongScarlet);
				}
				
				for (int i = 0; i < 4; i++)
				{
					if ((portraits[i] != null) && !portraits[i].isDead())
					{
						targets.add(portraits[i]);
					}
					
					if ((demons[i] != null) && !demons[i].isDead())
					{
						targets.add(demons[i]);
					}
				}
			}
			else
			{
				// Target is the players
				for (Player pc : getPlayers())
				{
					if (!pc.isDead())
					{
						targets.add(pc);
					}
				}
			}
			
			return targets;
		}
		
		/**
		 * returns the chosen symphony for Frintezza to play If the minions are injured he has 40% to play a healing song If they are all dead, he will only play harmful player symphonies
		 * @return
		 */
		private int getSong()
		{
			if (minionsNeedHeal())
			{
				return 1;
			}
			
			return Rnd.get(2, 4);
		}
		
		/**
		 * Checks if Frintezza's minions need heal (only major minions are checked) Return a "need heal" = true only 40% of the time
		 * @return
		 */
		private boolean minionsNeedHeal()
		{
			if (!Rnd.chance(40))
			{
				return false;
			}
			
			if ((weakScarlet != null) && !weakScarlet.isAlikeDead() && (weakScarlet.getCurrentHp() < ((weakScarlet.getMaxHp() * 2) / 3)))
			{
				return true;
			}
			
			if ((strongScarlet != null) && !strongScarlet.isAlikeDead() && (strongScarlet.getCurrentHp() < ((strongScarlet.getMaxHp() * 2) / 3)))
			{
				return true;
			}
			
			for (int i = 0; i < 4; i++)
			{
				if ((portraits[i] != null) && !portraits[i].isDead() && (portraits[i].getCurrentHp() < (portraits[i].getMaxHp() / 3)))
				{
					return true;
				}
				
				if ((demons[i] != null) && !demons[i].isDead() && (demons[i].getCurrentHp() < (demons[i].getMaxHp() / 3)))
				{
					return true;
				}
			}
			
			return false;
		}
	}
	
	/**
	 * The song was played, this class checks it's affects (if any)
	 */
	private class SongEffectLaunched extends RunnableImpl
	{
		private final List<Creature> _targets;
		private final int _song, _currentTime;
		
		/**
		 * @param targets - song's targets
		 * @param song - song id 1-5
		 * @param currentTimeOfSong - skills during music play are consecutive, repeating
		 */
		public SongEffectLaunched(List<Creature> targets, int song, int currentTimeOfSong)
		{
			_targets = targets;
			_song = song;
			_currentTime = currentTimeOfSong;
		}
		
		@Override
		public void runImpl()
		{
			if (frintezza == null)
			{
				return;
			}
			
			// If the song time is over stop this loop
			if (_currentTime > _intervalOfFrintezzaSongs)
			{
				return;
			}
			
			// Skills are consecutive, so call them again
			SongEffectLaunched songLaunched = new SongEffectLaunched(_targets, _song, _currentTime + (_intervalOfFrintezzaSongs / 10));
			ThreadPoolManager.getInstance().schedule(songLaunched, _intervalOfFrintezzaSongs / 10);
			frintezza.callSkill(SkillTable.getInstance().getInfo(5008, _song), _targets, false);
		}
	}
	
	NpcInstance spawn(NpcLocation loc)
	{
		return addSpawnWithoutRespawn(loc.npcId, loc, 0);
	}
	
	/**
	 * Shows a movie to the players in the lair.
	 * @param target - L2NpcInstance target is the center of this movie
	 * @param dist - int distance from target
	 * @param yaw - angle of movie (north = 90, south = 270, east = 0 , west = 180)
	 * @param pitch - pitch > 0 looks up / pitch < 0 looks down
	 * @param time - fast ++ or slow -- depends on the value
	 * @param duration - How long to watch the movie
	 * @param socialAction - 1,2,3,4 social actions / other values do nothing
	 */
	void showSocialActionMovie(NpcInstance target, int dist, int yaw, int pitch, int time, int duration, int socialAction)
	{
		if (target == null)
		{
			return;
		}
		
		for (Player pc : getPlayers())
		{
			if (pc.getDistance(target) <= 2550)
			{
				pc.enterMovieMode();
				pc.specialCamera(target, dist, yaw, pitch, time, duration);
			}
			else
			{
				pc.leaveMovieMode();
			}
		}
		
		if ((socialAction > 0) && (socialAction < 5))
		{
			target.broadcastPacket(new SocialAction(target.getObjectId(), socialAction));
		}
	}
	
	void blockAll(boolean flag)
	{
		block(frintezza, flag);
		block(weakScarlet, flag);
		block(strongScarlet, flag);
		
		for (int i = 0; i < 4; i++)
		{
			block(portraits[i], flag);
			block(demons[i], flag);
		}
	}
	
	void block(NpcInstance npc, boolean flag)
	{
		if ((npc == null) || npc.isDead())
		{
			return;
		}
		
		if (flag)
		{
			npc.abortAttack(true, false);
			npc.abortCast(true, true);
			npc.setTarget(null);
			
			if (npc.isMoving)
			{
				npc.stopMove();
			}
			
			npc.block();
		}
		else
		{
			npc.unblock();
		}
		
		npc.setIsInvul(flag);
	}
	
	private class SecondMorph extends RunnableImpl
	{
		private int _taskId = 0;
		
		public SecondMorph(int taskId)
		{
			_taskId = taskId;
		}
		
		@Override
		public void runImpl()
		{
			try
			{
				switch (_taskId)
				{
					case 1:
						int angle = Math.abs((weakScarlet.getHeading() < 32768 ? 180 : 540) - (int) (weakScarlet.getHeading() / 182.044444444));
						
						for (Player pc : getPlayers())
						{
							pc.enterMovieMode();
						}
						
						blockAll(true);
						showSocialActionMovie(weakScarlet, 500, angle, 5, 500, 15000, 0);
						ThreadPoolManager.getInstance().schedule(new SecondMorph(2), 2000);
						break;
					
					case 2:
						weakScarlet.broadcastPacket(new SocialAction(weakScarlet.getObjectId(), 1));
						weakScarlet.setCurrentHp((weakScarlet.getMaxHp() * 3) / 4, false);
						weakScarlet.setRHandId(_frintezzasSwordId);
						weakScarlet.broadcastCharInfo();
						ThreadPoolManager.getInstance().schedule(new SecondMorph(3), 5500);
						break;
					
					case 3:
						weakScarlet.broadcastPacket(new SocialAction(weakScarlet.getObjectId(), 4));
						blockAll(false);
						Skill skill = SkillTable.getInstance().getInfo(5017, 1);
						skill.getEffects(weakScarlet, weakScarlet, false, false);
						
						for (Player pc : getPlayers())
						{
							pc.leaveMovieMode();
						}
						
						break;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class ThirdMorph extends RunnableImpl
	{
		private int _taskId = 0;
		private int _angle = 0;
		
		public ThirdMorph(int taskId)
		{
			_taskId = taskId;
		}
		
		@Override
		public void runImpl()
		{
			try
			{
				switch (_taskId)
				{
					case 1:
						_angle = Math.abs((weakScarlet.getHeading() < 32768 ? 180 : 540) - (int) (weakScarlet.getHeading() / 182.044444444));
						
						for (Player pc : getPlayers())
						{
							pc.enterMovieMode();
						}
						
						blockAll(true);
						frintezza.broadcastPacket(new MagicSkillCanceled(frintezza.getObjectId()));
						frintezza.broadcastPacket(new SocialAction(frintezza.getObjectId(), 4));
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(2), 100);
						break;
					
					case 2:
						showSocialActionMovie(frintezza, 250, 120, 15, 0, 1000, 0);
						showSocialActionMovie(frintezza, 250, 120, 15, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(3), 6500);
						break;
					
					case 3:
						frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5006, 1, 34000, 0));
						showSocialActionMovie(frintezza, 500, 70, 15, 3000, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(4), 3000);
						break;
					
					case 4:
						showSocialActionMovie(frintezza, 2500, 90, 12, 6000, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(5), 3000);
						break;
					
					case 5:
						showSocialActionMovie(weakScarlet, 250, _angle, 12, 0, 1000, 0);
						showSocialActionMovie(weakScarlet, 250, _angle, 12, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(6), 500);
						break;
					
					case 6:
						weakScarlet.doDie(weakScarlet);
						showSocialActionMovie(weakScarlet, 450, _angle, 14, 8000, 8000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(7), 6250);
						break;
					
					case 7:
						NpcLocation loc = new NpcLocation();
						loc.set(weakScarlet.getLoc());
						loc.npcId = _strongScarletId;
						weakScarlet.deleteMe();
						weakScarlet = null;
						strongScarlet = spawn(loc);
						strongScarlet.addListener(_deathListener);
						block(strongScarlet, true);
						showSocialActionMovie(strongScarlet, 450, _angle, 12, 500, 14000, 2);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(9), 5000);
						break;
					
					case 9:
						blockAll(false);
						
						for (Player pc : getPlayers())
						{
							pc.leaveMovieMode();
						}
						
						Skill skill = SkillTable.getInstance().getInfo(5017, 1);
						skill.getEffects(strongScarlet, strongScarlet, false, false);
						break;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class Die extends RunnableImpl
	{
		private int _taskId = 0;
		
		public Die(int taskId)
		{
			_taskId = taskId;
		}
		
		@Override
		public void runImpl()
		{
			try
			{
				switch (_taskId)
				{
					case 1:
						blockAll(true);
						int _angle = Math.abs((strongScarlet.getHeading() < 32768 ? 180 : 540) - (int) (strongScarlet.getHeading() / 182.044444444));
						showSocialActionMovie(strongScarlet, 300, _angle - 180, 5, 0, 7000, 0);
						showSocialActionMovie(strongScarlet, 200, _angle, 85, 4000, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Die(2), 7500);
						break;
					
					case 2:
						showSocialActionMovie(frintezza, 100, 120, 5, 0, 7000, 0);
						showSocialActionMovie(frintezza, 100, 90, 5, 5000, 15000, 0);
						ThreadPoolManager.getInstance().schedule(new Die(3), 6000);
						break;
					
					case 3:
						showSocialActionMovie(frintezza, 900, 90, 25, 7000, 10000, 0);
						frintezza.doDie(frintezza);
						frintezza = null;
						ThreadPoolManager.getInstance().schedule(new Die(4), 7000);
						break;
					
					case 4:
						for (Player pc : getPlayers())
						{
							pc.leaveMovieMode();
						}
						
						cleanUp();
						break;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	void cleanUp()
	{
		startCollapseTimer(15 * 60 * 1000L);
		
		for (Player p : getPlayers())
		{
			p.sendPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(15));
		}
		
		for (NpcInstance n : getNpcs())
		{
			n.deleteMe();
		}
	}
	
	// Hack: ToRemove when doors will operate normally in reflections
	void blockUnblockNpcs(boolean block, int[] npcArray)
	{
		for (NpcInstance n : getNpcs())
		{
			if (ArrayUtils.contains(npcArray, n.getNpcId()))
			{
				if (block)
				{
					n.block();
					n.setIsInvul(true);
				}
				else
				{
					n.unblock();
					n.setIsInvul(false);
				}
			}
		}
	}
	
	public final class CurrentHpListener implements OnCurrentHpDamageListener
	{
		@Override
		public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill)
		{
			if (actor.isDead() || (actor != weakScarlet))
			{
				return;
			}
			
			double newHp = actor.getCurrentHp() - damage;
			double maxHp = actor.getMaxHp();
			
			switch (_scarletMorph)
			{
				case 1:
					if (newHp < (0.75 * maxHp))
					{
						_scarletMorph = 2;
						ThreadPoolManager.getInstance().schedule(new SecondMorph(1), 1100);
					}
					
					break;
				
				case 2:
					if (newHp < (0.1 * maxHp))
					{
						_scarletMorph = 3;
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(1), 2000);
					}
					
					break;
			}
		}
	}
	
	private class DeathListener implements OnDeathListener
	{
		public DeathListener()
		{
		}
		
		@Override
		public void onDeath(Creature self, Creature killer)
		{
			if (self.isNpc())
			{
				if (self.getNpcId() == HallAlarmDevice)
				{
					for (int hallADoor : hallADoors)
					{
						openDoor(hallADoor);
					}
					
					blockUnblockNpcs(false, blockANpcs);
					
					for (NpcInstance n : getNpcs())
					{
						if (ArrayUtils.contains(blockANpcs, n.getNpcId()))
						{
							n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getPlayers().get(Rnd.get(getPlayers().size())), 200);
						}
					}
				}
				else if (ArrayUtils.contains(blockANpcs, self.getNpcId()))
				{
					// ToCheck: find easier way
					for (NpcInstance n : getNpcs())
					{
						if (ArrayUtils.contains(blockANpcs, n.getNpcId()) && !n.isDead())
						{
							return;
						}
					}
					
					for (int corridorADoor : corridorADoors)
					{
						openDoor(corridorADoor);
					}
					
					blockUnblockNpcs(true, blockBNpcs);
				}
				else if (self.getNpcId() == DarkChoirPlayer)
				{
					for (NpcInstance n : getNpcs())
					{
						if ((n.getNpcId() == DarkChoirPlayer) && !n.isDead())
						{
							return;
						}
					}
					
					for (int hallBDoor : hallBDoors)
					{
						openDoor(hallBDoor);
					}
					
					blockUnblockNpcs(false, blockBNpcs);
				}
				else if (ArrayUtils.contains(blockBNpcs, self.getNpcId()))
				{
					if (Rnd.chance(10))
					{
						((NpcInstance) self).dropItem(killer.getPlayer(), DewdropItem, 1);
					}
					
					// ToCheck: find easier way
					for (NpcInstance n : getNpcs())
					{
						if ((ArrayUtils.contains(blockBNpcs, n.getNpcId()) || ArrayUtils.contains(blockANpcs, n.getNpcId())) && !n.isDead())
						{
							return;
						}
					}
					
					for (int corridorBDoor : corridorBDoors)
					{
						openDoor(corridorBDoor);
					}
					
					ThreadPoolManager.getInstance().schedule(new FrintezzaStart(), battleStartDelay);
				}
				else if (self.getNpcId() == _weakScarletId)
				{
					self.decayMe();
					return;
				}
				else if (self.getNpcId() == _strongScarletId)
				{
					ThreadPoolManager.getInstance().schedule(new Die(1), 10);
					setReenterTime(System.currentTimeMillis());
				}
			}
		}
	}
	
	public final class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			if (cha.isNpc() && ((cha.getNpcId() == _weakScarletId) || (cha.getNpcId() == _strongScarletId)))
			{
				cha.teleToLocation(new Location(-87784, -153304, -9176));
				((NpcInstance) cha).getAggroList().clear(true);
				cha.setCurrentHpMp(cha.getMaxHp(), cha.getMaxMp());
				cha.broadcastCharInfo();
			}
		}
	}
	
	@Override
	protected void onCollapse()
	{
		super.onCollapse();
		
		if (musicTask != null)
		{
			musicTask.cancel(true);
		}
	}
}