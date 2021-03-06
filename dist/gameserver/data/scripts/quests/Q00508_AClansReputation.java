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
package quests;

import lineage2.gameserver.Config;
import lineage2.gameserver.model.Player;
import lineage2.gameserver.model.instances.NpcInstance;
import lineage2.gameserver.model.pledge.Clan;
import lineage2.gameserver.model.quest.Quest;
import lineage2.gameserver.model.quest.QuestState;
import lineage2.gameserver.network.serverpackets.SystemMessage;
import lineage2.gameserver.scripts.ScriptFile;
import lineage2.gameserver.utils.Util;

public class Q00508_AClansReputation extends Quest implements ScriptFile
{
	private static final int SIR_ERIC_RODEMAI = 30868;
	private static final int NUCLEUS_OF_FLAMESTONE_GIANT = 8494;
	private static final int THEMIS_SCALE = 8277;
	private static final int Hisilromes_Heart = 14883;
	private static final int TIPHON_SHARD = 8280;
	private static final int GLAKIS_NECLEUS = 8281;
	private static final int RAHHAS_FANG = 8282;
	private static final int FLAMESTONE_GIANT = 25524;
	private static final int PALIBATI_QUEEN_THEMIS = 25252;
	private static final int Shilens_Priest_Hisilrome = 25478;
	private static final int GARGOYLE_LORD_TIPHON = 25255;
	private static final int LAST_LESSER_GIANT_GLAKI = 25245;
	private static final int RAHHA = 25051;
	private static final int[][] REWARDS_LIST =
	{
		{
			0,
			0
		},
		{
			PALIBATI_QUEEN_THEMIS,
			THEMIS_SCALE,
			85
		},
		{
			Shilens_Priest_Hisilrome,
			Hisilromes_Heart,
			65
		},
		{
			GARGOYLE_LORD_TIPHON,
			TIPHON_SHARD,
			50
		},
		{
			LAST_LESSER_GIANT_GLAKI,
			GLAKIS_NECLEUS,
			125
		},
		{
			RAHHA,
			RAHHAS_FANG,
			71
		},
		{
			FLAMESTONE_GIANT,
			NUCLEUS_OF_FLAMESTONE_GIANT,
			80
		}
	};
	private static final int[][] RADAR =
	{
		{
			0,
			0,
			0
		},
		{
			192346,
			21528,
			-3648
		},
		{
			191979,
			54902,
			-7658
		},
		{
			170038,
			-26236,
			-3824
		},
		{
			171762,
			55028,
			-5992
		},
		{
			117232,
			-9476,
			-3320
		},
		{
			144218,
			-5816,
			-4722
		},
	};
	
	public Q00508_AClansReputation()
	{
		super(PARTY_ALL);
		addStartNpc(SIR_ERIC_RODEMAI);
		
		for (int[] i : REWARDS_LIST)
		{
			if (i[0] > 0)
			{
				addKillId(i[0]);
			}
			
			if (i[1] > 0)
			{
				addQuestItem(i[1]);
			}
		}
	}
	
	@Override
	public String onEvent(String event, QuestState qs, NpcInstance npc)
	{
		String htmltext = event;
		
		if (event.equals("30868-0.htm") && (qs.getCond() == 0))
		{
			qs.setCond(1);
			qs.setState(STARTED);
		}
		else if (Util.isNumber(event))
		{
			int evt = Integer.parseInt(event);
			qs.set("raid", event);
			htmltext = "30868-" + event + ".htm";
			int x = RADAR[evt][0];
			int y = RADAR[evt][1];
			int z = RADAR[evt][2];
			
			if ((x + y + z) > 0)
			{
				qs.addRadar(x, y, z);
			}
			
			qs.playSound(SOUND_ACCEPT);
		}
		else if (event.equals("30868-7.htm"))
		{
			qs.playSound(SOUND_FINISH);
			qs.exitCurrentQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState qs)
	{
		String htmltext = "noquest";
		final Clan clan = qs.getPlayer().getClan();
		
		if (clan == null)
		{
			qs.exitCurrentQuest(true);
			htmltext = "30868-0a.htm";
		}
		else if (clan.getLeader().getPlayer() != qs.getPlayer())
		{
			qs.exitCurrentQuest(true);
			htmltext = "30868-0a.htm";
		}
		else if (clan.getLevel() < 5)
		{
			qs.exitCurrentQuest(true);
			htmltext = "30868-0b.htm";
		}
		else
		{
			final int cond = qs.getCond();
			final int raid = qs.getInt("raid");
			final int id = qs.getState();
			
			if ((id == CREATED) && (cond == 0))
			{
				htmltext = "30868-0c.htm";
			}
			else if ((id == STARTED) && (cond == 1))
			{
				int item = REWARDS_LIST[raid][1];
				long count = qs.getQuestItemsCount(item);
				
				if (count == 0)
				{
					htmltext = "30868-" + raid + "a.htm";
				}
				else if (count == 1)
				{
					htmltext = "30868-" + raid + "b.htm";
					int increasedPoints = clan.incReputation(REWARDS_LIST[raid][2], true, "Q00508_AClansReputation");
					qs.getPlayer().sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_SUCCESSFULLY_COMPLETED_A_CLAN_QUEST_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE).addNumber(increasedPoints));
					qs.takeItems(item, 1);
				}
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		Player clan_leader;
		
		try
		{
			clan_leader = qs.getPlayer().getClan().getLeader().getPlayer();
		}
		catch (Exception E)
		{
			return null;
		}
		
		if (clan_leader == null)
		{
			return null;
		}
		
		if (!qs.getPlayer().equals(clan_leader) && (clan_leader.getDistance(npc) > Config.ALT_PARTY_DISTRIBUTION_RANGE))
		{
			return null;
		}
		
		final QuestState state = clan_leader.getQuestState(getName());
		
		if ((state == null) || !state.isStarted() || (state.getCond() != 1))
		{
			return null;
		}
		
		final int raid = REWARDS_LIST[qs.getInt("raid")][0];
		final int item = REWARDS_LIST[qs.getInt("raid")][1];
		
		if ((npc.getId() == raid) && (qs.getQuestItemsCount(item) == 0))
		{
			qs.giveItems(item, 1);
			qs.playSound(SOUND_MIDDLE);
		}
		
		return null;
	}
	
	@Override
	public void onLoad()
	{
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
}
