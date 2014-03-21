package be.kdg.spacecrack.unittests;

import be.kdg.spacecrack.utilities.ITokenStringGenerator;
import be.kdg.spacecrack.utilities.TokenStringGenerator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
public class TokenStringGeneratorTests {

    @Test
    public void testGenerator() throws Exception {
        ITokenStringGenerator tokenValueGenerator = new TokenStringGenerator(1234L);
        String actual = tokenValueGenerator.generateTokenString(32);
        assertEquals("38gqit55s18bd2r37jf3lsgf7l", actual);
    }
}
