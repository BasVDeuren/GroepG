package be.kdg.spacecrack.model.game.action;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

public interface Action {
    public abstract int getCommandPointsCost();
    public abstract int getCrackCost();
     public abstract void execute();
}
