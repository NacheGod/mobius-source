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
package lineage2.gameserver.network.serverpackets;

public class ExBR_BuffEventState extends L2GameServerPacket
{
	private final int _type; // 1 - %, 2 - npcId
	private final int _value; // depending on type: for type 1 - % value; for type 2 -
	// 20573-20575
	private final int _state; // 0-1
	private final int _endtime; // only when type 2 as unix time in seconds from 1970
	
	public ExBR_BuffEventState(int type, int value, int state, int endtime)
	{
		_type = type;
		_value = value;
		_state = state;
		_endtime = endtime;
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:DB ExBrBuffEventState";
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(0xDC);
		writeD(_type);
		writeD(_value);
		writeD(_state);
		writeD(_endtime);
	}
}