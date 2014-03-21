package be.kdg.spacecrack.utilities;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Random;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
@Component("generator")
public class TokenStringGenerator implements ITokenStringGenerator {
    private Random random;

    public TokenStringGenerator(long seed) {
        random = new Random(seed);
    }

    public TokenStringGenerator() {
         random = new Random();
    }

    @Override
    public String generateTokenString(int length) {
        return new BigInteger(130*length/32, random ).toString(32);
    }
}
