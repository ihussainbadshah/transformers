package org.eclipse.ecsp.transform;

import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.transform.Transformer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for testing the functionality of Transformer.
 *
 * @author karora
 *
 */
public class TransformerUnitTest {

    /** The transformer. */
    private TestValueTransformer transformer;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        transformer = new TestValueTransformer();
    }
    
    /**
     * The Class TestValueTransformer.
     */
    class TestValueTransformer implements Transformer {

        /**
         * To blob.
         *
         * @param value the value
         * @return the byte[]
         */
        @Override
        public byte[] toBlob(IgniteEvent value) {
            return new byte[0];
        }
        
        /**
         * Gets the source of this Transformer.
         *
         * @return the source
         */
        @Override
        public String getSource() {
            return "IGNITE";
        }
        
        /**
         * From blob.
         *
         * @param value the value
         * @param igniteKey the ignite key
         * @return the ignite event
         */
        @Override
        public IgniteEvent fromBlob(byte[] value, IgniteKey<?> igniteKey) {
            IgniteEventImpl event = new IgniteEventImpl();            
            event.setSourceDeviceId((String) igniteKey.getKey());
            return event;
        }
    }

    /**
     * Testing if the fromBlob method is accepting IgniteKey as param.
     * 
     */
    @Test
    public void testTransformerFromBlobWithKey() {
        String key = "key1";
        IgniteKey<String> igniteKey = new IgniteStringKey(key);
        IgniteEvent event = transformer.fromBlob("test".getBytes(), igniteKey);
        Assert.assertEquals("Key should match in transformed event", key, event.getSourceDeviceId());
    }

}