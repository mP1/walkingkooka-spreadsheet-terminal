package test;


import com.google.j2cl.junit.apt.J2clTestInput;
import org.junit.Assert;
import org.junit.Test;

// copied from Sample
@com.google.j2cl.junit.apt.J2clTestInput(J2clTest.class)
public class J2clTest {

    @Test
    public void testAssertEquals() {
        checkEquals(
                1,
                1
        );
    }

    private static void checkEquals(final Object expected,
                                    final Object actual,
                                    final String message) {
        Assert.assertEquals(message, expected, actual);
    }
}


