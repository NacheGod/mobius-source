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
package lineage2.loginserver.crypt;

import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordHash
{
	private final static Logger _log = LoggerFactory.getLogger(PasswordHash.class);
	private final String name;
	
	public PasswordHash(String name)
	{
		this.name = name;
	}
	
	public boolean compare(String password, String expected)
	{
		try
		{
			return encrypt(password).equals(expected);
		}
		catch (Exception e)
		{
			_log.error(name + ": encryption error!", e);
			return false;
		}
	}
	
	public String encrypt(String password) throws Exception
	{
		AbstractChecksum checksum = JacksumAPI.getChecksumInstance(name);
		checksum.setEncoding("BASE64");
		checksum.update(password.getBytes());
		return checksum.format("#CHECKSUM");
	}
}