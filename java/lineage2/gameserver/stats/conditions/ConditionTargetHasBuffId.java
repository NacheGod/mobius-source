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
package lineage2.gameserver.stats.conditions;

import java.util.List;

import lineage2.gameserver.model.Creature;
import lineage2.gameserver.model.Effect;
import lineage2.gameserver.stats.Env;

/**
 * @author Mobius
 * @version $Revision: 1.0 $
 */
public final class ConditionTargetHasBuffId extends Condition
{
	private final int _id;
	private final int _level;
	
	/**
	 * Constructor for ConditionTargetHasBuffId.
	 * @param id int
	 * @param level int
	 */
	public ConditionTargetHasBuffId(int id, int level)
	{
		_id = id;
		_level = level;
	}
	
	/**
	 * Method testImpl.
	 * @param env Env
	 * @return boolean
	 */
	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		
		if (target == null)
		{
			return false;
		}
		
		if (_level == -1)
		{
			return target.getEffectList().getEffectsBySkillId(_id) != null;
		}
		
		List<Effect> el = target.getEffectList().getEffectsBySkillId(_id);
		
		if (el == null)
		{
			return false;
		}
		
		for (Effect effect : el)
		{
			if ((effect != null) && (effect.getSkill().getLevel() >= _level))
			{
				return true;
			}
		}
		
		return false;
	}
}
