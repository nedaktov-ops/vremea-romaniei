package com.vremea.romaniei.domain
import com.vremea.romaniei.domain.model.toWindDirection
import com.vremea.romaniei.domain.model.toWindDirectionRo
import org.junit.Test
import org.junit.Assert.*
class WeatherExtensionsTest {
    @Test fun `north wind returns N`() { assertEquals("N", 0.toWindDirection()) }
    @Test fun `south wind returns S`() { assertEquals("S", 180.toWindDirection()) }
    @Test fun `romanian west wind returns V`() { assertEquals("V", 270.toWindDirectionRo()) }
}
