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

import lineage2.gameserver.model.instances.NpcInstance;
import lineage2.gameserver.model.quest.Quest;
import lineage2.gameserver.model.quest.QuestState;
import lineage2.gameserver.scripts.ScriptFile;

public class Q00614_SlayTheEnemyCommander extends Quest implements ScriptFile
{
	private static final int DURAI = 31377;
	private static final int KETRAS_COMMANDER_TAYR = 25302;
	private static final int MARK_OF_VARKA_ALLIANCE4 = 7224;
	private static final int MARK_OF_VARKA_ALLIANCE5 = 7225;
	private static final int HEAD_OF_TAYR = 7241;
	private static final int FEATHER_OF_WISDOM = 7230;
	
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
	
	public Q00614_SlayTheEnemyCommander()
	{
		super(true);
		addStartNpc(DURAI);
		addKillId(KETRAS_COMMANDER_TAYR);
		addQuestItem(HEAD_OF_TAYR);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		
		if (event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "elder_ashas_barka_durai_q0614_0104.htm";
			st.setCond(1);
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("614_3"))
		{
			if (st.getQuestItemsCount(HEAD_OF_TAYR) >= 1)
			{
				htmltext = "elder_ashas_barka_durai_q0614_0201.htm";
				st.takeItems(HEAD_OF_TAYR, -1);
				st.giveItems(FEATHER_OF_WISDOM, 1);
				st.addExpAndSp(0, 10000);
				st.unset("cond");
				st.playSound(SOUND_FINISH);
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "elder_ashas_barka_durai_q0614_0106.htm";
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		
		if (cond == 0)
		{
			if (st.getPlayer().getLevel() >= 75)
			{
				if ((st.getQuestItemsCount(MARK_OF_VARKA_ALLIANCE4) == 1) || (st.getQuestItemsCount(MARK_OF_VARKA_ALLIANCE5) == 1))
				{
					htmltext = "elder_ashas_barka_durai_q0614_0101.htm";
				}
				else
				{
					htmltext = "elder_ashas_barka_durai_q0614_0102.htm";
					st.exitCurrentQuest(true);
				}
			}
			else
			{
				htmltext = "elder_ashas_barka_durai_q0614_0103.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if ((cond == 1) && (st.getQuestItemsCount(HEAD_OF_TAYR) == 0))
		{
			htmltext = "elder_ashas_barka_durai_q0614_0106.htm";
		}
		else if ((cond == 2) && (st.getQuestItemsCount(HEAD_OF_TAYR) >= 1))
		{
			htmltext = "elder_ashas_barka_durai_q0614_0105.htm";
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if (st.getCond() == 1)
		{
			st.giveItems(HEAD_OF_TAYR, 1);
			st.setCond(2);
			st.playSound(SOUND_ITEMGET);
		}
		
		return null;
	}
}
