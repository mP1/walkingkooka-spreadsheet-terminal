package test;

import com.google.gwt.junit.client.GWTTestCase;

@walkingkooka.j2cl.locale.LocaleAware
public class TestGwtTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "test.Test";
    }

    public void testAssertEquals() {
        assertEquals(
            1,
            1
        );
    }

    private static void checkEquals(final Object expected,
                                    final Object actual,
                                    final String message) {
        assertEquals(
            message,
            expected,
            actual
        );
    }
}

