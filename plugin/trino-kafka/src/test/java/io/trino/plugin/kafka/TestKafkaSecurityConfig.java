/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.kafka;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.airlift.configuration.testing.ConfigAssertions.assertFullMapping;
import static io.airlift.configuration.testing.ConfigAssertions.assertRecordedDefaults;
import static io.airlift.configuration.testing.ConfigAssertions.recordDefaults;
import static org.apache.kafka.common.security.auth.SecurityProtocol.PLAINTEXT;
import static org.apache.kafka.common.security.auth.SecurityProtocol.SASL_PLAINTEXT;
import static org.apache.kafka.common.security.auth.SecurityProtocol.SASL_SSL;
import static org.apache.kafka.common.security.auth.SecurityProtocol.SSL;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestKafkaSecurityConfig
{
    @Test
    public void testDefaults()
    {
        assertRecordedDefaults(recordDefaults(KafkaSecurityConfig.class)
                .setSecurityProtocol(null));
    }

    @Test
    public void testExplicitPropertyMappings()
    {
        Map<String, String> properties = ImmutableMap.of("kafka.security-protocol", "SSL");

        KafkaSecurityConfig expected = new KafkaSecurityConfig()
                .setSecurityProtocol(SSL);

        assertFullMapping(properties, expected);
    }

    @Test
    public void testValidSecurityProtocols()
    {
        new KafkaSecurityConfig()
                .setSecurityProtocol(PLAINTEXT)
                .validate();

        new KafkaSecurityConfig()
                .setSecurityProtocol(SSL)
                .validate();
    }

    @Test
    public void testInvalidSecurityProtocol()
    {
        assertThatThrownBy(() -> new KafkaSecurityConfig()
                .setSecurityProtocol(SASL_PLAINTEXT)
                .validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only PLAINTEXT and SSL security protocols are supported. See 'kafka.config.resources' if other security protocols are needed");

        assertThatThrownBy(() -> new KafkaSecurityConfig()
                .setSecurityProtocol(SASL_SSL)
                .validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only PLAINTEXT and SSL security protocols are supported. See 'kafka.config.resources' if other security protocols are needed");
    }
}
