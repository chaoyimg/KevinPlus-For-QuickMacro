/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kevin.utils;

public final class MSTimer {

    private long time = -1L;

    public boolean hasTimePassed(final long MS) {
        return System.currentTimeMillis() >= time + MS;
    }

    public boolean hasTimePassed(final int MS) {
        return System.currentTimeMillis() >= time + MS;
    }

    public long hasTimeLeft(final long MS) {
        return (MS + time) - System.currentTimeMillis();
    }

    public void reset() {
        time = System.currentTimeMillis();
    }
}